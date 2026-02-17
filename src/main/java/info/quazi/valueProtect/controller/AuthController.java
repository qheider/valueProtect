package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.LoginRequest;
import info.quazi.valueProtect.dto.LoginResponse;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.User;
import info.quazi.valueProtect.repository.EmployeeRepository;
import info.quazi.valueProtect.service.JwtService;
import info.quazi.valueProtect.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/login")
    @Operation(
        summary = "User login", 
        description = "Authenticate user and return JWT token for API access"
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );
            
            // Generate JWT token
            String token = jwtService.generateToken(authentication.getName());
            
            // Get user details
            Optional<User> userOpt = userService.findByUserName(loginRequest.getUsername());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Get roles
                List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());
                
                // Get employee ID if exists
                Long employeeId = null;
                Optional<Employee> employeeOpt = employeeRepository.findByUserId(user.getId());
                if (employeeOpt.isPresent()) {
                    employeeId = employeeOpt.get().getId();
                }
                
                LoginResponse response = new LoginResponse(
                    token, 
                    "Login successful", 
                    user.getUserName(), 
                    user.getEmail(),
                    user.getId(),
                    employeeId,
                    roles
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(new LoginResponse(token, "Login successful"));
            }
            
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                .body(new LoginResponse(null, "Invalid username or password"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                .body(new LoginResponse(null, "Authentication failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new LoginResponse(null, "Internal server error"));
        }
    }
    
    @PostMapping("/logout")
    @Operation(
        summary = "User logout", 
        description = "Logout user (client should discard the JWT token)"
    )
    public ResponseEntity<LoginResponse> logout() {
        // JWT tokens are stateless, so logout is handled on the client side
        // The client should simply discard the token
        return ResponseEntity.ok(new LoginResponse(null, "Logout successful"));
    }
}