# Appraisal CRUD API Implementation

This document provides a complete implementation of the appraisal CRUD API with company-level security, file upload functionality, and role-based access control.

## Overview

The appraisal system provides secure CRUD operations for property appraisals with the following key features:

1. **Company-Level Security**: Each appraisal belongs to the company of the authenticated employee
2. **Role-Based Access**: Admin users can access all company appraisals, regular users only their own
3. **File Upload Support**: Documents can be uploaded and associated with appraisals
4. **Property Management**: Properties are automatically created or linked during appraisal creation

## Database Schema

### Tables Created

1. **properties** - Stores property information
2. **property_features** - Stores detailed property characteristics
3. **appraisals** - Main appraisal records
4. **appraisal_documents** - File attachments linked to appraisals

### Key Relationships

- `appraisals.appraiser_id` → `employee.id` (enforces company ownership)
- `appraisals.property_id` → `properties.property_id`
- `appraisal_documents.appraisal_id` → `appraisals.appraisal_id`
- `property_features.property_id` → `properties.property_id`

## API Endpoints

### Authentication Required
All endpoints require JWT token authentication via `Authorization: Bearer <token>` header.

### Main Endpoints

#### 1. Create Appraisal
```http
POST /api/appraisals
Content-Type: application/json
Authorization: Bearer <token>

{
  "property": {
    "existingPropertyId": "uuid-here", // OR create new property
    "newProperty": {
      "apn": "123-456-789",
      "addressLine1": "123 Main St",
      "city": "Anytown",
      "stateProvince": "CA",
      "zipPostalCode": "12345",
      "propertyType": "SINGLE_FAMILY",
      "yearBuilt": 2020,
      "lotSizeSqft": 7500.00,
      "livingAreaSqft": 2500.00,
      "features": {
        "bedroomCount": 4,
        "bathroomCount": 2.5,
        "basementType": "Full",
        "garageSpaces": 2,
        "hvacType": "Central Air",
        "exteriorMaterial": "Brick",
        "conditionRating": "A1",
        "qualityRating": "A1"
      }
    }
  },
  "effectiveDate": "2024-01-15",
  "reportDate": "2024-01-20",
  "appraisedValue": 450000.00,
  "purpose": "Purchase",
  "status": "DRAFT"
}
```

#### 2. Get Appraisals List
```http
GET /api/appraisals
Authorization: Bearer <token>
```

**Behavior**:
- Admin users: Returns all appraisals for their company
- Regular users: Returns only their own appraisals

#### 3. Get Specific Appraisal
```http
GET /api/appraisals/{appraisalId}
Authorization: Bearer <token>
```

#### 4. Update Appraisal
```http
PUT /api/appraisals/{appraisalId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "effectiveDate": "2024-01-16",
  "reportDate": "2024-01-21",
  "appraisedValue": 455000.00,
  "purpose": "Refinance",
  "status": "REVIEW"
}
```

#### 5. Delete Appraisal
```http
DELETE /api/appraisals/{appraisalId}
Authorization: Bearer <token>
```

### Document Management Endpoints

#### 6. Upload Document
```http
POST /api/appraisals/{appraisalId}/documents
Content-Type: multipart/form-data
Authorization: Bearer <token>

Form Data:
- file: <document file>
- documentType: TITLE_DEED | FLOOR_PLAN | PLAT_MAP | PROPERTY_PHOTO | TAX_RECORD | OTHER
```

**File Restrictions**:
- Maximum size: 10MB
- Allowed types: PDF, images, Word documents, Excel files

#### 7. Get Documents
```http
GET /api/appraisals/{appraisalId}/documents
Authorization: Bearer <token>
```

#### 8. Download Document
```http
GET /api/appraisals/{appraisalId}/documents/download/{filename}
Authorization: Bearer <token>
```

#### 9. Delete Document
```http
DELETE /api/documents/{documentId}
Authorization: Bearer <token>
```

## Security Implementation

### Company-Level Isolation

