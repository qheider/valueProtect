# Document Upload Troubleshooting Guide

## 📁 File Storage Location

Files are stored in the following directory structure:
```
C:\AI projects\valueProtect\uploads\appraisal-documents\{appraisalId}\
```

For example, if an appraisal has ID `abc-123-def-456`, files will be stored at:
```
C:\AI projects\valueProtect\uploads\appraisal-documents\abc-123-def-456\
```

## 🔍 Troubleshooting Steps

### 1. **Fix Enum Value Mismatch (CRITICAL)**

The most common issue is a mismatch between database enum values and Java enum constants.

**Check your current database schema:**
```sql
SHOW COLUMNS FROM appraisal_documents LIKE 'document_type';
```

**If you see:** `'Title Deed', 'Floor Plan', 'Plat Map'` (with spaces and title case)
**You need:** `'TITLE_DEED', 'FLOOR_PLAN', 'PLAT_MAP'` (underscores, uppercase)

**To fix, run this migration script:**
```bash
mysql -u quazisr -p actpro < src/Script/fix_document_enum_values.sql
```

**Or manually:**
```sql
USE actpro;

-- Change to VARCHAR temporarily
ALTER TABLE appraisal_documents 
MODIFY COLUMN document_type VARCHAR(50);

-- Update existing data
UPDATE appraisal_documents SET document_type = 'TITLE_DEED' WHERE document_type = 'Title Deed';
UPDATE appraisal_documents SET document_type = 'FLOOR_PLAN' WHERE document_type = 'Floor Plan';
UPDATE appraisal_documents SET document_type = 'PLAT_MAP' WHERE document_type = 'Plat Map';
UPDATE appraisal_documents SET document_type = 'PROPERTY_PHOTO' WHERE document_type = 'Property Photo';
UPDATE appraisal_documents SET document_type = 'TAX_RECORD' WHERE document_type = 'Tax Record';
UPDATE appraisal_documents SET document_type = 'OTHER' WHERE document_type = 'Other';

-- Change back to ENUM with correct values
ALTER TABLE appraisal_documents 
MODIFY COLUMN document_type ENUM(
    'TITLE_DEED',
    'FLOOR_PLAN', 
    'PLAT_MAP',
    'PROPERTY_PHOTO',
    'TAX_RECORD',
    'OTHER'
) NOT NULL;
```

### 2. **Check if Table Exists**

Run the following SQL script to create the table if it doesn't exist:
```bash
mysql -u quazisr -p actpro < src/Script/fix_appraisal_documents_table.sql
```

Or manually connect to MySQL and run:
```sql
USE actpro;

CREATE TABLE IF NOT EXISTS appraisal_documents (
    document_id CHAR(36) PRIMARY KEY,
    appraisal_id CHAR(36) NOT NULL,
    document_type ENUM('TITLE_DEED', 'FLOOR_PLAN', 'PLAT_MAP', 'PROPERTY_PHOTO', 'TAX_RECORD', 'OTHER'),
    file_name VARCHAR(255),
    file_url VARCHAR(512),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appraisal_id) REFERENCES appraisals(appraisal_id) ON DELETE CASCADE,
    INDEX idx_appraisal_documents_appraisal (appraisal_id),
    INDEX idx_appraisal_documents_type (document_type)
);

-- Verify table exists
SHOW TABLES LIKE 'appraisal_documents';

-- Check table structure
DESCRIBE appraisal_documents;
```

### 2. **Check Backend Logs**

When you upload a file, look for these log messages in your Spring Boot console:

✅ **Success indicators:**
```
Starting document upload
Appraisal ID: xxx
Filename: xxx
File uploaded successfully, URL: xxx
Document saved successfully with ID: xxx
Document upload completed
```

❌ **Error indicators:**
```
Appraisal not found or access denied
Access denied: You can only upload documents...
Error deleting file: xxx
```

### 3. **Check Browser Console**

Open DevTools (F12) → Console tab and look for:

✅ **Success:**
```
Uploading file 1/3: document.pdf Type: TITLE_DEED
Successfully uploaded: document.pdf
```

