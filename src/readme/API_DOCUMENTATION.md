# Register Company with Admin User API

## Overview
The `registerCompanyWithAdminUser` API allows any client to create a complete company setup including:
- A new Company record
- An Admin User for that company
- An Employee record linking the user to the company with Admin role

## Entity Analysis Summary

### Company Entity
- **Fields**: name, companyCode, email, phone, website, address details, tax information
- **Status**: ACTIVE, INACTIVE, SUSPENDED
- **Relationships**: One-to-many with Employee

### User Entity  
- **Fields**: userName, email, password, enabled, lastLogin
- **Relationships**: Many-to-many with Role, One-to-many with Employee
- **Security**: Password is encrypted using PasswordEncoder

### Employee Entity
- **Fields**: firstName, lastName, employeeNumber, employeeType, contact details
- **Relationships**: Many-to-one with User and Company
- **Purpose**: Links users to companies they work for

### Role Entity
- **Fields**: name
- **Predefined Roles**: ADMIN (automatically assigned to company admin users)
- **Relationships**: Many-to-many with User

## API Endpoint

### POST `/api/companies/register-with-admin`

Creates a new company along with an admin user and employee record.

#### Request Body Example:
```json
{
  "companyName": "Acme Corporation",
  "companyCode": "ACME001",
  "companyEmail": "info@acme.com",
  "companyPhone": "+1-555-123-4567",
  "companyWebsite": "https://www.acme.com",
  "addressLine1": "123 Business Street",
  "addressLine2": "Suite 400",
  "city": "New York",
  "state": "NY", 
  "country": "USA",
  "postalCode": "10001",
  "taxId": "12-3456789",
  "registrationNumber": "REG123456",
  "description": "Leading technology solutions provider",
  "adminUsername": "admin.acme",
  "adminEmail": "admin@acme.com", 
  "adminPassword": "SecurePass123!",
  "adminFirstName": "John",
  "adminLastName": "Smith",
  "adminPhone": "+1-555-123-4568"
}
```

#### Required Fields:
- `companyName`
- `adminUsername` 
- `adminEmail`
- `adminPassword`

#### Response Example (Success):
```json
{
  "companyId": 1,
  "companyName": "Acme Corporation", 
  "companyCode": "ACME001",
  "companyStatus": "ACTIVE",
  "adminUserId": 2,
  "adminUsername": "admin.acme",
  "adminEmail": "admin@acme.com",
  "employeeId": 3,
  "message": "Company and admin user registered successfully"
}
```

#### Response Example (Error):
```json
{
  "companyId": null,
  "companyName": null,
  "companyCode": null, 
  "companyStatus": null,
  "adminUserId": null,
  "adminUsername": null,
  "adminEmail": null,
  "employeeId": null,
  "message": "Registration failed: User with username 'admin.acme' already exists"
}
```

## Features

### Validation
- Checks for duplicate company codes
- Validates unique usernames and emails
- Input validation using Bean Validation annotations
- Password encryption using Spring Security

### Atomicity
- All operations are wrapped in a transaction
- If any step fails, the entire registration is rolled back
- Ensures data consistency

### Default Values
- Company status is set to "ACTIVE"
- User is enabled by default
- Admin role is automatically assigned
- Employee number is auto-generated if not provided
- Employee type is set to 1 (Admin)

### Security
- Passwords are encrypted using bcrypt
- ADMIN role is automatically created if it doesn't exist
- All entities are marked as not archived (active)

## Error Handling

The API handles various error scenarios:
- Duplicate company code
- Duplicate username
- Duplicate email address
- Invalid input data
- Database transaction failures

All errors return HTTP 400 (Bad Request) with descriptive error messages.

## Usage Notes

1. This API is designed for initial company registration
2. The admin user created will have full administrative privileges 
3. Company codes should be unique across the system
4. Email addresses must be unique across all users
5. Passwords should meet security requirements (minimum 6 characters)
6. The API is accessible to any client (no authentication required for registration)

## Testing with Swagger UI

Once the application is running, you can test the API using Swagger UI at:
`http://localhost:8080/swagger-ui/index.html`

Look for the "Company" section and find the "Register company with admin user" endpoint.