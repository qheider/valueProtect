package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.CreateEmployeeRequest;
import info.quazi.valueProtect.dto.EmployeeDto;
import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.Role;
import info.quazi.valueProtect.repository.CompanyRepository;
import info.quazi.valueProtect.repository.UserRepository;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.repository.RoleRepository;
import info.quazi.valueProtect.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({EmployeeService.class, BCryptPasswordEncoder.class})
public class EmployeeCreationIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Company testCompany;
    private User adminUser;
    private Employee adminEmployee;
    private Role adminRole;
    private Role employeeRole;

    @BeforeEach
    void setUp() {
        // Create roles first
        adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        @SuppressWarnings("null")
        Role savedAdminRole = roleRepository.save(adminRole);
        adminRole = savedAdminRole;

        employeeRole = new Role();
        employeeRole.setName("ROLE_EMPLOYEE");
        @SuppressWarnings("null")
        Role savedEmployeeRole = roleRepository.save(employeeRole);
        employeeRole = savedEmployeeRole;

        // Create test company
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setCompanyCode("TC001");
        testCompany.setEmail("admin@testcompany.com");
        testCompany.setArchived(false);
        testCompany = companyRepository.save(testCompany);

        // Create admin user
        adminUser = new User();
        adminUser.setUserName("adminuser");
        adminUser.setEmail("admin@testcompany.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setEnabled(true);
        adminUser.setArchived(false);
        
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
        adminUser = userRepository.save(adminUser);

        // Create admin employee
        adminEmployee = new Employee();
        adminEmployee.setFirstName("Admin");
        adminEmployee.setLastName("User");
        adminEmployee.setEmployeeNumber("EMP001");
        adminEmployee.setUser(adminUser);
        adminEmployee.setCompany(testCompany);
        adminEmployee.setArchived(false);
        adminEmployee = employeeRepository.save(adminEmployee);
    }

    private void authenticateAdmin() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("adminuser", "admin123", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void testCreateEmployeeByAdmin_Success() {
        // Given
        authenticateAdmin();
        
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmployeeNumber("EMP002");
        request.setContactDetailsPhone("1234567890");
        request.setContactDetailsCity("New York");
        request.setUserName("johndoe");
        request.setPassword("password123");
        request.setEmployeeType(1);

        // When
        EmployeeDto result = employeeService.createEmployeeForAdminCompany(request);

        // Then
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("EMP002", result.getEmployeeNumber());
        assertEquals(testCompany.getId(), result.getCompanyId());
        assertEquals(testCompany.getName(), result.getCompanyName());
        assertNotNull(result.getUserName());
        assertEquals("johndoe", result.getUserName());

        // Verify in database
        @SuppressWarnings("null")
        Employee savedEmployee = employeeRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedEmployee);
        assertEquals(testCompany.getId(), savedEmployee.getCompany().getId());
        assertEquals(adminUser.getId(), savedEmployee.getCreatedByUserId());
        
        // Verify user was created with correct role
        User createdUser = savedEmployee.getUser();
        assertNotNull(createdUser);
        assertTrue(createdUser.getRoles().stream()
            .anyMatch(role -> "ROLE_EMPLOYEE".equals(role.getName())));
    }

    @Test
    public void testCreateEmployeeByNonAdmin_AccessDenied() {
        // Given - authenticate as regular user (not admin)
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken("regularuser", "password123", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setUserName("janesmith");
        request.setPassword("password123");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            employeeService.createEmployeeForAdminCompany(request));
        assertEquals("Access denied. Only admin users can create employees.", exception.getMessage());
    }
}