❌ **Errors:**
```
Failed to upload file: document.pdf Error: ...
```

### 4. **Check Network Tab**

In DevTools → Network tab:
1. Filter by "documents"
2. Look for POST requests to `/api/appraisals/{id}/documents`
3. Check status code:
   - ✅ **201** = Success
   - ❌ **400** = Bad request
   - ❌ **401** = Unauthorized
   - ❌ **403** = Forbidden
   - ❌ **500** = Server error

### 5. **Verify Database Records**

After uploading, check if records were created:
```sql
USE actpro;

-- Check recent uploads
SELECT * FROM appraisal_documents 
ORDER BY uploaded_at DESC 
LIMIT 10;

-- Check documents for specific appraisal
SELECT * FROM appraisal_documents 
WHERE appraisal_id = 'YOUR_APPRAISAL_ID';

-- Count documents per appraisal
SELECT appraisal_id, COUNT(*) as document_count 
FROM appraisal_documents 
GROUP BY appraisal_id;
```

### 6. **Verify Physical Files**

Check if files exist on disk:
```powershell
# Navigate to uploads directory
cd "C:\AI projects\valueProtect\uploads\appraisal-documents"

# List all appraisal directories
dir

# Check files in specific appraisal
dir {appraisal-id}
```

## 🐛 Common Issues and Solutions

### Issue 0: "Enum value mismatch" 🔥 **MOST COMMON**
**Symptoms:**
- Upload UI works but no data in database
- Backend logs might show: "Data truncation: Data truncated for column 'document_type'"
- Files exist on disk but no database records

**Cause:** Database enum has `'Title Deed'` but Java sends `'TITLE_DEED'`

**Solution:** Run the migration script:
```bash
mysql -u quazisr -p actpro < src/Script/fix_document_enum_values.sql
```

Then restart your Spring Boot application.

### Issue 1: "Table doesn't exist"
**Solution:** Run the SQL script from Step 1

### Issue 2: "Access denied"
**Solution:** Make sure you're logged in and the appraisal belongs to your company

### Issue 3: "File uploaded but not in database"
**Symptoms:** 
- Files exist in `uploads/` folder
- No records in `appraisal_documents` table

**Possible causes:**
- Transaction rollback due to error
- Database constraint violation
- Permission issues

**Check:**
```sql
-- Check if appraisal exists
SELECT * FROM appraisals WHERE appraisal_id = 'YOUR_APPRAISAL_ID';

-- Check foreign key constraints
SHOW CREATE TABLE appraisal_documents;
```

### Issue 4: "Upload progress shows but form closes immediately"
**Solution:** Check if there's a success callback that closes the form before uploads complete

### Issue 5: File size limit exceeded
**Error:** "File size cannot exceed 10MB"
**Solution:** Reduce file size or update limit in `application.properties`:
```properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

## 📊 Test Upload Flow

1. **Create an appraisal** first
2. **Note the appraisal ID** from the response
3. **Upload a small test file** (< 1MB)
4. **Check logs** for any errors
5. **Verify in database**:
   ```sql
   SELECT * FROM appraisal_documents WHERE appraisal_id = 'YOUR_ID';
   ```
6. **Check physical file**:
   ```
   uploads/appraisal-documents/{appraisal-id}/{filename}
   ```

## 🔧 Configuration Settings

Current configuration in `application.properties`:
```properties
# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.file.upload.directory=uploads/appraisal-documents
app.file.base.url=http://localhost:8080
```

## 📝 Supported File Types

- Images: JPG, JPEG, PNG, GIF
- Documents: PDF, DOC, DOCX
- Spreadsheets: XLS, XLSX

## 🆘 Still Not Working?

Enable DEBUG logging:
```properties
# Add to application.properties
logging.level.info.quazi.valueProtect.service.FileUploadService=DEBUG
logging.level.info.quazi.valueProtect.service.AppraisalService=DEBUG
```

Then restart your application and try uploading again. You'll see detailed logs about what's happening.
