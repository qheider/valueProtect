package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.Employee;

public class CreateEmployeeRequest {

    private String contactDetailsCity;
    private String contactDetailsPhone;
    private String contactDetailsSecondaryPhone;
    private String firstName;
    private String lastName;
    private String employeeNumber;
    private Integer employeeType;
    private Long userId;
    
    // User account fields
    private String userName;
    private String email;
    private String password;
    private String roleName = "ROLE_EMPLOYEE"; // Default role

    public Employee toEntity() {
        Employee employee = new Employee();
        employee.setContactDetailsCity(this.contactDetailsCity);
        employee.setContactDetailsPhone(this.contactDetailsPhone);
        employee.setContactDetailsSecondaryPhone(this.contactDetailsSecondaryPhone);
        employee.setFirstName(this.firstName);
        employee.setLastName(this.lastName);
        employee.setEmployeeNumber(this.employeeNumber);
        employee.setEmployeeType(this.employeeType);
        return employee;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
