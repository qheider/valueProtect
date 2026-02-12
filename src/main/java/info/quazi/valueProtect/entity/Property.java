package info.quazi.valueProtect.entity;

import info.quazi.valueProtect.converter.PropertyTypeConverter;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @Column(name = "property_id", length = 36, columnDefinition = "CHAR(36)")
    private String propertyId;

    @Column(name = "apn", length = 50)
    private String apn;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state_province", length = 50)
    private String stateProvince;

    @Column(name = "zip_postal_code", length = 20)
    private String zipPostalCode;

    @Convert(converter = PropertyTypeConverter.class)
    @Column(name = "property_type", columnDefinition = "VARCHAR(255)")
    private PropertyType propertyType;

    @Column(name = "year_built")
    private Integer yearBuilt;

    @Column(name = "lot_size_sqft", precision = 12, scale = 2)
    private BigDecimal lotSizeSqft;

    @Column(name = "living_area_sqft", precision = 12, scale = 2)
    private BigDecimal livingAreaSqft;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PropertyFeatures propertyFeatures;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appraisal> appraisals = new ArrayList<>();

    // Constructors
    public Property() {}

    public Property(String propertyId) {
        this.propertyId = propertyId;
        this.createdAt = LocalDateTime.now();
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PropertyFeatures getPropertyFeatures() {
        return propertyFeatures;
    }

    public void setPropertyFeatures(PropertyFeatures propertyFeatures) {
        this.propertyFeatures = propertyFeatures;
        if (propertyFeatures != null) {
            propertyFeatures.setProperty(this);
        }
    }

    public List<Appraisal> getAppraisals() {
        return appraisals;
    }

    public void setAppraisals(List<Appraisal> appraisals) {
        this.appraisals = appraisals;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum PropertyType {
        SINGLE_FAMILY("Single Family"),
        CONDO("Condo"),
        MULTI_FAMILY("Multi-Family"),
        COMMERCIAL("Commercial"),
        INDUSTRIAL("Industrial");

        private final String displayName;

        PropertyType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static PropertyType fromDisplayName(String displayName) {
            for (PropertyType type : values()) {
                if (type.displayName.equals(displayName)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown property type: " + displayName);
        }
    }
}