package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.AppraisalDocument.DocumentType;

import java.time.LocalDateTime;

public class AppraisalDocumentDto {

    private String documentId;
    private String appraisalId;
    private DocumentType documentType;
    private String fileName;
    private String fileUrl;
    private LocalDateTime uploadedAt;
    private long fileSize;

    // Constructors
    public AppraisalDocumentDto() {}

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getAppraisalId() {
        return appraisalId;
    }

    public void setAppraisalId(String appraisalId) {
        this.appraisalId = appraisalId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}