1. **Service Layer Security**: All operations verify that the appraisal belongs to the current user's company
2. **Repository Queries**: Custom queries join with employee table to enforce company ownership
3. **Access Control**: Users can only access appraisals where `appraiser.company.id = currentUser.employee.company.id`

### Role-Based Access Control

#### Admin Users (Role: ADMIN)
- View all appraisals in their company
- Modify any appraisal in their company
- Delete any appraisal in their company

#### Regular Users (Role: USER)
- View only their own appraisals
- Modify only their own appraisals
- Delete only their own appraisals

### Authentication Flow

1. User logs in via `/api/auth/login`
2. JWT token returned containing user information
3. Token included in `Authorization: Bearer <token>` header
4. `SecurityContextService` extracts current user and employee information
5. All operations validated against current user's company

## File Storage

### Directory Structure
```
uploads/
└── appraisal-documents/
    └── {appraisalId}/
        ├── title_deed_20240115_a1b2c3d4.pdf
        ├── property_photo_20240115_e5f6g7h8.jpg
        └── floor_plan_20240115_i9j0k1l2.pdf
```

### File URL Format
```
http://localhost:8080/api/appraisals/{appraisalId}/documents/download/{filename}
```

### File Naming Convention
```
{documentType}_{timestamp}_{uuid}.{extension}
```

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "code": "INVALID_REQUEST",
  "message": "Property information is required"
}
```

#### 401 Unauthorized
```json
{
  "code": "UNAUTHORIZED",
  "message": "JWT token is missing or invalid"
}
```

#### 403 Forbidden
```json
{
  "code": "ACCESS_DENIED",
  "message": "You can only modify your own appraisals"
}
```

#### 404 Not Found
```json
{
  "code": "NOT_FOUND",
  "message": "Appraisal not found or access denied"
}
```

## Configuration

### Application Properties
```properties
# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.file.upload.directory=uploads/appraisal-documents
app.file.base.url=http://localhost:8080
```

### Database Configuration
The system uses JPA with MySQL. Tables are auto-created via `spring.jpa.hibernate.ddl-auto=update`.

## Usage Examples

### Creating an Appraisal with New Property

1. **Login** to get JWT token
2. **POST** to `/api/appraisals` with property details
3. **Upload documents** via `/api/appraisals/{id}/documents`
4. **Update status** to move through workflow

### Creating an Appraisal with Existing Property

1. **GET** existing properties (if endpoint exists)
2. **POST** to `/api/appraisals` with `existingPropertyId`
3. Continue with document upload and updates

### Admin Workflow

1. **GET** `/api/appraisals` to see all company appraisals
2. **PUT** `/api/appraisals/{id}` to update any appraisal
3. **DELETE** `/api/appraisals/{id}` to remove appraisals

### Regular User Workflow

1. **GET** `/api/appraisals` to see own appraisals only
2. **PUT** `/api/appraisals/{id}` to update own appraisals only
3. **DELETE** `/api/appraisals/{id}` to remove own appraisals only

## Testing

### Using Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Authentication in Swagger
1. Click "Authorize" button
2. Enter: `Bearer <your-jwt-token>`
3. Test endpoints interactively

## Installation and Setup

1. **Database Setup**: Run the SQL script in `src/Script/appraisal_schema.sql`
2. **File Directory**: Ensure `uploads/appraisal-documents/` directory exists and is writable
3. **Configuration**: Update `application.properties` with your database and file settings
4. **Build and Run**: `mvn spring-boot:run`

## Architecture Notes

### Entity Design
- Uses UUID strings for appraisal and property IDs (36 characters)
- Proper JPA relationships with lazy loading
- Audit fields (created_at, updated_at) handled automatically

### Service Layer
- `AppraisalService`: Main business logic with security checks
- `SecurityContextService`: Centralized security context management
- `FileUploadService`: File handling with validation and cleanup

### Repository Layer
- Custom queries for company-level security
- Efficient joins to minimize database calls
- Proper indexing for performance

### Controller Layer
- OpenAPI/Swagger documentation
- Comprehensive error handling
- File upload/download support
- Security annotations

This implementation provides a complete, secure, and scalable appraisal management system with proper company-level isolation and role-based access control.