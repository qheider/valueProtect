package info.quazi.valueProtect.service;

import info.quazi.valueProtect.dto.CreateEmployeeRequest;
import info.quazi.valueProtect.dto.EmployeeDto;
import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.CompanyRepository;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CompanyRepository companyRepository;

    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository, UserService userService, CompanyRepository companyRepository) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        Employee employee = request.toEntity();
        
        User user = null;
        
        // If userId is provided, use existing user
        if (request.getUserId() != null) {
            @SuppressWarnings("null")
            Long userId = request.getUserId();
            if (userId != null) {
                user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            }
        } 
        // Otherwise, create a new user if username and password are provided
        else if (request.getUserName() != null && request.getPassword() != null) {
            // Generate email from employee info if not provided
            String email = request.getEmail();
            if (email == null && request.getFirstName() != null && request.getLastName() != null) {
                email = (request.getFirstName() + "." + request.getLastName() + "@valueprotect.com").toLowerCase();
            }
            
            // Default role if not specified
            String roleName = request.getRoleName() != null ? request.getRoleName() : "EMPLOYEE";
            
            // Create the user account
            user = userService.createUser(request.getUserName(), email, request.getPassword(), roleName);
        }
        
        if (user != null) {
            employee.setUser(user);
        }
        
        // Set company if companyId is provided (optional)
        if (request.getCompanyId() != null) {
            Long companyId = request.getCompanyId();
            if (companyId != null) {
                Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
                employee.setCompany(company);
            }
        }
        
        if (employee.getArchived() == null) {
            employee.setArchived(false);
        }
        @SuppressWarnings("null")
        Employee saved = employeeRepository.save(employee);
        return EmployeeDto.fromEntity(saved);
    }

    @Transactional
    public EmployeeDto createEmployeeForAdminCompany(CreateEmployeeRequest request) {
        // Get authenticated user
        User authenticatedUser = getAuthenticatedUser();
        
        // Check if user has admin role
        boolean isAdmin = authenticatedUser.getRoles().stream()
            .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role.getName()) || "ADMIN".equalsIgnoreCase(role.getName()));
        
        if (!isAdmin) {
            throw new RuntimeException("Access denied. Only admin users can create employees.");
        }
        
        // Get the employee record of the authenticated admin user
        Employee authenticatedEmployee = employeeRepository.findByUserAndArchivedFalse(authenticatedUser)
            .orElseThrow(() -> new RuntimeException("No employee record found for the authenticated user"));
        
        // Check if the admin user belongs to a company
        if (authenticatedEmployee.getCompany() == null) {
            throw new RuntimeException("Your employee record is not associated with any company. Cannot create employees.");
        }
        
        Company adminCompany = authenticatedEmployee.getCompany();
        
        // Override any companyId in the request - admin can only create employees for their own company
        request.setCompanyId(adminCompany.getId());
        
        Employee employee = request.toEntity();
        
        User user = null;
        
        // If userId is provided, use existing user
        if (request.getUserId() != null) {
            Long userId = request.getUserId();
            if (userId != null) {
                user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            }
        } 
        // Otherwise, create a new user if username and password are provided
        else if (request.getUserName() != null && request.getPassword() != null) {
            // Generate email from employee info if not provided
            String email = request.getEmail();
            if (email == null && request.getFirstName() != null && request.getLastName() != null) {
                email = (request.getFirstName() + "." + request.getLastName() + "@" + 
                        adminCompany.getName().toLowerCase().replaceAll("\\s+", "") + ".com").toLowerCase();
            }
            
            // Default role if not specified (employees should not have admin role by default)
            String roleName = request.getRoleName();
            if (roleName == null || "ROLE_ADMIN".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName)) {
                roleName = "ROLE_EMPLOYEE";
            }
            
            // Create the user account
            user = userService.createUser(request.getUserName(), email, request.getPassword(), roleName);
        }
        
        if (user != null) {
            employee.setUser(user);
        }
        
        // Set the admin's company for the new employee
        employee.setCompany(adminCompany);
        
        // Set the admin user as the creator
        employee.setCreatedByUserId(authenticatedUser.getId());
        
        if (employee.getArchived() == null) {
            employee.setArchived(false);
        }
        
        Employee saved = employeeRepository.save(employee);
        return EmployeeDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getActiveEmployees() {
        return employeeRepository.findByArchivedFalseOrArchivedIsNull().stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByCompany(Long companyId) {
        // Get authenticated user
        User authenticatedUser = getAuthenticatedUser();
        
        // Check if user has admin role
        boolean isAdmin = authenticatedUser.getRoles().stream()
            .anyMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role.getName()) || "ADMIN".equalsIgnoreCase(role.getName()));
        
        if (!isAdmin) {
            throw new RuntimeException("Access denied. Only admin users can view employees by company.");
        }
        
        // Get the employee record of the authenticated user
        Employee authenticatedEmployee = employeeRepository.findByUserAndArchivedFalse(authenticatedUser)
            .orElseThrow(() -> new RuntimeException("No employee record found for the authenticated user"));
        
        // Check if the authenticated user belongs to the same company
        if (authenticatedEmployee.getCompany() == null) {
            throw new RuntimeException("Your employee record is not associated with any company");
        }
        
        if (!authenticatedEmployee.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Access denied. You can only view employees from your own company.");
        }
        
        // Get the company
        if (companyId == null) {
            throw new RuntimeException("Company ID cannot be null");
        }
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
        
        // Return employees from the same company
        return employeeRepository.findByCompanyAndArchivedFalse(company).stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userService.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        }
        
        throw new RuntimeException("Invalid authentication principal");
    }
}
