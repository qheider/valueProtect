# Swagger UI Documentation

## Overview
Swagger UI provides an interactive interface for exploring and testing the ValueProtect API. It automatically generates documentation from the OpenAPI specification and allows you to execute API requests directly from your browser.

## Accessing Swagger UI

### Local Development
When running the application locally, access Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```

### Production
For production environments, replace `localhost:8080` with your deployed application URL:
```
https://your-domain.com/swagger-ui/index.html
```

## Features

### Interactive API Documentation
- **Explore Endpoints**: Browse all available API endpoints organized by controllers
- **View Schemas**: Examine request and response data structures
- **Test Requests**: Execute API calls directly from the browser
- **Authentication Support**: Test endpoints that require authentication

### API Groups
The ValueProtect API is organized into the following groups:
- **Company**: Company registration and management
- **Appraisal**: Appraisal operations and workflows
- **Auth**: Authentication and authorization
- **Employee**: Employee management
- **Policy**: Insurance policy operations
- **Client**: Client/customer management
- **Claim**: Claims processing

## Using Swagger UI

### Testing Public Endpoints

1. **Navigate to the endpoint** you want to test
2. **Click "Try it out"** button
3. **Fill in the request parameters** (body, query, path parameters)
4. **Click "Execute"** to send the request
5. **Review the response** including status code, headers, and body

### Example: Register a Company

1. Expand the **Company** section
2. Find **POST `/api/companies/register-with-admin`**
3. Click **"Try it out"**
4. Modify the request body with your data:
```json
{
  "companyName": "Test Company",
  "companyCode": "TEST001",
  "companyEmail": "info@testcompany.com",
  "adminUsername": "admin.test",
  "adminEmail": "admin@testcompany.com",
  "adminPassword": "SecurePass123!"
}
```
5. Click **"Execute"**
6. View the response with the created company and admin details

### Testing Protected Endpoints

For endpoints that require authentication:

1. **Authenticate first** using the login endpoint:
   - Navigate to **Auth** section
   - Use **POST `/api/auth/login`**
   - Provide valid credentials
   - Copy the JWT token from the response

2. **Authorize Swagger UI**:
   - Click the **"Authorize"** button at the top of the page
   - Enter the token in the format: `Bearer <your-token>`
   - Click **"Authorize"**
   - Click **"Close"**

3. **Test protected endpoints**:
   - All subsequent requests will include the authorization header
   - Execute requests as normal

## Response Codes

Common HTTP response codes you'll encounter:

- **200 OK**: Request succeeded
- **201 Created**: Resource created successfully
- **400 Bad Request**: Invalid input or validation error
- **401 Unauthorized**: Authentication required or failed
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side error

## Request Body Editor

Swagger UI provides a JSON editor for request bodies:
- **Syntax highlighting**: Easy to read and edit JSON
- **Example values**: Auto-populated with example data
- **Model schema**: View the expected structure
- **Validation**: Client-side validation before sending

## Tips and Best Practices

### Testing Workflow
1. Start with public endpoints (like company registration)
2. Authenticate to get a token
3. Test protected endpoints with the token
4. Use the schema definitions to understand data structures

### Data Management
- Use unique values for codes and emails when testing
- Keep track of created resource IDs for subsequent operations
- Use the example values as templates for your test data

### Troubleshooting
- **401 Errors**: Check if your token is valid and properly formatted
- **400 Errors**: Review required fields and data formats
- **CORS Issues**: Ensure you're accessing from an allowed origin

## OpenAPI Specification

### Viewing the Raw Spec
Access the raw OpenAPI specification at:
```
http://localhost:8080/v3/api-docs
```

### YAML Format
For YAML format:
```
http://localhost:8080/v3/api-docs.yaml
```

### Importing to Other Tools
You can import the OpenAPI specification into:
- **Postman**: Import the JSON/YAML spec URL
- **Insomnia**: Use the OpenAPI import feature
- **API clients**: Any tool supporting OpenAPI 3.0

## Configuration

The Swagger UI configuration is defined in the OpenAPI configuration class. Key settings include:
- API title and description
- Version information
- Contact details
- License information
- Security schemes

## Additional Resources

### Related Documentation
- [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Detailed API usage guide
- [APPRAISAL_API_DOCUMENTATION.md](APPRAISAL_API_DOCUMENTATION.md) - Appraisal endpoints
- [EMPLOYEE_CREATION_API_README.md](EMPLOYEE_CREATION_API_README.md) - Employee management

### External Resources
- [Swagger UI Documentation](https://swagger.io/tools/swagger-ui/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Spring Doc OpenAPI](https://springdoc.org/)

## Support

For issues or questions:
1. Check the API documentation files in the `readme` folder
2. Review the OpenAPI schema for detailed field information
3. Examine response messages for specific error details
4. Contact the development team for persistent issues
