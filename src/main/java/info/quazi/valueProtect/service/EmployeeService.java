package info.quazi.valueProtect.service;

import info.quazi.valueProtect.dto.CreateEmployeeRequest;
import info.quazi.valueProtect.dto.EmployeeDto;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository, UserService userService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional
    public EmployeeDto createEmployee(CreateEmployeeRequest request) {
        Employee employee = request.toEntity();
        
        User user = null;
        
        // If userId is provided, use existing user
        if (request.getUserId() != null) {
            Long userId = request.getUserId();
            user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        } 
        // Otherwise, create a new user if username and password are provided
        else if (request.getUserName() != null && request.getPassword() != null) {
            // Generate email from employee info if not provided
            String email = request.getEmail();
            if (email == null && request.getFirstName() != null && request.getLastName() != null) {
                email = (request.getFirstName() + "." + request.getLastName() + "@valueprotect.com").toLowerCase();
            }
            
            // Default role if not specified
            String roleName = request.getRoleName() != null ? request.getRoleName() : "ROLE_EMPLOYEE";
            
            // Create the user account
            user = userService.createUser(request.getUserName(), email, request.getPassword(), roleName);
        }
        
        if (user != null) {
            employee.setUser(user);
        }
        
        employee.setArchived(false);
        Employee saved = employeeRepository.save(employee);
        return EmployeeDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDto> getActiveEmployees() {
        return employeeRepository.findByArchivedFalseOrArchivedIsNull().stream()
            .map(EmployeeDto::fromEntity)
            .collect(Collectors.toList());
    }
}
