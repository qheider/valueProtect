package info.quazi.valueProtect.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "property_features")
public class PropertyFeatures {

    @Id
    @Column(name = "feature_id", length = 36, columnDefinition = "CHAR(36)")
    private String featureId;

    @Column(name = "bedroom_count")
    private Integer bedroomCount;

    @Column(name = "bathroom_count", precision = 3, scale = 1)
    private BigDecimal bathroomCount;

    @Column(name = "basement_type", length = 50)
    private String basementType;

    @Column(name = "garage_spaces")
    private Integer garageSpaces;

    @Column(name = "hvac_type", length = 100)
    private String hvacType;

    @Column(name = "exterior_material", length = 100)
    private String exteriorMaterial;

    @Column(name = "condition_rating", length = 2)
    private String conditionRating;

    @Column(name = "quality_rating", length = 2)
    private String qualityRating;

    @OneToOne
    @JoinColumn(name = "property_id", referencedColumnName = "property_id")
    private Property property;

    // Constructors
    public PropertyFeatures() {}

    public PropertyFeatures(String featureId, Property property) {
        this.featureId = featureId;
        this.property = property;
    }

    // Getters and Setters
    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getPropertyId() {
        return property != null ? property.getPropertyId() : null;
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

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}