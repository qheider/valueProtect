package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company", description = "Company registration and management APIs")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @Operation(summary = "Register a new company", description = "Creates a new company registration")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Company createdCompany = companyService.create(company);
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update company", description = "Updates an existing company's information")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        Company updatedCompany = companyService.update(id, company);
        return ResponseEntity.ok(updatedCompany);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID", description = "Retrieves a company by its ID")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Company company = companyService.findById(id);
        return ResponseEntity.ok(company);
    }

    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieves all registered companies")
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyService.findAll();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get companies by status", description = "Retrieves companies filtered by status (ACTIVE, INACTIVE, SUSPENDED)")
    public ResponseEntity<List<Company>> getCompaniesByStatus(@PathVariable String status) {
        List<Company> companies = companyService.findByStatus(status);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/code/{companyCode}")
    @Operation(summary = "Get company by code", description = "Retrieves a company by its unique company code")
    public ResponseEntity<Company> getCompanyByCode(@PathVariable String companyCode) {
        Company company = companyService.findByCompanyCode(companyCode);
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete company (soft delete)", description = "Soft deletes a company by archiving it")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
