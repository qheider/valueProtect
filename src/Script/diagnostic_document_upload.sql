-- Quick diagnostic script to check document upload configuration
-- Run this to verify your setup is correct

USE actpro;

SELECT '========================================' AS '';
SELECT 'DOCUMENT UPLOAD DIAGNOSTIC REPORT' AS '';
SELECT '========================================' AS '';

-- 1. Check if table exists
SELECT '1. Table Existence Check' AS '';
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN '✓ Table EXISTS'
        ELSE '✗ Table MISSING'
    END AS status
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'actpro' 
AND TABLE_NAME = 'appraisal_documents';

-- 2. Check table structure
SELECT '2. Table Structure' AS '';
DESCRIBE appraisal_documents;

-- 3. Check enum values (CRITICAL)
SELECT '3. ENUM Values Check' AS '';
SELECT 
    COLUMN_TYPE,
    CASE 
        WHEN COLUMN_TYPE LIKE '%TITLE_DEED%' THEN '✓ CORRECT - Has underscores (TITLE_DEED)'
        WHEN COLUMN_TYPE LIKE '%Title Deed%' THEN '✗ WRONG - Has spaces (Title Deed) - NEEDS FIX!'
        ELSE '? UNKNOWN FORMAT'
    END AS enum_status
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = 'actpro' 
AND TABLE_NAME = 'appraisal_documents' 
AND COLUMN_NAME = 'document_type';

-- 4. Check existing documents
SELECT '4. Existing Documents Count' AS '';
SELECT COUNT(*) AS total_documents FROM appraisal_documents;

-- 5. Recent uploads (if any)
SELECT '5. Recent Uploads (Last 5)' AS '';
SELECT 
    document_id,
    appraisal_id,
    document_type,
    file_name,
    uploaded_at
FROM appraisal_documents 
ORDER BY uploaded_at DESC 
LIMIT 5;

-- 6. Check foreign key relationships
SELECT '6. Foreign Key Constraints' AS '';
SELECT 
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'actpro'
AND TABLE_NAME = 'appraisal_documents'
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 7. Check available appraisals
SELECT '7. Available Appraisals (Last 5)' AS '';
SELECT 
    appraisal_id,
    purpose,
    status,
    created_at
FROM appraisals 
ORDER BY created_at DESC 
LIMIT 5;

-- 8. Documents per appraisal
SELECT '8. Documents Per Appraisal' AS '';
SELECT 
    a.appraisal_id,
    a.purpose,
    COUNT(d.document_id) AS document_count
FROM appraisals a
LEFT JOIN appraisal_documents d ON a.appraisal_id = d.appraisal_id
GROUP BY a.appraisal_id, a.purpose
HAVING COUNT(d.document_id) > 0
ORDER BY COUNT(d.document_id) DESC;

SELECT '========================================' AS '';
SELECT 'DIAGNOSTIC COMPLETE' AS '';
SELECT 'If enum status shows WRONG, run:' AS '';
SELECT 'mysql -u quazisr -p actpro < src/Script/fix_document_enum_values.sql' AS '';
SELECT '========================================' AS '';
