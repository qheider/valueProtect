package info.quazi.valueProtect.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private String message;
    private String username;
    private String email;
    private Long userId;
    private Long employeeId;
    private List<String> roles;
    
    public LoginResponse() {}
    
    public LoginResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
    
    public LoginResponse(String token, String message, String username, String email, Long userId) {
        this.token = token;
        this.message = message;
        this.username = username;
        this.email = email;
        this.userId = userId;
    }
    
    public LoginResponse(String token, String message, String username, String email, Long userId, Long employeeId, List<String> roles) {
        this.token = token;
        this.message = message;
        this.username = username;
        this.email = email;
        this.userId = userId;
        this.employeeId = employeeId;
        this.roles = roles;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}