package info.quazi.valueProtect.service;

import info.quazi.valueProtect.dto.*;
import info.quazi.valueProtect.entity.*;
import info.quazi.valueProtect.exception.UnauthorizedAppraiserAssignmentException;
import info.quazi.valueProtect.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional

public class AppraisalService {
    private static final Logger log = LoggerFactory.getLogger(AppraisalService.class);

    private final AppraisalRepository appraisalRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyFeaturesRepository propertyFeaturesRepository;
    private final AppraisalDocumentRepository appraisalDocumentRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final SecurityContextService securityContextService;
    private final FileUploadService fileUploadService;

    public AppraisalService(AppraisalRepository appraisalRepository,
                           PropertyRepository propertyRepository,
                           PropertyFeaturesRepository propertyFeaturesRepository,
                           AppraisalDocumentRepository appraisalDocumentRepository,
                           EmployeeRepository employeeRepository,
                           CompanyRepository companyRepository,
                           UserRepository userRepository,
                           SecurityContextService securityContextService,
                           FileUploadService fileUploadService) {
        this.appraisalRepository = appraisalRepository;
        this.propertyRepository = propertyRepository;
        this.propertyFeaturesRepository = propertyFeaturesRepository;
        this.appraisalDocumentRepository = appraisalDocumentRepository;
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.securityContextService = securityContextService;
        this.fileUploadService = fileUploadService;
    }

    public AppraisalDto createAppraisal(CreateAppraisalRequest request) {
        // Get current employee context
        String currentEmployeeId = securityContextService.getCurrentEmployeeId();
        Employee currentEmployee = employeeRepository.findById(Long.parseLong(currentEmployeeId))
            .orElseThrow(() -> new RuntimeException("Current employee not found"));
        
        boolean isAdmin = securityContextService.isCurrentUserAdmin();
        boolean isLender = securityContextService.isCurrentUserLender();
        
        // Handle property creation or retrieval
        String propertyId = handleProperty(request.getProperty());
        if (propertyId == null) {
            throw new RuntimeException("Property ID cannot be null");
        }
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new RuntimeException("Property not found"));
        
        // Create appraisal
        Appraisal appraisal = new Appraisal(UUID.randomUUID().toString());
        appraisal.setProperty(property);

        Employee appraiserEmployee;
        Employee lenderEmployee = null;
        
        // Handle employee assignment based on role and request parameters
        if (isAdmin) {
            // Admin can specify both appraiser and lender, or defaults
            if (request.getAppraiserId() != null) {
                @SuppressWarnings("null")
                Long appraiserId = request.getAppraiserId();
                @SuppressWarnings("null")
                Employee appraiser = employeeRepository.findById(appraiserId)
                    .orElseThrow(() -> new RuntimeException("Appraiser not found"));
                validateSameCompany(appraiser, currentEmployee.getCompany().getId());
                appraiserEmployee = appraiser;
            } else {
                // If admin doesn't specify appraiser, they become the appraiser
                appraiserEmployee = currentEmployee;
            }
            
            if (request.getLenderId() != null) {
                @SuppressWarnings("null")
                Long lenderId = request.getLenderId();
                @SuppressWarnings("null")
                Employee lender = employeeRepository.findById(lenderId)
                    .orElseThrow(() -> new RuntimeException("Lender employee not found"));
                validateSameCompany(lender, currentEmployee.getCompany().getId());
                lenderEmployee = lender;
            }
        } else if (isLender) {
            // Lender creates appraisal and assigns themselves as lender
            if (request.getAppraiserId() != null) {
                // Use specified appraiser if provided
                @SuppressWarnings("null")
                Long appraiserId = request.getAppraiserId();
                @SuppressWarnings("null")
                Employee specifiedAppraiser = employeeRepository.findById(appraiserId)
                    .orElseThrow(() -> new RuntimeException("Appraiser not found"));
                validateSameCompany(specifiedAppraiser, currentEmployee.getCompany().getId());
                appraiserEmployee = specifiedAppraiser;
            } else {
                // Automatically assign a random available appraiser
                appraiserEmployee = findRandomAvailableAppraiser(currentEmployee.getCompany().getId());
                if (appraiserEmployee == null) {
                    throw new RuntimeException("No available preferred appraisers found for the lender company");
                }
            }
            
            lenderEmployee = currentEmployee;
        } else {
            // Regular employee (appraiser) assigns themselves as appraiser
            appraiserEmployee = currentEmployee;
            
            // Can optionally specify a lender
            if (request.getLenderId() != null) {
                @SuppressWarnings("null")
                Long lenderId = request.getLenderId();
                @SuppressWarnings("null")
                Employee lender = employeeRepository.findById(lenderId)
                    .orElseThrow(() -> new RuntimeException("Lender employee not found"));
                validateSameCompany(lender, currentEmployee.getCompany().getId());
                lenderEmployee = lender;
            }
        }

