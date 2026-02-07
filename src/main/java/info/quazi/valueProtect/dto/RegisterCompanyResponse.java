package info.quazi.valueProtect.dto;

public class RegisterCompanyResponse {
    
    private Long companyId;
    private String companyName;
    private String companyCode;
    private String companyStatus;
    
    private Long adminUserId;
    private String adminUsername;
    private String adminEmail;
    
    private Long employeeId;
    
    private String message;
    
    // Constructors
    public RegisterCompanyResponse() {}
    
    public RegisterCompanyResponse(Long companyId, String companyName, String companyCode, 
                                 String companyStatus, Long adminUserId, String adminUsername, 
                                 String adminEmail, Long employeeId, String message) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyCode = companyCode;
        this.companyStatus = companyStatus;
        this.adminUserId = adminUserId;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.employeeId = employeeId;
        this.message = message;
    }
    
    // Getters and Setters
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
    
    public String getCompanyCode() {
        return companyCode;
    }
    
    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    
    public String getCompanyStatus() {
        return companyStatus;
    }
    
    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }
    
    public Long getAdminUserId() {
        return adminUserId;
    }
    
    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }
    
    public String getAdminUsername() {
        return adminUsername;
    }
    
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
    
    public String getAdminEmail() {
        return adminEmail;
    }
    
    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
    
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}