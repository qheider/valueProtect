package info.quazi.valueProtect.service;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${app.azure.storage.account-name}")
    private String accountName;

    @Value("${app.azure.storage.container-url}")
    private String containerUrl;

    @Value("${app.azure.storage.connection-string:}")
    private String connectionString;

    @Value("${app.azure.storage.account-key:}")
    private String accountKey;

    private BlobContainerClient blobContainerClient;

    private BlobContainerClient getBlobContainerClient() {
        if (blobContainerClient != null) {
            return blobContainerClient;
        }

        String containerName = extractContainerName(containerUrl);
        if (containerName == null || containerName.isBlank()) {
            throw new IllegalStateException("Invalid Azure container URL: " + containerUrl);
        }

        if (connectionString != null && !connectionString.isBlank()) {
            blobContainerClient = new BlobContainerClientBuilder()
                    .connectionString(connectionString)
                    .containerName(containerName)
                    .buildClient();
            return blobContainerClient;
        }

        String endpoint = String.format("https://%s.blob.core.windows.net", accountName);

        if (accountKey != null && !accountKey.isBlank()) {
            blobContainerClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(new AzureNamedKeyCredential(accountName, accountKey))
                    .buildClient()
                    .getBlobContainerClient(containerName);
            return blobContainerClient;
        }

        blobContainerClient = new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient()
                .getBlobContainerClient(containerName);
        return blobContainerClient;
    }

    public String uploadFile(MultipartFile file, String appraisalId, String documentType) throws IOException {
        log.info("Starting Azure file upload for appraisal: {}, file: {}, type: {}",
                appraisalId, file.getOriginalFilename(), documentType);
        
        validateFile(file);
        BlobContainerClient containerClient = getBlobContainerClient();
        
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(documentType, fileExtension);
        String blobName = appraisalId + "/" + uniqueFilename;
        
        try {
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");

            containerClient.getBlobClient(blobName)
                    .upload(file.getInputStream(), file.getSize(), true);
            containerClient.getBlobClient(blobName).setHttpHeaders(headers);

            String fileUrl = containerClient.getBlobClient(blobName).getBlobUrl();
            log.info("File uploaded successfully to Azure blob: {}", fileUrl);
            return fileUrl;
        } catch (BlobStorageException e) {
            log.error("Azure upload failed for blob {}: {}", blobName, e.getMessage());
            throw new IOException("Azure upload failed: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            getBlobContainerClient().getBlobClient(extractBlobName(fileUrl)).deleteIfExists();
        } catch (Exception e) {
            log.warn("Error deleting Azure blob {}: {}", fileUrl, e.getMessage());
        }
    }

    public boolean fileExists(String fileUrl) {
        try {
            return getBlobContainerClient().getBlobClient(extractBlobName(fileUrl)).exists();
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

    public long getFileSize(String fileUrl) {
        try {
            BlobProperties properties = getBlobContainerClient()
                    .getBlobClient(extractBlobName(fileUrl))
                    .getProperties();
            return properties.getBlobSize();
        } catch (Exception e) {
            return 0;
        }
    }

    public Resource resolveResource(String fileUrl) throws IOException {
        try {
            BlobInputStream inputStream = getBlobContainerClient()
                    .getBlobClient(extractBlobName(fileUrl))
                    .openInputStream();
            return new InputStreamResource(inputStream);
        } catch (BlobStorageException e) {
            throw new IOException("Unable to read Azure blob: " + e.getMessage(), e);
        }
    }

    public String resolveContentType(String fileUrl) {
        try {
            String contentType = getBlobContainerClient()
                    .getBlobClient(extractBlobName(fileUrl))
                    .getProperties()
                    .getContentType();
            return (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    private String extractContainerName(String fullContainerUrl) {
        if (fullContainerUrl == null || fullContainerUrl.isBlank()) {
            return null;
        }

        try {
            URI uri = new URI(fullContainerUrl);
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                return null;
            }
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid Azure container URL", e);
        }
    }

    private String extractBlobName(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("File URL cannot be blank");
        }

        String normalizedContainerUrl = containerUrl.endsWith("/") ? containerUrl : containerUrl + "/";
        if (!fileUrl.startsWith(normalizedContainerUrl)) {
            try {
                URI uri = new URI(fileUrl);
                String containerName = extractContainerName(containerUrl);
                String path = uri.getPath();
                String prefix = "/" + containerName + "/";
                if (path != null && path.startsWith(prefix)) {
                    return path.substring(prefix.length());
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid file URL format", e);
            }
            throw new IllegalArgumentException("File URL does not belong to configured Azure container");
        }

        return fileUrl.substring(normalizedContainerUrl.length());
    }
}