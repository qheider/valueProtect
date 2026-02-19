-- Script to ensure appraisal_documents table exists and has correct structure
-- Run this script if document uploads are not being saved

USE actpro;

-- Check and create appraisal_documents table if not exists
-- IMPORTANT: Enum values must match Java enum constants exactly (uppercase with underscores)
CREATE TABLE IF NOT EXISTS appraisal_documents (
    document_id CHAR(36) PRIMARY KEY,
    appraisal_id CHAR(36) NOT NULL,
    document_type ENUM('TITLE_DEED', 'FLOOR_PLAN', 'PLAT_MAP', 'PROPERTY_PHOTO', 'TAX_RECORD', 'OTHER') NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(512),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_appraisal_documents_appraisal (appraisal_id),
    INDEX idx_appraisal_documents_type (document_type)
);

-- Add foreign key constraint if it doesn't exist
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = 'actpro'
    AND TABLE_NAME = 'appraisal_documents'
    AND CONSTRAINT_NAME = 'appraisal_documents_ibfk_1'
);

SET @sql_add_fk = IF(@fk_exists = 0,
    'ALTER TABLE appraisal_documents ADD CONSTRAINT appraisal_documents_ibfk_1 FOREIGN KEY (appraisal_id) REFERENCES appraisals(appraisal_id) ON DELETE CASCADE',
    'SELECT "Foreign key already exists"'
);

PREPARE stmt FROM @sql_add_fk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify table structure
SELECT 'Table structure:' AS info;
DESCRIBE appraisal_documents;

-- Check if table has any data
SELECT 'Current document count:' AS info, COUNT(*) AS document_count FROM appraisal_documents;

-- Show recent appraisals (to verify we have appraisals to upload documents to)
SELECT 'Recent appraisals:' AS info;
SELECT appraisal_id, purpose, status, created_at 
FROM appraisals 
ORDER BY created_at DESC 
LIMIT 5;
