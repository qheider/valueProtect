package info.quazi.valueProtect.service;

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
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${app.file.upload.directory:uploads/appraisal-documents}")
    private String uploadDirectory;

    @Value("${app.file.base.url:http://localhost:8080}")
    private String baseUrl;

    public String uploadFile(MultipartFile file, String appraisalId, String documentType) throws IOException {
        // Validate file
        validateFile(file);
        
        // Create directory structure
        Path uploadPath = createDirectoryStructure(appraisalId);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(documentType, fileExtension);
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return file URL
        return generateFileUrl(appraisalId, uniqueFilename);
    }

    public void deleteFile(String fileUrl) {
        try {
            Path filePath = getFilePathFromUrl(fileUrl);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Error deleting file: " + fileUrl + " - " + e.getMessage());
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
        Path appraisalPath = basePath.resolve(appraisalId);
        
        if (!Files.exists(appraisalPath)) {
            Files.createDirectories(appraisalPath);
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
        // Extract filename from URL
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        
        // Extract appraisal ID from URL pattern: .../appraisals/{appraisalId}/documents/download/{filename}
        String[] urlParts = fileUrl.split("/");
        String appraisalId = null;
        for (int i = 0; i < urlParts.length - 3; i++) {
            if ("appraisals".equals(urlParts[i]) && "documents".equals(urlParts[i + 2])) {
                appraisalId = urlParts[i + 1];
                break;
            }
        }
        
        if (appraisalId == null) {
            throw new IllegalArgumentException("Invalid file URL format");
        }
        
        return Paths.get(uploadDirectory, appraisalId, filename);
    }

    public long getFileSize(String fileUrl) {
        try {
            Path filePath = getFilePathFromUrl(fileUrl);
            return Files.size(filePath);
        } catch (IOException e) {
            return 0;
        }
    }
}