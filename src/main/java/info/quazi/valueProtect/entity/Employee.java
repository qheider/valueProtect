package info.quazi.valueProtect.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee")
public class Employee extends BaseEntity {

    @Column(name = "contactDetails_city", length = 255)
    private String contactDetailsCity;

    @Column(name = "contactDetails_phone", length = 255)
    private String contactDetailsPhone;

    @Column(name = "contactDetails_secondaryPhone", length = 255)
    private String contactDetailsSecondaryPhone;

    @Column(name = "firstName", length = 255)
    private String firstName;

    @Column(name = "lastName", length = 255)
    private String lastName;

    @Column(name = "employeeNumber", length = 255)
    private String employeeNumber;

    @Column(name = "employeeType")
    private Integer employeeType;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "empPicture_contentType", length = 255)
    private String empPictureContentType;

    @Lob
    @Column(name = "empPicture_data", columnDefinition = "MEDIUMBLOB")
    private byte[] empPictureData;

    @Column(name = "empPicture_name", length = 255)
    private String empPictureName;

    @OneToMany(mappedBy = "appraiser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appraisal> appraisals = new ArrayList<>();

    public String getContactDetailsCity() {
        return contactDetailsCity;
    }

    public void setContactDetailsCity(String contactDetailsCity) {
        this.contactDetailsCity = contactDetailsCity;
    }

    public String getContactDetailsPhone() {
        return contactDetailsPhone;
    }

    public void setContactDetailsPhone(String contactDetailsPhone) {
        this.contactDetailsPhone = contactDetailsPhone;
    }

    public String getContactDetailsSecondaryPhone() {
        return contactDetailsSecondaryPhone;
    }

    public void setContactDetailsSecondaryPhone(String contactDetailsSecondaryPhone) {
        this.contactDetailsSecondaryPhone = contactDetailsSecondaryPhone;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public Integer getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(Integer employeeType) {
        this.employeeType = employeeType;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getEmpPictureContentType() {
        return empPictureContentType;
    }

    public void setEmpPictureContentType(String empPictureContentType) {
        this.empPictureContentType = empPictureContentType;
    }

    public byte[] getEmpPictureData() {
        return empPictureData;
    }

    public void setEmpPictureData(byte[] empPictureData) {
        this.empPictureData = empPictureData;
    }

    public String getEmpPictureName() {
        return empPictureName;
    }

    public void setEmpPictureName(String empPictureName) {
        this.empPictureName = empPictureName;
    }

    public List<Appraisal> getAppraisals() {
        return appraisals;
    }

    public void setAppraisals(List<Appraisal> appraisals) {
        this.appraisals = appraisals;
    }
}
