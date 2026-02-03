package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.CreatePassProtectRequest;
import info.quazi.valueProtect.dto.PassProtectDto;
import info.quazi.valueProtect.dto.UpdatePassProtectRequest;
import info.quazi.valueProtect.service.PassProtectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passprotect")
@Tag(name = "PassProtect", description = "PassProtect management APIs")
public class PassProtectController {

    private final PassProtectService passProtectService;

    public PassProtectController(PassProtectService passProtectService) {
        this.passProtectService = passProtectService;
    }

    @PostMapping
    @Operation(summary = "Create a new PassProtect", description = "Creates a new PassProtect record for the authenticated user")
    public ResponseEntity<PassProtectDto> create(@RequestBody CreatePassProtectRequest request) {
        PassProtectDto created = passProtectService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a PassProtect", description = "Updates an existing PassProtect record owned by the authenticated user")
    public ResponseEntity<PassProtectDto> update(
            @PathVariable Long id,
            @RequestBody UpdatePassProtectRequest request) {
        PassProtectDto updated = passProtectService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get PassProtect by ID", description = "Retrieves a PassProtect record by ID owned by the authenticated user")
    public ResponseEntity<PassProtectDto> getById(@PathVariable Long id) {
        PassProtectDto passProtect = passProtectService.getById(id);
        return ResponseEntity.ok(passProtect);
    }

    @GetMapping("/search")
    @Operation(summary = "Search PassProtect by company name", description = "Searches PassProtect records by partial company name for the authenticated user")
    public ResponseEntity<List<PassProtectDto>> searchByCompanyName(@RequestParam String companyName) {
        List<PassProtectDto> results = passProtectService.searchByCompanyName(companyName);
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a PassProtect (soft delete)", description = "Soft deletes a PassProtect record owned by the authenticated user")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passProtectService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
