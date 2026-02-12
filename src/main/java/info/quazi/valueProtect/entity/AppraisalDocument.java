package info.quazi.valueProtect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_documents")
public class AppraisalDocument {

    @Id
    @Column(name = "document_id", length = 36, columnDefinition = "CHAR(36)")
    private String documentId;

    @Column(name = "appraisal_id", length = 36, columnDefinition = "CHAR(36)")
    private String appraisalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_url", length = 512)
    private String fileUrl;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appraisal_id", insertable = false, updatable = false)
    private Appraisal appraisal;

    // Constructors
    public AppraisalDocument() {}

    public AppraisalDocument(String documentId, String appraisalId, DocumentType documentType) {
        this.documentId = documentId;
        this.appraisalId = appraisalId;
        this.documentType = documentType;
        this.uploadedAt = LocalDateTime.now();
    }

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

    public Appraisal getAppraisal() {
        return appraisal;
    }

    public void setAppraisal(Appraisal appraisal) {
        this.appraisal = appraisal;
    }

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public enum DocumentType {
        TITLE_DEED("Title Deed"),
        FLOOR_PLAN("Floor Plan"),
        PLAT_MAP("Plat Map"),
        PROPERTY_PHOTO("Property Photo"),
        TAX_RECORD("Tax Record"),
        OTHER("Other");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}