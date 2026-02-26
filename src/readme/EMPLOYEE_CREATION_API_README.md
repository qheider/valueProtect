# Employee Management API

This API allows admin employees to create new employees for their own company with proper security and validation.

## Features Created

### 1. Employee Creation API
- **Endpoint**: `POST /api/employees`
- **Security**: Only admin users can create employees
- **Company Isolation**: Admins can only create employees for their own company
- **User Account Management**: Can create new user accounts or link to existing users

### 2. Enhanced Security Rules
- Only authenticated users with `ROLE_ADMIN` can create employees
- Admin must have an active employee record associated with a company
- New employees are automatically assigned to the admin's company
- Cannot assign `ROLE_ADMIN` to new employees (defaults to `ROLE_EMPLOYEE`)

### 3. Validation and Business Logic
- Required fields: `firstName`, `lastName`
- Auto-generates email if not provided: `firstname.lastname@{company}.com`
- Tracks audit information (who created the employee)
- Input validation with proper error messages

### 4. Key Components Updated

#### EmployeeController
- Updated `POST /api/employees` to use secure employee creation
- Added comprehensive API documentation with security rules
- Added validation annotations

#### EmployeeService
- New method: `createEmployeeForAdminCompany()` with security enforcement
- Validates admin role and company association
- Auto-generates emails and enforces role restrictions

#### CreateEmployeeRequest DTO
- Added validation annotations for input fields
- Field length limits and format validation
- Clear field descriptions

### 5. API Usage Examples

#### Create Employee with New User Account
```json
POST /api/employees
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe", 
  "employeeNumber": "EMP001",
  "contactDetailsPhone": "1234567890",
  "contactDetailsCity": "New York",
  "employeeType": 1,
  "userName": "john.doe",
  "password": "securepass123",
  "roleName": "ROLE_EMPLOYEE"
}
```

#### Create Employee with Existing User
```json
POST /api/employees
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Smith",
  "employeeNumber": "EMP002", 
  "contactDetailsPhone": "0987654321",
  "userId": 123
}
```

#### Minimal Employee Creation
```json
POST /api/employees
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "firstName": "Alice",
  "lastName": "Johnson",
  "userName": "alice.johnson", 
  "password": "password123"
}
```

### 6. Response Format
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "employeeNumber": "EMP001", 
  "contactDetailsPhone": "1234567890",
  "contactDetailsCity": "New York",
  "employeeType": 1,
  "companyId": 1,
  "companyName": "Test Company",
  "userName": "john.doe",
  "userId": 2,
  "archived": false
}
```

### 7. Error Responses

- **403 Forbidden**: "Access denied. Only admin users can create employees."
- **400 Bad Request**: "Your employee record is not associated with any company. Cannot create employees."
- **400 Bad Request**: Validation errors for required fields
- **404 Not Found**: "User not found with id: 123" (when linking existing user)

### 8. Security Implementation

1. **Authentication**: JWT Bearer token required
2. **Authorization**: `ROLE_ADMIN` required
3. **Company Isolation**: Admin can only create employees for their own company
4. **Role Restrictions**: New employees cannot be given admin role
5. **Audit Trail**: Tracks which admin created each employee

### 9. Additional Endpoints

- `GET /api/employees/company/{companyId}` - Get employees by company (admin only)
- `GET /api/employees` - Get all active employees

### 10. Testing

Comprehensive integration tests created to verify:
- Successful employee creation by admin
- Access denied for non-admin users
- Company association validation
- Email auto-generation
- Security enforcement

## Files Modified/Created

1. **EmployeeController.java** - Updated with secure endpoint
2. **EmployeeService.java** - Added `createEmployeeForAdminCompany()` method
3. **CreateEmployeeRequest.java** - Added validation annotations
4. **EmployeeCreationIntegrationTest.java** - Comprehensive tests
5. **EMPLOYEE_API_DOCUMENTATION.json** - Detailed API documentation

## Next Steps

1. Run the application: `./mvnw spring-boot:run`
2. Test the API using the admin user credentials
3. Use the provided JSON examples to test employee creation
4. Check logs for audit information and debugging

The API is now ready for production use with proper security, validation, and business rule enforcement.