package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.RegisterCompanyRequest;
import info.quazi.valueProtect.dto.RegisterCompanyResponse;
import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.repository.CompanyRepository;
import info.quazi.valueProtect.repository.UserRepository;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RegisterCompanyIntegrationTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        // Clear any existing test data
        employeeRepository.deleteAll();
        userRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    public void testRegisterCompanyWithAdminUser_Success() {
        // Prepare request
        RegisterCompanyRequest request = new RegisterCompanyRequest();
        request.setCompanyName("Test Corporation");
        request.setCompanyCode("TEST001");
        request.setCompanyEmail("info@test.com");
        request.setCompanyPhone("+1-555-123-4567");
        request.setAdminUsername("test.admin");
        request.setAdminEmail("admin@test.com");
        request.setAdminPassword("TestPass123!");
        request.setAdminFirstName("Test");
        request.setAdminLastName("Admin");

        // Execute
        RegisterCompanyResponse response = companyService.registerCompanyWithAdminUser(request);

        // Assertions
        assertNotNull(response);
        assertNotNull(response.getCompanyId());
        assertNotNull(response.getAdminUserId());
        assertNotNull(response.getEmployeeId());
        assertEquals("Test Corporation", response.getCompanyName());
        assertEquals("TEST001", response.getCompanyCode());
        assertEquals("ACTIVE", response.getCompanyStatus());
        assertEquals("test.admin", response.getAdminUsername());
        assertEquals("admin@test.com", response.getAdminEmail());
        assertEquals("Company and admin user registered successfully", response.getMessage());

        // Verify data in database
        @SuppressWarnings("null")
        Optional<Company> savedCompany = companyRepository.findById(response.getCompanyId());
        assertTrue(savedCompany.isPresent());
        assertEquals("Test Corporation", savedCompany.get().getName());
        assertEquals("ACTIVE", savedCompany.get().getStatus());

        @SuppressWarnings("null")
        Optional<User> savedUser = userRepository.findById(response.getAdminUserId());
        assertTrue(savedUser.isPresent());
        assertEquals("test.admin", savedUser.get().getUserName());
        assertEquals("admin@test.com", savedUser.get().getEmail());
        assertTrue(savedUser.get().getEnabled());
        assertFalse(savedUser.get().getRoles().isEmpty());
        assertEquals("ADMIN", savedUser.get().getRoles().iterator().next().getName());

        @SuppressWarnings("null")
        Optional<Employee> savedEmployee = employeeRepository.findById(response.getEmployeeId());
        assertTrue(savedEmployee.isPresent());
        assertEquals("Test", savedEmployee.get().getFirstName());
        assertEquals("Admin", savedEmployee.get().getLastName());
        assertEquals(savedUser.get(), savedEmployee.get().getUser());
        assertEquals(savedCompany.get(), savedEmployee.get().getCompany());
    }

    @Test
    public void testRegisterCompanyWithAdminUser_DuplicateUsername() {
        // First registration
        RegisterCompanyRequest request1 = new RegisterCompanyRequest();
        request1.setCompanyName("First Company");
        request1.setCompanyCode("FIRST001");
        request1.setAdminUsername("duplicate.user");
        request1.setAdminEmail("first@company.com");
        request1.setAdminPassword("Password123!");

        companyService.registerCompanyWithAdminUser(request1);

        // Second registration with same username
        RegisterCompanyRequest request2 = new RegisterCompanyRequest();
        request2.setCompanyName("Second Company");
        request2.setCompanyCode("SECOND001");
        request2.setAdminUsername("duplicate.user");  // Same username
        request2.setAdminEmail("second@company.com");
        request2.setAdminPassword("Password123!");

        // Should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            companyService.registerCompanyWithAdminUser(request2);
        });

        assertTrue(exception.getMessage().contains("User with username 'duplicate.user' already exists"));
    }

    @Test
    public void testRegisterCompanyWithAdminUser_DuplicateCompanyCode() {
        // First registration
        RegisterCompanyRequest request1 = new RegisterCompanyRequest();
        request1.setCompanyName("First Company");
        request1.setCompanyCode("DUPLICATE");
        request1.setAdminUsername("user1");
        request1.setAdminEmail("user1@company.com");
        request1.setAdminPassword("Password123!");

        companyService.registerCompanyWithAdminUser(request1);

        // Second registration with same company code
        RegisterCompanyRequest request2 = new RegisterCompanyRequest();
        request2.setCompanyName("Second Company");
        request2.setCompanyCode("DUPLICATE");  // Same company code
        request2.setAdminUsername("user2");
        request2.setAdminEmail("user2@company.com");
        request2.setAdminPassword("Password123!");

        // Should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            companyService.registerCompanyWithAdminUser(request2);
        });

        assertTrue(exception.getMessage().contains("Company with code 'DUPLICATE' already exists"));
    }
}