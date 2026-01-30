package info.quazi.valueProtect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "billingcompany")
public class BillingCompany extends BaseEntity {

    @Column(name = "address_city", length = 255)
    private String addressCity;

    @Column(name = "address_country", length = 255)
    private String addressCountry;

    @Column(name = "address_postalCode", length = 255)
    private String addressPostalCode;

    @Column(name = "address_province", length = 255)
    private String addressProvince;

    @Column(name = "address_streetDirection", length = 255)
    private String addressStreetDirection;

    @Column(name = "address_streetName", length = 255)
    private String addressStreetName;

    @Column(name = "address_streetNumber", length = 255)
    private String addressStreetNumber;

    @Column(name = "address_streetType", length = 255)
    private String addressStreetType;

    @Column(name = "address_unitNumber")
    private Integer addressUnitNumber;

    @Column(name = "brand", length = 255)
    private String brand;

    @Column(name = "companyType", length = 255)
    private String companyType;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "primaryPhone", length = 255)
    private String primaryPhone;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "name", length = 250)
    private String name;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "webUrl", length = 255)
    private String webUrl;

    @Column(name = "poBox", length = 255)
    private String poBox;

    @Column(name = "companyInfo", length = 255)
    private String companyInfo;

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public String getAddressPostalCode() {
        return addressPostalCode;
    }

    public void setAddressPostalCode(String addressPostalCode) {
        this.addressPostalCode = addressPostalCode;
    }

    public String getAddressProvince() {
        return addressProvince;
    }

    public void setAddressProvince(String addressProvince) {
        this.addressProvince = addressProvince;
    }

    public String getAddressStreetDirection() {
        return addressStreetDirection;
    }

    public void setAddressStreetDirection(String addressStreetDirection) {
        this.addressStreetDirection = addressStreetDirection;
    }

    public String getAddressStreetName() {
        return addressStreetName;
    }

    public void setAddressStreetName(String addressStreetName) {
        this.addressStreetName = addressStreetName;
    }

    public String getAddressStreetNumber() {
        return addressStreetNumber;
    }

    public void setAddressStreetNumber(String addressStreetNumber) {
        this.addressStreetNumber = addressStreetNumber;
    }

    public String getAddressStreetType() {
        return addressStreetType;
    }

    public void setAddressStreetType(String addressStreetType) {
        this.addressStreetType = addressStreetType;
    }

    public Integer getAddressUnitNumber() {
        return addressUnitNumber;
    }

    public void setAddressUnitNumber(Integer addressUnitNumber) {
        this.addressUnitNumber = addressUnitNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrimaryPhone() {
        return primaryPhone;
    }

    public void setPrimaryPhone(String primaryPhone) {
        this.primaryPhone = primaryPhone;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getCompanyInfo() {
        return companyInfo;
    }

    public void setCompanyInfo(String companyInfo) {
        this.companyInfo = companyInfo;
    }
}
