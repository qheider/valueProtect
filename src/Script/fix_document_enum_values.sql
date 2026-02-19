-- Fix enum values in appraisal_documents table to match Java enum constants
-- Current: 'Title Deed', 'Floor Plan', etc. (with spaces)
-- Required: 'TITLE_DEED', 'FLOOR_PLAN', etc. (underscores, uppercase)

USE actpro;

-- Step 1: Check current data (if any)
SELECT 'Existing documents before migration:' AS info;
SELECT document_id, appraisal_id, document_type, file_name 
FROM appraisal_documents
LIMIT 10;

-- Step 2: Backup the table (optional but recommended)
-- CREATE TABLE appraisal_documents_backup AS SELECT * FROM appraisal_documents;

-- Step 3: Modify the enum column to accept the new values
-- First, change to VARCHAR to allow any value temporarily
ALTER TABLE appraisal_documents 
MODIFY COLUMN document_type VARCHAR(50);

-- Step 4: Update existing data (if any) to match new enum values
UPDATE appraisal_documents SET document_type = 'TITLE_DEED' WHERE document_type = 'Title Deed';
UPDATE appraisal_documents SET document_type = 'FLOOR_PLAN' WHERE document_type = 'Floor Plan';
UPDATE appraisal_documents SET document_type = 'PLAT_MAP' WHERE document_type = 'Plat Map';
UPDATE appraisal_documents SET document_type = 'PROPERTY_PHOTO' WHERE document_type = 'Property Photo';
UPDATE appraisal_documents SET document_type = 'TAX_RECORD' WHERE document_type = 'Tax Record';
UPDATE appraisal_documents SET document_type = 'OTHER' WHERE document_type = 'Other';

-- Step 5: Change back to ENUM with correct values
ALTER TABLE appraisal_documents 
MODIFY COLUMN document_type ENUM(
    'TITLE_DEED',
    'FLOOR_PLAN', 
    'PLAT_MAP',
    'PROPERTY_PHOTO',
    'TAX_RECORD',
    'OTHER'
) NOT NULL;

-- Step 6: Verify the change
SELECT 'Updated table structure:' AS info;
SHOW COLUMNS FROM appraisal_documents LIKE 'document_type';

SELECT 'Existing documents after migration:' AS info;
SELECT document_id, appraisal_id, document_type, file_name 
FROM appraisal_documents
LIMIT 10;

SELECT 'Fix completed successfully!' AS status;
