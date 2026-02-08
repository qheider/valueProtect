package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.Employee;

public class EmployeeDto {

    private Long id;
    private String contactDetailsCity;
    private String contactDetailsPhone;
    private String contactDetailsSecondaryPhone;
    private String firstName;
    private String lastName;
    private String employeeNumber;
    private Integer employeeType;
    private Long userId;
    private String empPictureName;
    private String empPictureContentType;
    private Boolean archived;
    private Long companyId;
    private String companyName;
    private String userName;

    public static EmployeeDto fromEntity(Employee employee) {
        if (employee == null) {
            return null;
        }
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setContactDetailsCity(employee.getContactDetailsCity());
        dto.setContactDetailsPhone(employee.getContactDetailsPhone());
        dto.setContactDetailsSecondaryPhone(employee.getContactDetailsSecondaryPhone());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setEmployeeType(employee.getEmployeeType());
        dto.setUserId(employee.getUser() != null ? employee.getUser().getId() : null);
        dto.setEmpPictureName(employee.getEmpPictureName());
        dto.setEmpPictureContentType(employee.getEmpPictureContentType());
        dto.setArchived(employee.getArchived());
        dto.setCompanyId(employee.getCompany() != null ? employee.getCompany().getId() : null);
        dto.setCompanyName(employee.getCompany() != null ? employee.getCompany().getName() : null);
        dto.setUserName(employee.getUser() != null ? employee.getUser().getUserName() : null);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmpPictureName() {
        return empPictureName;
    }

    public void setEmpPictureName(String empPictureName) {
        this.empPictureName = empPictureName;
    }

    public String getEmpPictureContentType() {
        return empPictureContentType;
    }

    public void setEmpPictureContentType(String empPictureContentType) {
        this.empPictureContentType = empPictureContentType;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
