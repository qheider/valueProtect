package info.quazi.valueProtect.service;

import info.quazi.valueProtect.entity.Role;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.RoleRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(String userName, String email, String password, String roleName) {
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

        // Set default role
        Role role = roleRepository.findByName(roleName)
            .orElseGet(() -> {
                Role newRole = new Role();
                newRole.setName(roleName);
                return roleRepository.save(newRole);
            });

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
}
