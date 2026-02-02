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
            @SuppressWarnings("null")
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
            String roleName = request.getRoleName() != null ? request.getRoleName() : "EMPLOYEE";
            
            // Create the user account
            user = userService.createUser(request.getUserName(), email, request.getPassword(), roleName);
        }
        
        if (user != null) {
            employee.setUser(user);
        }
        
        if (employee.getArchived() == null) {
            employee.setArchived(false);
        }
        @SuppressWarnings("null")
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
    public EmployeeDto getEmployeeById(Long id) {
        @SuppressWarnings("null")
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return EmployeeDto.fromEntity(employee);
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, CreateEmployeeRequest request) {
        @SuppressWarnings("null")
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Update basic employee information
        if (request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }
        if (request.getEmployeeNumber() != null) {
            employee.setEmployeeNumber(request.getEmployeeNumber());
        }
        if (request.getEmployeeType() != null) {
            employee.setEmployeeType(request.getEmployeeType());
        }
        if (request.getContactDetailsCity() != null) {
            employee.setContactDetailsCity(request.getContactDetailsCity());
        }
        if (request.getContactDetailsPhone() != null) {
            employee.setContactDetailsPhone(request.getContactDetailsPhone());
        }
        if (request.getContactDetailsSecondaryPhone() != null) {
            employee.setContactDetailsSecondaryPhone(request.getContactDetailsSecondaryPhone());
        }
        
        // Update user association if userId is provided
        if (request.getUserId() != null) {
            @SuppressWarnings("null")
            Long requestUserId = request.getUserId();
            User user = userRepository.findById(requestUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + requestUserId));
            employee.setUser(user);
        }
        
        @SuppressWarnings("null")
        Employee updated = employeeRepository.save(employee);
        return EmployeeDto.fromEntity(updated);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        @SuppressWarnings("null")
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Soft delete by setting archived to true
        employee.setArchived(true);
        employeeRepository.save(employee);
    }

    @Transactional
    public void hardDeleteEmployee(Long id) {
        @SuppressWarnings("null")
        boolean exists = employeeRepository.existsById(id);
        if (!exists) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        @SuppressWarnings("null")
        Long nonNullId = id;
        employeeRepository.deleteById(nonNullId);
    }
}
