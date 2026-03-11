package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.*;
import info.quazi.valueProtect.entity.AppraisalDocument.DocumentType;
import info.quazi.valueProtect.exception.UnauthorizedAppraiserAssignmentException;
import info.quazi.valueProtect.service.AppraisalService;
import info.quazi.valueProtect.service.FileUploadService;
import info.quazi.valueProtect.service.PdfProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appraisals")
@Tag(name = "Appraisal Management", description = "CRUD operations for property appraisals with company-level security")
@SecurityRequirement(name = "bearerAuth")
public class AppraisalController {

    private static final Logger log = LoggerFactory.getLogger(AppraisalController.class);
    private final AppraisalService appraisalService;
    private final FileUploadService fileUploadService;
    private final PdfProcessingService pdfProcessingService;

    @Value("${app.file.base.url:http://localhost:8080}")
    private String baseUrl;

    public AppraisalController(AppraisalService appraisalService,
                               FileUploadService fileUploadService,
                               PdfProcessingService pdfProcessingService) {
        this.appraisalService = appraisalService;
        this.fileUploadService = fileUploadService;
        this.pdfProcessingService = pdfProcessingService;
    }

    @PostMapping
    @Operation(
        summary = "Create new appraisal",
        description = "Create a new property appraisal with role-based employee assignment. " +
                     "Admin: Can specify both appraiser and lender. " +
                     "Lender: Can optionally specify appraiser, otherwise system assigns random available appraiser. " +
                     "Appraiser: Automatically assigned as appraiser, can optionally specify lender. " +
                     "Property can be existing or newly created."
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

    @PostMapping("/{appraisalId}/assign-appraiser")
    @Operation(
        summary = "Assign appraiser to appraisal",
        description = "Assign an appraiser to an appraisal request. The appraiser must have APPRAISER role, " +
                     "belong to an APPRAISAL company, and that company must be in the lender's preferred list."
    )
    @ApiResponse(responseCode = "200", description = "Appraiser assigned successfully")
    @ApiResponse(responseCode = "403", description = "Appraiser assignment not authorized")
    @ApiResponse(responseCode = "404", description = "Appraisal or user not found")
    @PreAuthorize("hasRole('ADMIN') or hasRole('LENDER')")
    public ResponseEntity<AppraisalDto> assignAppraiser(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId,
            @RequestParam("userId") @Parameter(description = "Appraiser user ID") Long userId) {
        AppraisalDto updated = appraisalService.assignAppraiser(appraisalId, userId);
        return ResponseEntity.ok(updated);
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
            log.info("Received document upload request for appraisal: {}, file: {}, type: {}", 
                     appraisalId, file.getOriginalFilename(), documentType);
            
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }
            
            AppraisalDocumentDto document = appraisalService.uploadDocument(appraisalId, file, documentType);
            log.info("Document upload successful for appraisal: {}, document ID: {}", 
                     appraisalId, document.getDocumentId());
            
            // Trigger PDF processing for appraisal reports
            if (documentType == DocumentType.APPRAISAL_REPORT && document.getFileUrl() != null) {
                try {
                    String fullPdfUrl = buildFullDocumentUrl(appraisalId, document.getFileName());
                    log.info("Triggering PDF processing for appraisal report: {}, URL: {}", 
                            appraisalId, fullPdfUrl);
                    pdfProcessingService.processPdfDocument(fullPdfUrl, appraisalId);
                } catch (Exception e) {
                    log.error("Failed to trigger PDF processing for appraisal {}: {}", 
                            appraisalId, e.getMessage(), e);
                    // Don't fail the upload if PDF processing fails
                }
            }
            
            return new ResponseEntity<>(document, HttpStatus.CREATED);
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Client error during document upload: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            log.error("File upload failed for appraisal {}: {}", appraisalId, e.getMessage(), e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during document upload for appraisal {}: {}", appraisalId, e.getMessage(), e);
            throw new RuntimeException("Document upload failed due to server error: " + e.getMessage(), e);
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

            // Resolve document from DB for this appraisal, then use stored file URL to locate actual file
            List<AppraisalDocumentDto> documents = appraisalService.getAppraisalDocuments(appraisalId);
            Optional<AppraisalDocumentDto> matchedDocument = documents.stream()
                    .filter(doc -> isDocumentMatch(doc, filename))
                    .findFirst();

            if (matchedDocument.isEmpty() || matchedDocument.get().getFileUrl() == null) {
                return ResponseEntity.notFound().build();
            }

            AppraisalDocumentDto document = matchedDocument.get();
            Path filePath = fileUploadService.resolveFilePath(document.getFileUrl());
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
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeDownloadName(document, filename) + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/documents/{documentId}/download")
    @Operation(
        summary = "Download appraisal document by document ID",
        description = "Download a document using its document ID. This avoids filename/path mismatch issues."
    )
    @ApiResponse(responseCode = "200", description = "File downloaded successfully")
    @ApiResponse(responseCode = "404", description = "File not found or access denied")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER') or hasRole('APPRAISER')")
    public ResponseEntity<Resource> downloadDocumentById(
            @PathVariable @Parameter(description = "Document ID") String documentId) {

        log.info("Document download requested - Document ID: {}", documentId);
        
        try {
            AppraisalDocumentDto document = appraisalService.getDocumentForDownload(documentId);
            log.debug("Document found: {}, FileURL: {}", document.getFileName(), document.getFileUrl());
            
            if (document.getFileUrl() == null || document.getFileUrl().isBlank()) {
                log.warn("Document {} has no file URL", documentId);
                return ResponseEntity.notFound().build();
            }

            Path filePath = fileUploadService.resolveFilePath(document.getFileUrl());
            log.debug("Resolved file path: {}", filePath.toAbsolutePath());
            
            @SuppressWarnings("null")
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not found or not readable: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String fallbackName = extractFilenameFromUrl(document.getFileUrl());
            log.info("Document download successful - File: {}, Size: {} bytes", 
                    fallbackName, resource.contentLength());
                    
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + safeDownloadName(document, fallbackName) + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("IO error during document download - Document ID: {}, Error: {}", 
                      documentId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error during document download - Document ID: {}, Error: {}", 
                      documentId, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isDocumentMatch(AppraisalDocumentDto document, String requestedFilename) {
        if (document == null) {
            return false;
        }

        String normalizedRequested = normalizeFilename(requestedFilename);
        String normalizedOriginal = normalizeFilename(document.getFileName());
        String normalizedStored = normalizeFilename(extractFilenameFromUrl(document.getFileUrl()));

        return normalizedRequested.equals(normalizedOriginal) || normalizedRequested.equals(normalizedStored);
    }

    /**
     * Build full URL for accessing a document by appraisal ID and filename
     * Used for external API calls that need to download the document
     */
    private String buildFullDocumentUrl(String appraisalId, String filename) {
        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return cleanBaseUrl + "/api/appraisals/" + appraisalId + "/documents/download/" + filename;
    }

    private String safeDownloadName(AppraisalDocumentDto document, String fallbackName) {
        if (document != null && document.getFileName() != null && !document.getFileName().isBlank()) {
            return document.getFileName();
        }
        return fallbackName;
    }

    private String extractFilenameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }

        String normalized = fileUrl;
        int queryStart = normalized.indexOf('?');
        if (queryStart >= 0) {
            normalized = normalized.substring(0, queryStart);
        }

        String[] parts = normalized.split("/");
        if (parts.length == 0) {
            return null;
        }
        return parts[parts.length - 1];
    }

    private String normalizeFilename(String filename) {
        if (filename == null) {
            return "";
        }

        String withoutQuery = filename.split("\\?")[0];
        String[] segments = withoutQuery.split("/");
        return Arrays.stream(segments)
                .reduce((first, second) -> second)
                .orElse(withoutQuery);
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

    @ExceptionHandler(UnauthorizedAppraiserAssignmentException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAppraiserAssignment(UnauthorizedAppraiserAssignmentException e) {
        ErrorResponse error = new ErrorResponse("APPRAISER_ASSIGNMENT_NOT_ALLOWED", e.getMessage());
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