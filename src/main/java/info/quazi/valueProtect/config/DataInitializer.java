package info.quazi.valueProtect.config;

import info.quazi.valueProtect.entity.Role;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.RoleRepository;
import info.quazi.valueProtect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
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
        try {
            log.info("Starting data initialization...");
            
            Role adminRole = findOrCreateRole("ADMIN");
            Role userRole = findOrCreateRole("USER");

            Optional<User> existingAdmin = userRepository.findByUserNameWithRoles("admin");

            if (existingAdmin.isEmpty()) {
                log.info("Admin user not found. Creating new admin user...");
                User admin = new User();
                admin.setUserName("admin");
                admin.setPassword(passwordEncoder.encode("Heartcore123!"));
                admin.setEmail("admin@valueprotect.com");
                admin.setEnabled(true);
                admin.setArchived(false);
                
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                roles.add(userRole);
                admin.setRoles(roles);
                
                userRepository.save(admin);
                log.info("Admin user created successfully with username: admin");
            } else {
                log.info("Admin user already exists with username: admin");
                // Update password if needed
                User admin = existingAdmin.get();
                String newPasswordEncoded = passwordEncoder.encode("Heartcore123!");
                
                // Always update to ensure password is current
                admin.setPassword(newPasswordEncoded);
                userRepository.save(admin);
                log.info("Admin user password has been updated");
            }
            
            log.info("Data initialization completed");
        } catch (Exception e) {
            log.error("Error during data initialization", e);
            throw e;
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
