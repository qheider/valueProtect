package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.*;
import info.quazi.valueProtect.entity.AppraisalDocument.DocumentType;
import info.quazi.valueProtect.service.AppraisalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/appraisals")
@Tag(name = "Appraisal Management", description = "CRUD operations for property appraisals with company-level security")
@SecurityRequirement(name = "bearerAuth")
public class AppraisalController {

    private final AppraisalService appraisalService;

    public AppraisalController(AppraisalService appraisalService) {
        this.appraisalService = appraisalService;
    }

    @PostMapping
    @Operation(
        summary = "Create new appraisal",
        description = "Create a new property appraisal. The appraisal will be associated with the logged-in employee's company. Property can be existing or newly created."
    )
    @ApiResponse(responseCode = "201", description = "Appraisal created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<AppraisalDto> createAppraisal(
            @Valid @RequestBody CreateAppraisalRequest request) {
        
        AppraisalDto createdAppraisal = appraisalService.createAppraisal(request);
        return new ResponseEntity<>(createdAppraisal, HttpStatus.CREATED);
    }

    @GetMapping("/{appraisalId}")
    @Operation(
        summary = "Get appraisal by ID",
        description = "Retrieve a specific appraisal by its ID. Access is restricted to the company of the logged-in employee."
    )
    @ApiResponse(responseCode = "200", description = "Appraisal found")
    @ApiResponse(responseCode = "404", description = "Appraisal not found or access denied")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<AppraisalDto> getAppraisal(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId) {
        
        AppraisalDto appraisal = appraisalService.getAppraisal(appraisalId);
        return ResponseEntity.ok(appraisal);
    }

    @GetMapping
    @Operation(
        summary = "Get appraisals list",
        description = "Get list of appraisals scoped to the logged-in user. Admin users can see all company appraisals, regular users see only their own."
    )
    @ApiResponse(responseCode = "200", description = "Appraisals retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<List<AppraisalDto>> getAppraisals() {
        List<AppraisalDto> appraisals = appraisalService.getAppraisals();
        return ResponseEntity.ok(appraisals);
    }

    @PutMapping("/{appraisalId}")
    @Operation(
        summary = "Update appraisal",
        description = "Update an existing appraisal. Only the original appraiser or admin can modify appraisals."
    )
    @ApiResponse(responseCode = "200", description = "Appraisal updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Appraisal not found")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<AppraisalDto> updateAppraisal(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId,
            @Valid @RequestBody UpdateAppraisalRequest request) {
        
        AppraisalDto updatedAppraisal = appraisalService.updateAppraisal(appraisalId, request);
        return ResponseEntity.ok(updatedAppraisal);
    }

    @DeleteMapping("/{appraisalId}")
    @Operation(
        summary = "Delete appraisal",
        description = "Delete an appraisal and all associated documents. Only the original appraiser or admin can delete appraisals."
    )
    @ApiResponse(responseCode = "204", description = "Appraisal deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Appraisal not found")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<Void> deleteAppraisal(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId) {
        
        appraisalService.deleteAppraisal(appraisalId);
        return ResponseEntity.noContent().build();
    }

    // Document Management Endpoints

    @PostMapping("/{appraisalId}/documents")
    @Operation(
        summary = "Upload appraisal document",
        description = "Upload a supporting document for an appraisal. Supported formats: PDF, images, Word documents, Excel files."
    )
    @ApiResponse(responseCode = "201", description = "Document uploaded successfully")
    @ApiResponse(responseCode = "400", description = "Invalid file or request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<AppraisalDocumentDto> uploadDocument(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId,
            @RequestParam("file") @Parameter(description = "Document file to upload") MultipartFile file,
            @RequestParam("documentType") @Parameter(description = "Type of document") DocumentType documentType) {
        
        try {
            AppraisalDocumentDto document = appraisalService.uploadDocument(appraisalId, file, documentType);
            return new ResponseEntity<>(document, HttpStatus.CREATED);
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{appraisalId}/documents")
    @Operation(
        summary = "Get appraisal documents",
        description = "Retrieve all documents associated with an appraisal."
    )
    @ApiResponse(responseCode = "200", description = "Documents retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Appraisal not found or access denied")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<List<AppraisalDocumentDto>> getAppraisalDocuments(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId) {
        
        List<AppraisalDocumentDto> documents = appraisalService.getAppraisalDocuments(appraisalId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{appraisalId}/documents/download/{filename}")
    @Operation(
        summary = "Download appraisal document",
        description = "Download a specific document file associated with an appraisal."
    )
    @ApiResponse(responseCode = "200", description = "File downloaded successfully")
    @ApiResponse(responseCode = "404", description = "File not found or access denied")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId,
            @PathVariable @Parameter(description = "File name") String filename) {
        
        try {
            // Verify access to appraisal first
            appraisalService.getAppraisal(appraisalId);
            
            // Construct file path
            Path filePath = Paths.get("uploads/appraisal-documents", appraisalId, filename);
            @SuppressWarnings("null")
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/documents/{documentId}")
    @Operation(
        summary = "Delete appraisal document",
        description = "Delete a specific document from an appraisal. Only the original appraiser or admin can delete documents."
    )
    @ApiResponse(responseCode = "204", description = "Document deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Document not found")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable @Parameter(description = "Document ID") String documentId) {
        
        appraisalService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    // Exception Handlers

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse("INVALID_REQUEST", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
        ErrorResponse error = new ErrorResponse("ACCESS_DENIED", e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Schema(description = "Error response")
    public static class ErrorResponse {
        @Schema(description = "Error code")
        private String code;
        
        @Schema(description = "Error message")
        private String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}