-- Add required company_type column to company table
-- Persisted as STRING from CompanyType enum

USE actpro;

ALTER TABLE company
    ADD COLUMN company_type VARCHAR(20) NULL;

UPDATE company
SET company_type = 'APPRAISAL'
WHERE company_type IS NULL;

ALTER TABLE company
    MODIFY COLUMN company_type VARCHAR(20) NOT NULL;