        if (lenderEmployee == null || lenderEmployee.getCompany() == null) {
            throw new UnauthorizedAppraiserAssignmentException("Lender company is required for assignment");
        }

        validateAppraiserEligibilityForLender(appraiserEmployee, lenderEmployee.getCompany());
        appraisal.setAppraiser(appraiserEmployee);
        appraisal.setLenderEmployee(lenderEmployee);
        appraisal.setLenderCompany(lenderEmployee.getCompany());
        
        appraisal.setEffectiveDate(request.getEffectiveDate());
        appraisal.setReportDate(request.getReportDate());
        appraisal.setAppraisedValue(request.getAppraisedValue());
        appraisal.setPurpose(request.getPurpose());
        appraisal.setStatus(request.getStatus());
        appraisal.setFinalReportUrl(request.getFinalReportUrl());
        
        @SuppressWarnings("null")
        Appraisal savedAppraisal = appraisalRepository.save(appraisal);
        
        return convertToDto(savedAppraisal);
    }

    @SuppressWarnings("null")
    public AppraisalDto updateAppraisal(String appraisalId, UpdateAppraisalRequest request) {
        // Verify access
        Long companyId = securityContextService.getCurrentCompanyId();
        Appraisal appraisal = appraisalRepository.findByAppraisalIdAndCompanyId(appraisalId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found or access denied"));
        
        // Check if current user can modify this appraisal
        if (!canModifyAppraisal(appraisal)) {
            throw new SecurityException("Access denied: You can only modify your own appraisals");
        }
        
        // Update fields
        if (request.getEffectiveDate() != null) {
            appraisal.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getReportDate() != null) {
            appraisal.setReportDate(request.getReportDate());
        }
        if (request.getAppraisedValue() != null) {
            appraisal.setAppraisedValue(request.getAppraisedValue());
        }
        if (request.getPurpose() != null) {
            appraisal.setPurpose(request.getPurpose());
        }
        if (request.getStatus() != null) {
            appraisal.setStatus(request.getStatus());
        }
        if (request.getLenderId() != null) {
            @SuppressWarnings("null")
            Long lenderId = request.getLenderId();
            @SuppressWarnings("null")
            Employee lenderEmployee = employeeRepository.findById(lenderId)
                .orElseThrow(() -> new RuntimeException("Lender employee not found"));
                
            // Verify lender employee belongs to the same company
            if (!lenderEmployee.getCompany().getId().equals(companyId)) {
                throw new SecurityException("Lender employee must belong to the same company");
            }
            
            appraisal.setLenderEmployee(lenderEmployee);
            appraisal.setLenderCompany(lenderEmployee.getCompany());
        }
        if (request.getFinalReportUrl() != null) {
            appraisal.setFinalReportUrl(request.getFinalReportUrl());
        }
        
        Appraisal savedAppraisal = appraisalRepository.save(appraisal);
        return convertToDto(savedAppraisal);
    }

    @Transactional(readOnly = true)
    public AppraisalDto getAppraisal(String appraisalId) {
        Long companyId = securityContextService.getCurrentCompanyId();
        Appraisal appraisal = appraisalRepository.findByAppraisalIdAndCompanyId(appraisalId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found or access denied"));
        
        return convertToDto(appraisal);
    }

    @Transactional(readOnly = true)
    public List<AppraisalDto> getAppraisals() {
        String currentEmployeeId = securityContextService.getCurrentEmployeeId();
        Long companyId = securityContextService.getCurrentCompanyId();
        boolean isAdmin = securityContextService.isCurrentUserAdmin();
        boolean isLender = securityContextService.isCurrentUserLender();
        
        List<Appraisal> appraisals;
        if (isAdmin) {
            // Admin can see all company appraisals
            appraisals = appraisalRepository.findByCompanyId(companyId);
        } else if (isLender) {
            // Lender can see all appraisals for their lender company
            appraisals = appraisalRepository.findByLenderCompanyId(companyId);
        } else {
            // Regular user can only see their own appraisals as appraiser
            appraisals = appraisalRepository.findByAppraiserIdAndCompanyId(Long.parseLong(currentEmployeeId), companyId);
        }
        
        return appraisals.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    public void deleteAppraisal(String appraisalId) {
        Long companyId = securityContextService.getCurrentCompanyId();
        Appraisal appraisal = appraisalRepository.findByAppraisalIdAndCompanyId(appraisalId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found or access denied"));
        
        if (!canModifyAppraisal(appraisal)) {
            throw new SecurityException("Access denied: You can only delete your own appraisals");
        }
        
        // Delete associated documents and files
        List<AppraisalDocument> documents = appraisalDocumentRepository.findByAppraisalId(appraisalId);
        for (AppraisalDocument document : documents) {
            fileUploadService.deleteFile(document.getFileUrl());
        }
        
        @SuppressWarnings("null")
        Appraisal nonNullAppraisal = appraisal;
        appraisalRepository.delete(nonNullAppraisal);
    }

    public AppraisalDocumentDto uploadDocument(String appraisalId, 
                                             MultipartFile file, 
                                             AppraisalDocument.DocumentType documentType) throws IOException {
        log.info("=== Starting document upload ===");
        log.info("Appraisal ID: {}", appraisalId);
        log.info("Filename: {}", file.getOriginalFilename());
        log.info("Document Type: {}", documentType);
        log.info("File Size: {} bytes", file.getSize());
        
        // Verify access to appraisal
        Long companyId = securityContextService.getCurrentCompanyId();
        log.debug("Current company ID: {}", companyId);
        
        Appraisal appraisal = appraisalRepository.findByAppraisalIdAndCompanyId(appraisalId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found or access denied"));
        log.debug("Appraisal found and access verified");
        
        if (!canModifyAppraisal(appraisal)) {
            log.warn("Access denied for user to modify appraisal: {}", appraisalId);
            throw new SecurityException("Access denied: You can only upload documents to your own appraisals");
        }
        
        // Upload file
        log.info("Uploading file to filesystem...");
        String fileUrl = fileUploadService.uploadFile(file, appraisalId, documentType.getDisplayName());
        log.info("File uploaded successfully, URL: {}", fileUrl);
        
        // Create document record
        String documentId = UUID.randomUUID().toString();
        log.debug("Creating document record with ID: {}", documentId);
        
        AppraisalDocument document = new AppraisalDocument(
                documentId, 
                appraisalId, 
                documentType
        );
        document.setFileName(file.getOriginalFilename());
        document.setFileUrl(fileUrl);
        
        log.info("Saving document record to database...");
        AppraisalDocument savedDocument = appraisalDocumentRepository.save(document);
        log.info("Document saved successfully with ID: {}", savedDocument.getDocumentId());
        log.info("=== Document upload completed ===");
        
        return convertToDocumentDto(savedDocument);
    }

    @Transactional(readOnly = true)
    public List<AppraisalDocumentDto> getAppraisalDocuments(String appraisalId) {
        // Verify access
        Long companyId = securityContextService.getCurrentCompanyId();
        appraisalRepository.findByAppraisalIdAndCompanyId(appraisalId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found or access denied"));
        
        List<AppraisalDocument> documents = appraisalDocumentRepository.findByAppraisalId(appraisalId);
        return documents.stream()
                .map(this::convertToDocumentDto)
                .collect(Collectors.toList());
    }

    public void deleteDocument(String documentId) {
        Long companyId = securityContextService.getCurrentCompanyId();
        AppraisalDocument document = appraisalDocumentRepository.findByDocumentIdAndCompanyId(documentId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found or access denied"));
        
        // Check if user can modify the appraisal
        Appraisal appraisal = appraisalRepository.findByAppraisalId(document.getAppraisalId())
                .orElseThrow(() -> new IllegalArgumentException("Associated appraisal not found"));
        
        if (!canModifyAppraisal(appraisal)) {
            throw new SecurityException("Access denied: You can only delete documents from your own appraisals");
        }
        
        // Delete file from filesystem
        fileUploadService.deleteFile(document.getFileUrl());
        
        // Delete document record
        appraisalDocumentRepository.delete(document);
    }

    @Transactional
    public AppraisalDto assignAppraiser(String requestId, Long userId) {
        Appraisal appraisal = appraisalRepository.findByAppraisalId(requestId)
                .orElseThrow(() -> new RuntimeException("Appraisal not found with id: " + requestId));

        if (appraisal.getLenderCompany() == null) {
            throw new UnauthorizedAppraiserAssignmentException("Lender company is required for assignment");
        }

        Employee appraiserEmployee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Employee not found for user id: " + userId));
        validateAppraiserEligibilityForLender(appraiserEmployee, appraisal.getLenderCompany());

        appraisal.setAppraiser(appraiserEmployee);
        Appraisal saved = appraisalRepository.save(appraisal);
        return convertToDto(saved);
    }

    private String handleProperty(CreateAppraisalRequest.PropertyInfo propertyInfo) {
        if (propertyInfo.getExistingPropertyId() != null) {
            // Use existing property
            String propertyId = propertyInfo.getExistingPropertyId();
            if (!propertyRepository.existsByPropertyId(propertyId)) {
                throw new IllegalArgumentException("Property not found: " + propertyId);
            }
            return propertyId;
        } else if (propertyInfo.getNewProperty() != null) {
            // Create new property
            return createProperty(propertyInfo.getNewProperty());
        } else {
            throw new IllegalArgumentException("Either existing property ID or new property data must be provided");
        }
    }

    private String createProperty(CreatePropertyRequest request) {
        String propertyId = UUID.randomUUID().toString();
        
        Property property = new Property(propertyId);
        property.setApn(request.getApn());
        property.setAddressLine1(request.getAddressLine1());
        property.setCity(request.getCity());
        property.setStateProvince(request.getStateProvince());
        property.setZipPostalCode(request.getZipPostalCode());
        property.setPropertyType(request.getPropertyType());
        property.setYearBuilt(request.getYearBuilt());
        property.setLotSizeSqft(request.getLotSizeSqft());
        property.setLivingAreaSqft(request.getLivingAreaSqft());
        
        Property savedProperty = propertyRepository.save(property);
        
        // Create property features if provided
        if (request.getFeatures() != null) {
            createPropertyFeatures(propertyId, request.getFeatures());
        }
        
        return savedProperty.getPropertyId();
    }

    private void createPropertyFeatures(String propertyId, CreatePropertyRequest.PropertyFeaturesDto featuresDto) {
        Property property = propertyRepository.findByPropertyId(propertyId)
            .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));
            
        PropertyFeatures features = new PropertyFeatures(UUID.randomUUID().toString(), property);
        features.setBedroomCount(featuresDto.getBedroomCount());
        features.setBathroomCount(featuresDto.getBathroomCount());
        features.setBasementType(featuresDto.getBasementType());
        features.setGarageSpaces(featuresDto.getGarageSpaces());
        features.setHvacType(featuresDto.getHvacType());
        features.setExteriorMaterial(featuresDto.getExteriorMaterial());
        features.setConditionRating(featuresDto.getConditionRating());
        features.setQualityRating(featuresDto.getQualityRating());
        
        propertyFeaturesRepository.save(features);
    }

    private boolean canModifyAppraisal(Appraisal appraisal) {
        String currentEmployeeId = securityContextService.getCurrentEmployeeId();
        boolean isAdmin = securityContextService.isCurrentUserAdmin();
        
        // Admin can modify any appraisal
        if (isAdmin) {
            return true;
        }
        
        // Appraiser can modify their own appraisals
        if (appraisal.getAppraiser() != null && 
            currentEmployeeId.equals(String.valueOf(appraisal.getAppraiser().getId()))) {
            return true;
        }
        
        // Lender can modify appraisals assigned to them
        if (appraisal.getLenderEmployee() != null && 
            currentEmployeeId.equals(String.valueOf(appraisal.getLenderEmployee().getId()))) {
            return true;
        }
        
        return false;
    }

    private void validateSameCompany(Employee employee, Long companyId) {
        if (!employee.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Employee must belong to the same company");
        }
    }

    private void validateAppraiserEligibilityForLender(Employee appraiserEmployee, Company lenderCompany) {
        if (appraiserEmployee == null) {
            throw new UnauthorizedAppraiserAssignmentException("Appraiser employee is required for assignment");
        }

        User appraiserUser = appraiserEmployee.getUser();
        if (!hasRole(appraiserUser, RoleName.APPRAISER)) {
            throw new UnauthorizedAppraiserAssignmentException("User must have APPRAISER role");
        }

        Company appraiserCompany = appraiserEmployee.getCompany();
        if (appraiserCompany == null || appraiserCompany.getCompanyType() != CompanyType.APPRAISAL) {
            throw new UnauthorizedAppraiserAssignmentException("Appraiser must belong to an APPRAISAL company");
        }

        if (lenderCompany == null) {
            throw new UnauthorizedAppraiserAssignmentException("Lender company is required for assignment");
        }

        boolean isPreferred = lenderCompany.getPreferredAppraisalCompanies().stream()
            .anyMatch(company -> company.getId() != null && company.getId().equals(appraiserCompany.getId()));

        if (!isPreferred) {
            throw new UnauthorizedAppraiserAssignmentException(
                    "Appraiser company is not in lender's preferred appraisal list");
        }
    }

    private boolean hasRole(User user, RoleName roleName) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .map(name -> name == null ? "" : name.replace("ROLE_", ""))
                .anyMatch(normalized -> roleName.name().equalsIgnoreCase(normalized));
    }

    private Employee findRandomAvailableAppraiser(Long companyId) {
        // Find lender company by ID
        if (companyId == null) {
            throw new RuntimeException("Company ID cannot be null");
        }
        Company lenderCompany = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found"));

        // Find available appraisers from lender's preferred appraisal companies only
        List<Employee> availableAppraisers = lenderCompany.getPreferredAppraisalCompanies().stream()
            .filter(company -> company.getCompanyType() == CompanyType.APPRAISAL)
            .flatMap(company -> employeeRepository.findByCompanyAndArchivedFalse(company).stream())
            .filter(emp -> !isEmployeeLender(emp))
            .filter(emp -> hasRole(emp.getUser(), RoleName.APPRAISER))
            .collect(Collectors.toList());
            
        if (availableAppraisers.isEmpty()) {
            return null;
        }
        
        // Return random appraiser
        return availableAppraisers.get(new java.util.Random().nextInt(availableAppraisers.size()));
    }

    private boolean isEmployeeLender(Employee employee) {
        if (employee.getUser() == null || employee.getUser().getRoles() == null) {
            return false;
        }
        return employee.getUser().getRoles().stream()
            .anyMatch(role -> "LENDER".equalsIgnoreCase(role.getName()));
    }

    private AppraisalDto convertToDto(Appraisal appraisal) {
        AppraisalDto dto = new AppraisalDto();
        dto.setAppraisalId(appraisal.getAppraisalId());
        
        // Set property ID from relationship
        if (appraisal.getProperty() != null) {
            dto.setPropertyId(appraisal.getProperty().getPropertyId());
        }
        
        // Set appraiser ID and name
        if (appraisal.getAppraiser() != null) {
            dto.setAppraiserId(String.valueOf(appraisal.getAppraiser().getId()));
            dto.setAppraiserName(appraisal.getAppraiser().getFirstName() + " " + appraisal.getAppraiser().getLastName());
        }
        
        // Set lender ID and name
        if (appraisal.getLenderEmployee() != null) {
            dto.setLenderId(String.valueOf(appraisal.getLenderEmployee().getId()));
            dto.setLenderName(appraisal.getLenderEmployee().getFirstName() + " " + appraisal.getLenderEmployee().getLastName());
        }
        
        dto.setEffectiveDate(appraisal.getEffectiveDate());
        dto.setReportDate(appraisal.getReportDate());
        dto.setAppraisedValue(appraisal.getAppraisedValue());
        dto.setPurpose(appraisal.getPurpose());
        dto.setStatus(appraisal.getStatus());
        dto.setFinalReportUrl(appraisal.getFinalReportUrl());
        dto.setCreatedAt(appraisal.getCreatedAt());
        dto.setUpdatedAt(appraisal.getUpdatedAt());
        
        // Set property information
        if (appraisal.getProperty() != null) {
            dto.setProperty(convertToPropertyDto(appraisal.getProperty()));
        }
        
        // Set documents
        List<AppraisalDocumentDto> documentDtos = appraisal.getDocuments().stream()
                .map(this::convertToDocumentDto)
                .collect(Collectors.toList());
        dto.setDocuments(documentDtos);
        
        return dto;
    }

    private AppraisalDto.PropertyDto convertToPropertyDto(Property property) {
        AppraisalDto.PropertyDto dto = new AppraisalDto.PropertyDto();
        dto.setPropertyId(property.getPropertyId());
        dto.setApn(property.getApn());
        dto.setAddressLine1(property.getAddressLine1());
        dto.setCity(property.getCity());
        dto.setStateProvince(property.getStateProvince());
        dto.setZipPostalCode(property.getZipPostalCode());
        dto.setPropertyType(property.getPropertyType());
        dto.setYearBuilt(property.getYearBuilt());
        dto.setLotSizeSqft(property.getLotSizeSqft());
        dto.setLivingAreaSqft(property.getLivingAreaSqft());
        
        if (property.getPropertyFeatures() != null) {
            dto.setFeatures(convertToPropertyFeaturesDto(property.getPropertyFeatures()));
        }
        
        return dto;
    }

    private AppraisalDto.PropertyFeaturesDto convertToPropertyFeaturesDto(PropertyFeatures features) {
        AppraisalDto.PropertyFeaturesDto dto = new AppraisalDto.PropertyFeaturesDto();
        dto.setFeatureId(features.getFeatureId());
        dto.setBedroomCount(features.getBedroomCount());
        dto.setBathroomCount(features.getBathroomCount());
        dto.setBasementType(features.getBasementType());
        dto.setGarageSpaces(features.getGarageSpaces());
        dto.setHvacType(features.getHvacType());
        dto.setExteriorMaterial(features.getExteriorMaterial());
        dto.setConditionRating(features.getConditionRating());
        dto.setQualityRating(features.getQualityRating());
        return dto;
    }

    private AppraisalDocumentDto convertToDocumentDto(AppraisalDocument document) {
        AppraisalDocumentDto dto = new AppraisalDocumentDto();
        dto.setDocumentId(document.getDocumentId());
        dto.setAppraisalId(document.getAppraisalId());
        dto.setDocumentType(document.getDocumentType());
        dto.setFileName(document.getFileName());
        dto.setFileUrl(document.getFileUrl());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setFileSize(fileUploadService.getFileSize(document.getFileUrl()));
        return dto;
    }
}