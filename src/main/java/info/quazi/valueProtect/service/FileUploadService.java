package info.quazi.valueProtect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${app.file.upload.directory:uploads/appraisal-documents}")
    private String uploadDirectory;

    @Value("${app.file.base.url:http://localhost:8080}")
    private String baseUrl;

        private static final Pattern DOWNLOAD_URL_PATTERN =
            Pattern.compile(".*/appraisals/([^/]+)/documents/download/([^/]+)$");

    public String uploadFile(MultipartFile file, String appraisalId, String documentType) throws IOException {
        log.info("Starting file upload for appraisal: {}, file: {}, type: {}", 
                appraisalId, file.getOriginalFilename(), documentType);
        
        // Validate file first
        validateFile(file);
        log.debug("File validation passed");
        
        // Create directory structure with proper error handling
        Path uploadPath;
        try {
            uploadPath = createDirectoryStructure(appraisalId);
            log.info("Upload directory created/verified: {}", uploadPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create directory structure for appraisal {}: {}", appraisalId, e.getMessage());
            throw new IOException("Cannot create upload directory: " + e.getMessage(), e);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(documentType, fileExtension);
        log.debug("Generated unique filename: {}", uniqueFilename);
        
        // Save file with better error handling
        Path filePath = uploadPath.resolve(uniqueFilename);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved successfully to: {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save file for appraisal {}: {}", appraisalId, e.getMessage());
            throw new IOException("Cannot save file: " + e.getMessage(), e);
        }
        
        // Verify file was actually written
        if (!Files.exists(filePath) || Files.size(filePath) == 0) {
            log.error("File verification failed - file does not exist or is empty: {}", filePath);
            throw new IOException("File upload verification failed");
        }
        
        // Return file URL
        String fileUrl = generateFileUrl(appraisalId, uniqueFilename);
        log.debug("Generated file URL: {}", fileUrl);
        return fileUrl;
    }

    public void deleteFile(String fileUrl) {
        try {
            Path filePath = getFilePathFromUrl(fileUrl);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            // Log error but don't throw exception
            log.warn("Error deleting file {}: {}", fileUrl, e.getMessage());
        }
    }

    public boolean fileExists(String fileUrl) {
        try {
            Path filePath = getFilePathFromUrl(fileUrl);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Check file size (10MB limit)
        long maxSize = 10 * 1024 * 1024; // 10MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }
        
        // Check allowed file types
        String contentType = file.getContentType();
        if (!isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }
    }

    private boolean isAllowedContentType(String contentType) {
        return contentType != null && (
            contentType.startsWith("image/") ||
            contentType.equals("application/pdf") ||
            contentType.equals("application/msword") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            contentType.equals("application/vnd.ms-excel") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
    }

    private Path createDirectoryStructure(String appraisalId) throws IOException {
        // Create directory: uploads/appraisal-documents/{appraisalId}/
        Path basePath = Paths.get(uploadDirectory);
        
        // Ensure base upload directory exists
        if (!Files.exists(basePath)) {
            try {
                Files.createDirectories(basePath);
                log.info("Created base upload directory: {}", basePath.toAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to create base upload directory {}: {}", basePath, e.getMessage());
                throw new IOException("Cannot create base upload directory: " + basePath, e);
            }
        }
        
        Path appraisalPath = basePath.resolve(appraisalId);
        
        if (!Files.exists(appraisalPath)) {
            try {
                Files.createDirectories(appraisalPath);
                log.info("Created appraisal directory: {}", appraisalPath.toAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to create appraisal directory {}: {}", appraisalPath, e.getMessage());
                throw new IOException("Cannot create appraisal directory: " + appraisalPath, e);
            }
        }
        
        // Verify directory is writable
        if (!Files.isWritable(appraisalPath)) {
            log.error("Appraisal directory is not writable: {}", appraisalPath);
            throw new IOException("Upload directory is not writable: " + appraisalPath);
        }
        
        return appraisalPath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateUniqueFilename(String documentType, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s%s", 
            documentType.toLowerCase().replace(" ", "_"), 
            timestamp, 
            uuid, 
            extension);
    }

    private String generateFileUrl(String appraisalId, String filename) {
        return String.format("%s/api/appraisals/%s/documents/download/%s", 
            baseUrl, appraisalId, filename);
    }

    private Path getFilePathFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("Invalid file URL format");
        }

        String normalized = fileUrl.trim().split("\\?")[0].replace('\\', '/');

        // Primary format: .../appraisals/{appraisalId}/documents/download/{filename}
        Matcher downloadMatcher = DOWNLOAD_URL_PATTERN.matcher(normalized);
        if (downloadMatcher.matches()) {
            String appraisalId = downloadMatcher.group(1);
            String filename = downloadMatcher.group(2);
            return Paths.get(uploadDirectory, appraisalId, filename);
        }

        // Backward-compatible format: uploads/appraisal-documents/{appraisalId}/{filename}
        String marker = "uploads/appraisal-documents/";
        int markerIndex = normalized.indexOf(marker);
        if (markerIndex >= 0) {
            String relativePath = normalized.substring(markerIndex + marker.length());
            String[] segments = relativePath.split("/");
            if (segments.length >= 2) {
                String appraisalId = segments[0];
                String filename = segments[segments.length - 1];
                return Paths.get(uploadDirectory, appraisalId, filename);
            }
        }

        throw new IllegalArgumentException("Invalid file URL format");
    }

    public long getFileSize(String fileUrl) {
        try {
            Path filePath = getFilePathFromUrl(fileUrl);
            return Files.size(filePath);
        } catch (Exception e) {
            log.debug("Cannot resolve file size for URL {}: {}", fileUrl, e.getMessage());
            return 0;
        }
    }

    public Path resolveFilePath(String fileUrl) {
        return getFilePathFromUrl(fileUrl);
    }
}