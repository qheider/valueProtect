package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.Property.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreatePropertyRequest {

    @NotBlank(message = "APN is required")
    private String apn;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State/Province is required")
    private String stateProvince;

    @NotBlank(message = "ZIP/Postal code is required")
    private String zipPostalCode;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    @Positive(message = "Year built must be positive")
    private Integer yearBuilt;

    @Positive(message = "Lot size must be positive")
    private BigDecimal lotSizeSqft;

    @Positive(message = "Living area must be positive")
    private BigDecimal livingAreaSqft;

    @Valid
    private PropertyFeaturesDto features;

    // Constructors
    public CreatePropertyRequest() {}

    // Getters and Setters
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

    public static class PropertyFeaturesDto {
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