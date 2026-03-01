package info.quazi.valueProtect.service;

import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.Role;
import info.quazi.valueProtect.entity.RoleName;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.repository.RoleRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleAssignmentValidator roleAssignmentValidator;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder,
                       RoleAssignmentValidator roleAssignmentValidator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleAssignmentValidator = roleAssignmentValidator;
    }

    @Transactional
    public User createUser(String userName, String email, String password, String roleName) {
        return createUser(userName, email, password, roleName, null);
    }

    @Transactional
    public User createUser(String userName, String email, String password, String roleName, Company company) {
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUserName(userName);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists with username: " + userName);
        }

        if (email != null) {
            Optional<User> existingEmailUser = userRepository.findByEmail(email);
            if (existingEmailUser.isPresent()) {
                throw new RuntimeException("User already exists with email: " + email);
            }
        }

        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);

        RoleName normalizedRoleName = toRoleName(roleName);

        if (company != null) {
            roleAssignmentValidator.validate(company.getCompanyType(), normalizedRoleName);
        }

        // Set default role
        Role role = getOrCreateRole(normalizedRoleName);

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updatePassword(String userName, String newPassword) {
        User user = userRepository.findByUserName(userName)
            .orElseThrow(() -> new RuntimeException("User not found: " + userName));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void assignRoleToUser(Long userId, Role role) {
        if (role == null || role.getName() == null || role.getName().isBlank()) {
            throw new RuntimeException("Role is required");
        }

        RoleName roleName = toRoleName(role.getName());
        assignRoleToUser(userId, roleName);
    }

    @Transactional
    public void assignRoleToUser(Long userId, RoleName roleName) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found for user id: " + userId));

        if (employee.getCompany() == null) {
            throw new RuntimeException("User is not associated with a company");
        }

        roleAssignmentValidator.validate(employee.getCompany().getCompanyType(), roleName);

        Role roleEntity = getOrCreateRole(roleName);
        user.getRoles().add(roleEntity);
        userRepository.save(user);
    }

    private Role getOrCreateRole(RoleName roleName) {
        String persistedRoleName = roleName.name();
        return roleRepository.findByName(persistedRoleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(persistedRoleName);
                    return roleRepository.save(newRole);
                });
    }

    public RoleName toRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new RuntimeException("Role name is required");
        }
        String normalized = roleName.replace("ROLE_", "").toUpperCase(Locale.ROOT);
        return RoleName.valueOf(normalized);
    }
}
