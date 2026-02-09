package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.CreateEmployeeRequest;
import info.quazi.valueProtect.dto.EmployeeDto;
import info.quazi.valueProtect.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee", description = "Employee management APIs")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') || hasRole('admin')")
    @Operation(summary = "Get all active employees", description = "Retrieves a list of all non-archived employees")
    public ResponseEntity<List<EmployeeDto>> getActiveEmployees() {
        List<EmployeeDto> employees = employeeService.getActiveEmployees();
        return ResponseEntity.ok(employees);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') || hasRole('admin')")
    @Operation(summary = "Create a new employee", description = "Creates a new employee record for the admin's own company. " +
                     "**Security Rules:** " +
                     "1. Only admin users can create employees. " +
                     "2. Admin can only create employees for their own company. " +
                     "3. If user's employee record has no company, creation is denied. " +
                     "4. The new employee will automatically be assigned to the admin's company.")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeDto created = employeeService.createEmployeeForAdminCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('ADMIN') || hasRole('admin')")
    @Operation(
        summary = "Get employees by company", 
        description = "Retrieves all employees from a specific company. " +
                     "**Security Rules:** " +
                     "1. Only admin users can access this endpoint. " +
                     "2. Admin can only view employees from their own company. " +
                     "3. If user's employee record has no company, access is denied. " +
                     "4. If user tries to access another company's employees, access is denied."
    )
    public ResponseEntity<List<EmployeeDto>> getEmployeesByCompany(@PathVariable Long companyId) {
        List<EmployeeDto> employees = employeeService.getEmployeesByCompany(companyId);
        return ResponseEntity.ok(employees);
    }
}
