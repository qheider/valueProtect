package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.Appraisal.AppraisalStatus;
import info.quazi.valueProtect.entity.Property.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AppraisalDto {

    private String appraisalId;
    private String propertyId;
    private String appraiserId;
    private String appraiserName;
    private LocalDate effectiveDate;
    private LocalDate reportDate;
    private BigDecimal appraisedValue;
    private String purpose;
    private AppraisalStatus status;
    private String finalReportUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Property information
    private PropertyDto property;
    
    // Documents
    private List<AppraisalDocumentDto> documents;
    
    // Document count for quick reference
    private int documentCount;

    // Constructors
    public AppraisalDto() {}

    // Getters and Setters
    public String getAppraisalId() {
        return appraisalId;
    }

    public void setAppraisalId(String appraisalId) {
        this.appraisalId = appraisalId;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getAppraiserId() {
        return appraiserId;
    }

    public void setAppraiserId(String appraiserId) {
        this.appraiserId = appraiserId;
    }

    public String getAppraiserName() {
        return appraiserName;
    }

    public void setAppraiserName(String appraiserName) {
        this.appraiserName = appraiserName;
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

    public PropertyDto getProperty() {
        return property;
    }

    public void setProperty(PropertyDto property) {
        this.property = property;
    }

    public List<AppraisalDocumentDto> getDocuments() {
        return documents;
    }

    public void setDocuments(List<AppraisalDocumentDto> documents) {
        this.documents = documents;
        this.documentCount = documents != null ? documents.size() : 0;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(int documentCount) {
        this.documentCount = documentCount;
    }

    public static class PropertyDto {
        private String propertyId;
        private String apn;
        private String addressLine1;
        private String city;
        private String stateProvince;
        private String zipPostalCode;
        private PropertyType propertyType;
        private Integer yearBuilt;
        private BigDecimal lotSizeSqft;
        private BigDecimal livingAreaSqft;
        private PropertyFeaturesDto features;

        // Constructors
        public PropertyDto() {}

        // Getters and Setters
        public String getPropertyId() {
            return propertyId;
        }

        public void setPropertyId(String propertyId) {
            this.propertyId = propertyId;
        }

        public String getApn() {
            return apn;
        }

        public void setApn(String apn) {
            this.apn = apn;
        }

        public String getAddressLine1() {
            return addressLine1;
        }

        public void setAddressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStateProvince() {
            return stateProvince;
        }

        public void setStateProvince(String stateProvince) {
            this.stateProvince = stateProvince;
        }

        public String getZipPostalCode() {
            return zipPostalCode;
        }

        public void setZipPostalCode(String zipPostalCode) {
            this.zipPostalCode = zipPostalCode;
        }

        public PropertyType getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(PropertyType propertyType) {
            this.propertyType = propertyType;
        }

        public Integer getYearBuilt() {
            return yearBuilt;
        }

        public void setYearBuilt(Integer yearBuilt) {
            this.yearBuilt = yearBuilt;
        }

        public BigDecimal getLotSizeSqft() {
            return lotSizeSqft;
        }

        public void setLotSizeSqft(BigDecimal lotSizeSqft) {
            this.lotSizeSqft = lotSizeSqft;
        }

        public BigDecimal getLivingAreaSqft() {
            return livingAreaSqft;
        }

        public void setLivingAreaSqft(BigDecimal livingAreaSqft) {
            this.livingAreaSqft = livingAreaSqft;
        }

        public PropertyFeaturesDto getFeatures() {
            return features;
        }

        public void setFeatures(PropertyFeaturesDto features) {
            this.features = features;
        }
    }

    public static class PropertyFeaturesDto {
        private String featureId;
        private Integer bedroomCount;
        private BigDecimal bathroomCount;
        private String basementType;
        private Integer garageSpaces;
        private String hvacType;
        private String exteriorMaterial;
        private String conditionRating;
        private String qualityRating;

        // Constructors
        public PropertyFeaturesDto() {}

        // Getters and Setters
        public String getFeatureId() {
            return featureId;
        }

        public void setFeatureId(String featureId) {
            this.featureId = featureId;
        }

        public Integer getBedroomCount() {
            return bedroomCount;
        }

        public void setBedroomCount(Integer bedroomCount) {
            this.bedroomCount = bedroomCount;
        }

        public BigDecimal getBathroomCount() {
            return bathroomCount;
        }

        public void setBathroomCount(BigDecimal bathroomCount) {
            this.bathroomCount = bathroomCount;
        }

        public String getBasementType() {
            return basementType;
        }

        public void setBasementType(String basementType) {
            this.basementType = basementType;
        }

        public Integer getGarageSpaces() {
            return garageSpaces;
        }

        public void setGarageSpaces(Integer garageSpaces) {
            this.garageSpaces = garageSpaces;
        }

        public String getHvacType() {
            return hvacType;
        }

        public void setHvacType(String hvacType) {
            this.hvacType = hvacType;
        }

        public String getExteriorMaterial() {
            return exteriorMaterial;
        }

        public void setExteriorMaterial(String exteriorMaterial) {
            this.exteriorMaterial = exteriorMaterial;
        }

        public String getConditionRating() {
            return conditionRating;
        }

        public void setConditionRating(String conditionRating) {
            this.conditionRating = conditionRating;
        }

        public String getQualityRating() {
            return qualityRating;
        }

        public void setQualityRating(String qualityRating) {
            this.qualityRating = qualityRating;
        }
    }
}