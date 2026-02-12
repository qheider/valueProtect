package info.quazi.valueProtect.service;

import info.quazi.valueProtect.dto.*;
import info.quazi.valueProtect.entity.*;
import info.quazi.valueProtect.repository.*;
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

    private final AppraisalRepository appraisalRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyFeaturesRepository propertyFeaturesRepository;
    private final AppraisalDocumentRepository appraisalDocumentRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityContextService securityContextService;
    private final FileUploadService fileUploadService;

    public AppraisalService(AppraisalRepository appraisalRepository,
                           PropertyRepository propertyRepository,
                           PropertyFeaturesRepository propertyFeaturesRepository,
                           AppraisalDocumentRepository appraisalDocumentRepository,
                           EmployeeRepository employeeRepository,
                           SecurityContextService securityContextService,
                           FileUploadService fileUploadService) {
        this.appraisalRepository = appraisalRepository;
        this.propertyRepository = propertyRepository;
        this.propertyFeaturesRepository = propertyFeaturesRepository;
        this.appraisalDocumentRepository = appraisalDocumentRepository;
        this.employeeRepository = employeeRepository;
        this.securityContextService = securityContextService;
        this.fileUploadService = fileUploadService;
    }

    public AppraisalDto createAppraisal(CreateAppraisalRequest request) {
        // Get current employee context
        String currentEmployeeId = securityContextService.getCurrentEmployeeId();
        Employee currentEmployee = employeeRepository.findById(Long.parseLong(currentEmployeeId))
            .orElseThrow(() -> new RuntimeException("Current employee not found"));
        
        // Handle property creation or retrieval
        String propertyId = handleProperty(request.getProperty());
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new RuntimeException("Property not found"));
        
        // Create appraisal
        Appraisal appraisal = new Appraisal(UUID.randomUUID().toString());
        appraisal.setProperty(property);
        appraisal.setAppraiser(currentEmployee);
        appraisal.setEffectiveDate(request.getEffectiveDate());
        appraisal.setReportDate(request.getReportDate());
        appraisal.setAppraisedValue(request.getAppraisedValue());
        appraisal.setPurpose(request.getPurpose());
        appraisal.setStatus(request.getStatus());
        appraisal.setFinalReportUrl(request.getFinalReportUrl());
        
        Appraisal savedAppraisal = appraisalRepository.save(appraisal);
        
        return convertToDto(savedAppraisal);
    }

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
        
        List<Appraisal> appraisals;
        if (isAdmin) {
            // Admin can see all company appraisals
            appraisals = appraisalRepository.findByCompanyId(companyId);
        } else {
            // Regular user can only see their own appraisals
            appraisals = appraisalRepository.findByAppraiserIdAndCompanyId(Long.parseLong(currentEmployeeId), companyId);
        }
        
        return appraisals.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

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
        
        appraisalRepository.delete(appraisal);
    }

    public AppraisalDocumentDto uploadDocument(String appraisalId, 
                                             MultipartFile file, 
                                             AppraisalDocument.DocumentType documentType) throws IOException {
        // Verify access to appraisal
        Long companyId = securityContextService.getCurrentCompanyId();
        Appraisal appraisal = appraisalRepository.findByAppraisalIdAndCompanyId(appraisalId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found or access denied"));
        
        if (!canModifyAppraisal(appraisal)) {
            throw new SecurityException("Access denied: You can only upload documents to your own appraisals");
        }
        
        // Upload file
        String fileUrl = fileUploadService.uploadFile(file, appraisalId, documentType.getDisplayName());
        
        // Create document record
        AppraisalDocument document = new AppraisalDocument(
                UUID.randomUUID().toString(), 
                appraisalId, 
                documentType
        );
        document.setFileName(file.getOriginalFilename());
        document.setFileUrl(fileUrl);
        
        AppraisalDocument savedDocument = appraisalDocumentRepository.save(document);
        
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
        
        return isAdmin || (appraisal.getAppraiser() != null && 
                          currentEmployeeId.equals(String.valueOf(appraisal.getAppraiser().getId())));
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