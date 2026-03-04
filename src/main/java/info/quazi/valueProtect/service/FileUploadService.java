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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        @Value("${app.file.upload.directory:uploads/appraisal-documents}")
        private String uploadDirectory;

        private static final Pattern LEGACY_DOWNLOAD_URL_PATTERN =
            Pattern.compile(".*/appraisals/([^/]+)/documents/download/([^/]+)$");

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
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                return getLocalFileSize(fileUrl);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public Resource resolveResource(String fileUrl) throws IOException {
        return resolveResource(fileUrl, null, null);
    }

    public Resource resolveResource(String fileUrl, String appraisalId, String fileName) throws IOException {
        try {
            BlobInputStream inputStream = openFirstAvailableBlobInputStream(fileUrl, appraisalId, fileName);
            return new InputStreamResource(inputStream);
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                Resource localResource = resolveLocalResource(fileUrl, appraisalId, fileName);
                if (localResource != null) {
                    log.warn("Azure blob not found for URL {}, using local filesystem fallback", fileUrl);
                    return localResource;
                }
                throw new FileNotFoundException("Document not found in Azure Blob Storage");
            }
            throw new IOException("Unable to read Azure blob", e);
        }
    }

    public String resolveContentType(String fileUrl) {
        return resolveContentType(fileUrl, null, null);
    }

    public String resolveContentType(String fileUrl, String appraisalId, String fileName) {
        try {
            String contentType = resolveFirstAvailableBlobContentType(fileUrl, appraisalId, fileName);
            return (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        } catch (BlobStorageException e) {
            if (e.getStatusCode() == 404) {
                String localType = resolveLocalContentType(fileUrl, appraisalId, fileName);
                return localType == null ? "application/octet-stream" : localType;
            }
            return "application/octet-stream";
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

        String normalizedUrl = fileUrl.trim().split("\\?")[0].replace('\\', '/');

        String normalizedContainerUrl = containerUrl.endsWith("/") ? containerUrl : containerUrl + "/";
        if (!normalizedUrl.startsWith(normalizedContainerUrl)) {
            try {
                URI uri = new URI(normalizedUrl);
                String containerName = extractContainerName(containerUrl);
                String path = uri.getPath();
                String prefix = "/" + containerName + "/";
                if (path != null && path.startsWith(prefix)) {
                    return path.substring(prefix.length());
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid file URL format", e);
            }

            Matcher legacyMatcher = LEGACY_DOWNLOAD_URL_PATTERN.matcher(normalizedUrl);
            if (legacyMatcher.matches()) {
                return legacyMatcher.group(1) + "/" + legacyMatcher.group(2);
            }

            throw new IllegalArgumentException("File URL does not belong to configured Azure container");
        }

        return normalizedUrl.substring(normalizedContainerUrl.length());
    }

    private BlobInputStream openFirstAvailableBlobInputStream(String fileUrl, String appraisalId, String fileName) {
        BlobStorageException lastNotFound = null;
        for (String blobName : buildBlobCandidates(fileUrl, appraisalId, fileName)) {
            try {
                return getBlobContainerClient().getBlobClient(blobName).openInputStream();
            } catch (BlobStorageException e) {
                if (e.getStatusCode() == 404) {
                    lastNotFound = e;
                    continue;
                }
                throw e;
            }
        }
        if (lastNotFound != null) {
            throw lastNotFound;
        }
        throw new IllegalArgumentException("Unable to resolve blob candidate from URL");
    }

    private String resolveFirstAvailableBlobContentType(String fileUrl, String appraisalId, String fileName) {
        BlobStorageException lastNotFound = null;
        for (String blobName : buildBlobCandidates(fileUrl, appraisalId, fileName)) {
            try {
                return getBlobContainerClient().getBlobClient(blobName).getProperties().getContentType();
            } catch (BlobStorageException e) {
                if (e.getStatusCode() == 404) {
                    lastNotFound = e;
                    continue;
                }
                throw e;
            }
        }
        if (lastNotFound != null) {
            throw lastNotFound;
        }
        return null;
    }

    private List<String> buildBlobCandidates(String fileUrl, String appraisalId, String fileName) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();

        try {
            String primaryBlobName = extractBlobName(fileUrl);
            addBlobCandidate(candidates, primaryBlobName);
            addBlobCandidate(candidates, decodeIfEncoded(primaryBlobName));

            String filenameFromBlob = extractFilename(primaryBlobName);
            if (appraisalId != null && !appraisalId.isBlank() && filenameFromBlob != null && !filenameFromBlob.isBlank()) {
                addBlobCandidate(candidates, appraisalId + "/" + filenameFromBlob);
                addBlobCandidate(candidates, appraisalId + "/" + decodeIfEncoded(filenameFromBlob));
            }
        } catch (Exception ignored) {
        }

        if (appraisalId != null && !appraisalId.isBlank() && fileName != null && !fileName.isBlank()) {
            addBlobCandidate(candidates, appraisalId + "/" + fileName);
            addBlobCandidate(candidates, appraisalId + "/" + decodeIfEncoded(fileName));
        }

        if (fileName != null && !fileName.isBlank()) {
            addBlobCandidate(candidates, fileName);
            addBlobCandidate(candidates, decodeIfEncoded(fileName));
        }

        return new ArrayList<>(candidates);
    }

    private void addBlobCandidate(LinkedHashSet<String> candidates, String candidate) {
        if (candidate != null && !candidate.isBlank()) {
            candidates.add(candidate);
        }
    }

    private String decodeIfEncoded(String value) {
        if (value == null || value.isBlank() || !value.contains("%")) {
            return value;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String extractFilename(String pathLike) {
        if (pathLike == null || pathLike.isBlank()) {
            return null;
        }
        String normalized = pathLike.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        return lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
    }

    private Resource resolveLocalResource(String fileUrl, String appraisalId, String fileName) {
        try {
            Path localPath = resolveLocalPath(fileUrl, appraisalId, fileName);
            if (Files.exists(localPath) && Files.isReadable(localPath)) {
                return new FileSystemResource(localPath);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private long getLocalFileSize(String fileUrl) {
        try {
            Path localPath = resolveLocalPath(fileUrl, null, null);
            if (Files.exists(localPath)) {
                return Files.size(localPath);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private String resolveLocalContentType(String fileUrl, String appraisalId, String fileName) {
        try {
            Path localPath = resolveLocalPath(fileUrl, appraisalId, fileName);
            if (Files.exists(localPath)) {
                return Files.probeContentType(localPath);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Path resolveLocalPath(String fileUrl, String appraisalId, String fileName) {
        if (appraisalId != null && !appraisalId.isBlank() && fileName != null && !fileName.isBlank()) {
            Path explicit = Paths.get(uploadDirectory, appraisalId, fileName);
            if (Files.exists(explicit)) {
                return explicit;
            }
        }

        String normalized = fileUrl == null ? "" : fileUrl.trim().split("\\?")[0].replace('\\', '/');

        Matcher legacyMatcher = LEGACY_DOWNLOAD_URL_PATTERN.matcher(normalized);
        if (legacyMatcher.matches()) {
            return Paths.get(uploadDirectory, legacyMatcher.group(1), legacyMatcher.group(2));
        }

        int markerIndex = normalized.indexOf("uploads/appraisal-documents/");
        if (markerIndex >= 0) {
            String relative = normalized.substring(markerIndex + "uploads/appraisal-documents/".length());
            String[] segments = relative.split("/");
            if (segments.length >= 2) {
                String extractedAppraisalId = segments[0];
                String filename = segments[segments.length - 1];
                return Paths.get(uploadDirectory, extractedAppraisalId, filename);
            }
        }

        String[] parts = normalized.split("/");
        if (parts.length >= 2) {
            String extractedAppraisalId = parts[parts.length - 2];
            String filename = parts[parts.length - 1];
            return Paths.get(uploadDirectory, extractedAppraisalId, filename);
        }

        throw new IllegalArgumentException("Cannot resolve local file path from URL");
    }
}