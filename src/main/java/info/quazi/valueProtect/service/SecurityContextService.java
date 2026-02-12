package info.quazi.valueProtect.service;

import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityContextService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public SecurityContextService(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("No authenticated user found");
        }

        String username = authentication.getName();
        Optional<User> user = userRepository.findByUserName(username);
        return user.orElseThrow(() -> new SecurityException("User not found: " + username));
    }

    public Employee getCurrentEmployee() {
        User currentUser = getCurrentUser();
        Optional<Employee> employee = employeeRepository.findByUserId(currentUser.getId());
        return employee.orElseThrow(() -> new SecurityException("Employee record not found for user: " + currentUser.getUserName()));
    }

    public Long getCurrentCompanyId() {
        Employee employee = getCurrentEmployee();
        if (employee.getCompany() == null) {
            throw new SecurityException("Employee is not associated with any company");
        }
        return employee.getCompany().getId();
    }

    public boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUser();
        return currentUser.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
    }

    public boolean hasAccessToCompanyData(Long companyId) {
        Long currentCompanyId = getCurrentCompanyId();
        return currentCompanyId.equals(companyId);
    }

    public String getCurrentEmployeeId() {
        Employee employee = getCurrentEmployee();
        return String.valueOf(employee.getId());
    }
}