package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.Appraisal.AppraisalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateAppraisalRequest {

    private LocalDate effectiveDate;
    
    private LocalDate reportDate;
    
    private BigDecimal appraisedValue;
    
    private String purpose;
    
    private AppraisalStatus status;
    
    private String finalReportUrl;

    // Constructors
    public UpdateAppraisalRequest() {}

    // Getters and Setters
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
}