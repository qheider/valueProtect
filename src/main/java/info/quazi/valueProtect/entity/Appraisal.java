package info.quazi.valueProtect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appraisals")
public class Appraisal {

    @Id
    @Column(name = "appraisal_id", length = 36, columnDefinition = "CHAR(36)")
    private String appraisalId;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "report_date")
    private LocalDate reportDate;

    @Column(name = "appraised_value", precision = 15, scale = 2)
    private BigDecimal appraisedValue;

    @Column(name = "purpose", length = 100)
    private String purpose;

    @Convert(converter = AppraisalStatusConverter.class)
    @Column(name = "status")
    private AppraisalStatus status;

    @Column(name = "final_report_url", length = 512)
    private String finalReportUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", referencedColumnName = "property_id")
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appraiser_id", referencedColumnName = "id")
    private Employee appraiser;

    @OneToMany(mappedBy = "appraisal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AppraisalDocument> documents = new ArrayList<>();

    // Constructors
    public Appraisal() {}

    public Appraisal(String appraisalId) {
        this.appraisalId = appraisalId;
        this.status = AppraisalStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAppraisalId() {
        return appraisalId;
    }

    public void setAppraisalId(String appraisalId) {
        this.appraisalId = appraisalId;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public BigDecimal getAppraisedValue() {
        return appraisedValue;
    }

    public void setAppraisedValue(BigDecimal appraisedValue) {
        this.appraisedValue = appraisedValue;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public AppraisalStatus getStatus() {
        return status;
    }

    public void setStatus(AppraisalStatus status) {
        this.status = status;
    }

    public String getFinalReportUrl() {
        return finalReportUrl;
    }

    public void setFinalReportUrl(String finalReportUrl) {
        this.finalReportUrl = finalReportUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Employee getAppraiser() {
        return appraiser;
    }

    public void setAppraiser(Employee appraiser) {
        this.appraiser = appraiser;
    }

    public List<AppraisalDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<AppraisalDocument> documents) {
        this.documents = documents;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = AppraisalStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum AppraisalStatus {
        DRAFT("Draft"),
        REVIEW("Review"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        AppraisalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}