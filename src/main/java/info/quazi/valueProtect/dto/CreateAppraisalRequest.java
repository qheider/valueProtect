package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.Appraisal.AppraisalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateAppraisalRequest {

    @NotNull(message = "Property information is required")
    @Valid
    private PropertyInfo property;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    @NotNull(message = "Report date is required")
    private LocalDate reportDate;

    @NotNull(message = "Appraised value is required")
    @Positive(message = "Appraised value must be positive")
    private BigDecimal appraisedValue;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    private AppraisalStatus status = AppraisalStatus.DRAFT;

    private String finalReportUrl;

    // Constructors
    public CreateAppraisalRequest() {}

    // Getters and Setters
    public PropertyInfo getProperty() {
        return property;
    }

    public void setProperty(PropertyInfo property) {
        this.property = property;
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

    public static class PropertyInfo {
        // Either provide existing property ID or create new property
        private String existingPropertyId;
        
        @Valid
        private CreatePropertyRequest newProperty;

        // Constructors
        public PropertyInfo() {}

        // Getters and Setters
        public String getExistingPropertyId() {
            return existingPropertyId;
        }

        public void setExistingPropertyId(String existingPropertyId) {
            this.existingPropertyId = existingPropertyId;
        }

        public CreatePropertyRequest getNewProperty() {
            return newProperty;
        }

        public void setNewProperty(CreatePropertyRequest newProperty) {
            this.newProperty = newProperty;
        }
    }
}