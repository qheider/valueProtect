package info.quazi.valueProtect.service;

import info.quazi.valueProtect.dto.RegisterCompanyRequest;
import info.quazi.valueProtect.dto.RegisterCompanyResponse;
import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.Role;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.CompanyRepository;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.repository.RoleRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository, 
                         RoleRepository roleRepository, EmployeeRepository employeeRepository,
                         PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Company create(Company company) {
        company.setArchived(false);
        
        if (company.getStatus() == null || company.getStatus().isEmpty()) {
            company.setStatus("ACTIVE");
        }
        
        return companyRepository.save(company);
    }

    @Transactional
    public Company update(Long id, Company companyDetails) {
        Company company = companyRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        if (companyDetails.getName() != null) {
            company.setName(companyDetails.getName());
        }
        if (companyDetails.getCompanyCode() != null) {
            company.setCompanyCode(companyDetails.getCompanyCode());
        }
        if (companyDetails.getEmail() != null) {
            company.setEmail(companyDetails.getEmail());
        }
        if (companyDetails.getPhone() != null) {
            company.setPhone(companyDetails.getPhone());
        }
        if (companyDetails.getWebsite() != null) {
            company.setWebsite(companyDetails.getWebsite());
        }
        if (companyDetails.getAddressLine1() != null) {
            company.setAddressLine1(companyDetails.getAddressLine1());
        }
        if (companyDetails.getAddressLine2() != null) {
            company.setAddressLine2(companyDetails.getAddressLine2());
        }
        if (companyDetails.getCity() != null) {
            company.setCity(companyDetails.getCity());
        }
        if (companyDetails.getState() != null) {
            company.setState(companyDetails.getState());
        }
        if (companyDetails.getCountry() != null) {
            company.setCountry(companyDetails.getCountry());
        }
        if (companyDetails.getPostalCode() != null) {
            company.setPostalCode(companyDetails.getPostalCode());
        }
        if (companyDetails.getTaxId() != null) {
            company.setTaxId(companyDetails.getTaxId());
        }
        if (companyDetails.getRegistrationNumber() != null) {
            company.setRegistrationNumber(companyDetails.getRegistrationNumber());
        }
        if (companyDetails.getDescription() != null) {
            company.setDescription(companyDetails.getDescription());
        }
        if (companyDetails.getStatus() != null) {
            company.setStatus(companyDetails.getStatus());
        }

        @SuppressWarnings("null")
        Company savedCompany = companyRepository.save(company);
        return savedCompany;
    }

    @Transactional(readOnly = true)
    public Company findById(Long id) {
        return companyRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Company> findAll() {
        return companyRepository.findByArchivedFalse();
    }

    @Transactional(readOnly = true)
    public List<Company> findByStatus(String status) {
        return companyRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Company findByCompanyCode(String companyCode) {
        return companyRepository.findByCompanyCode(companyCode)
                .orElseThrow(() -> new RuntimeException("Company not found with code: " + companyCode));
    }

    @Transactional
    public void softDelete(Long id) {
        if (id == null) {
            throw new RuntimeException("Company ID cannot be null");
        }
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        company.setArchived(true);
        companyRepository.save(company);
    }

    @Transactional
    public RegisterCompanyResponse registerCompanyWithAdminUser(RegisterCompanyRequest request) {
        // Validate request
        validateRegistrationRequest(request);
        
        try {
            // 1. Create Company
            Company company = createCompanyFromRequest(request);
            Company savedCompany = companyRepository.save(company);
            
            // 2. Create Admin User
            User adminUser = createAdminUserFromRequest(request);
            User savedUser = userRepository.save(adminUser);
            
            // 3. Create Employee record linking user to company
            Employee employee = createEmployeeFromRequest(request, savedUser, savedCompany);
            Employee savedEmployee = employeeRepository.save(employee);
            
            // 4. Build response
            return new RegisterCompanyResponse(
                savedCompany.getId(),
                savedCompany.getName(),
                savedCompany.getCompanyCode(),
                savedCompany.getStatus(),
                savedUser.getId(),
                savedUser.getUserName(),
                savedUser.getEmail(),
                savedEmployee.getId(),
                "Company and admin user registered successfully"
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to register company: " + e.getMessage(), e);
        }
    }
    
    private void validateRegistrationRequest(RegisterCompanyRequest request) {
        // Check if company code already exists
        if (request.getCompanyCode() != null && !request.getCompanyCode().isEmpty()) {
            Optional<Company> existingCompany = companyRepository.findByCompanyCode(request.getCompanyCode());
            if (existingCompany.isPresent()) {
                throw new RuntimeException("Company with code '" + request.getCompanyCode() + "' already exists");
            }
        }
        
        // Check if username already exists
        Optional<User> existingUser = userRepository.findByUserName(request.getAdminUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with username '" + request.getAdminUsername() + "' already exists");
        }
        
        // Check if email already exists
        Optional<User> existingEmailUser = userRepository.findByEmail(request.getAdminEmail());
        if (existingEmailUser.isPresent()) {
            throw new RuntimeException("User with email '" + request.getAdminEmail() + "' already exists");
        }
    }
    
    private Company createCompanyFromRequest(RegisterCompanyRequest request) {
        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setCompanyCode(request.getCompanyCode());
        company.setEmail(request.getCompanyEmail());
        company.setPhone(request.getCompanyPhone());
        company.setWebsite(request.getCompanyWebsite());
        company.setAddressLine1(request.getAddressLine1());
        company.setAddressLine2(request.getAddressLine2());
        company.setCity(request.getCity());
        company.setState(request.getState());
        company.setCountry(request.getCountry());
        company.setPostalCode(request.getPostalCode());
        company.setTaxId(request.getTaxId());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setDescription(request.getDescription());
        company.setStatus("ACTIVE");
        company.setArchived(false);
        
        return company;
    }
    
    private User createAdminUserFromRequest(RegisterCompanyRequest request) {
        User user = new User();
        user.setUserName(request.getAdminUsername());
        user.setEmail(request.getAdminEmail());
        user.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        user.setEnabled(true);
        user.setArchived(false);
        
        // Get or create ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ADMIN");
                    newRole.setArchived(false);
                    return roleRepository.save(newRole);
                });
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);
        
        return user;
    }
    
    private Employee createEmployeeFromRequest(RegisterCompanyRequest request, User user, Company company) {
        Employee employee = new Employee();
        employee.setFirstName(request.getAdminFirstName());
        employee.setLastName(request.getAdminLastName());
        employee.setContactDetailsPhone(request.getAdminPhone());
        employee.setEmployeeType(1); // Admin employee type
        employee.setUser(user);
        employee.setCompany(company);
        employee.setArchived(false);
        
        // Generate employee number if not provided
        String employeeNumber = "EMP" + System.currentTimeMillis();
        employee.setEmployeeNumber(employeeNumber);
        
        return employee;
    }
}
