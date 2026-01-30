package info.quazi.valueProtect.config;

import info.quazi.valueProtect.entity.Role;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.RoleRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        Role adminRole = findOrCreateRole("ADMIN");
        Role userRole = findOrCreateRole("USER");

        Optional<User> existingAdmin = userRepository.findByUserName("admin");

        if (existingAdmin.isEmpty()) {
            User admin = new User();
            admin.setUserName("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@valueprotect.com");
            admin.setEnabled(true);
            admin.setArchived(false);
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);
            
            userRepository.save(admin);
        }
    }

    private Role findOrCreateRole(String roleName) {
        Optional<Role> existing = roleRepository.findByName(roleName);

        if (existing.isPresent()) {
            return existing.get();
        }

        Role role = new Role();
        role.setName(roleName);
        role.setArchived(false);
        return roleRepository.save(role);
    }
}
