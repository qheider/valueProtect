-- Appraisal System Database Schema
-- Creates tables for properties, property features, appraisals, and appraisal documents

-- Create properties table
CREATE TABLE IF NOT EXISTS properties (
    property_id CHAR(36) PRIMARY KEY,
    apn VARCHAR(50),
    address_line1 VARCHAR(255),
    city VARCHAR(100),
    state_province VARCHAR(50),
    zip_postal_code VARCHAR(20),
    property_type ENUM('SINGLE_FAMILY', 'CONDO', 'MULTI_FAMILY', 'COMMERCIAL', 'INDUSTRIAL'),
    year_built INT,
    lot_size_sqft DECIMAL(12,2),
    living_area_sqft DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_apn (apn),
    INDEX idx_address (address_line1, city, zip_postal_code),
    INDEX idx_property_type (property_type)
);

-- Create property_features table
CREATE TABLE IF NOT EXISTS property_features (
    feature_id CHAR(36) PRIMARY KEY,
    property_id CHAR(36) NOT NULL,
    bedroom_count INT,
    bathroom_count DECIMAL(3,1),
    basement_type VARCHAR(50),
    garage_spaces INT,
    hvac_type VARCHAR(100),
    exterior_material VARCHAR(100),
    condition_rating CHAR(2),
    quality_rating CHAR(2),
    FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE,
    INDEX idx_property_features_property (property_id)
);

-- Create appraisals table
CREATE TABLE IF NOT EXISTS appraisals (
    appraisal_id CHAR(36) PRIMARY KEY,
    property_id CHAR(36) NOT NULL,
    appraiser_id CHAR(36) NOT NULL,
    effective_date DATE,
    report_date DATE,
    appraised_value DECIMAL(15,2),
    purpose VARCHAR(100),
    status ENUM('DRAFT', 'REVIEW', 'COMPLETED', 'CANCELLED') DEFAULT 'DRAFT',
    final_report_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE,
    FOREIGN KEY (appraiser_id) REFERENCES employee(id) ON DELETE CASCADE,
    INDEX idx_appraisals_property (property_id),
    INDEX idx_appraisals_appraiser (appraiser_id),
    INDEX idx_appraisals_status (status),
    INDEX idx_appraisals_dates (effective_date, report_date)
);

-- Create appraisal_documents table
-- IMPORTANT: Enum values must match Java enum constants exactly (uppercase with underscores)
CREATE TABLE IF NOT EXISTS appraisal_documents (
    document_id CHAR(36) PRIMARY KEY,
    appraisal_id CHAR(36) NOT NULL,
    document_type ENUM('TITLE_DEED', 'FLOOR_PLAN', 'PLAT_MAP', 'PROPERTY_PHOTO', 'TAX_RECORD', 'OTHER') NOT NULL,
    file_name VARCHAR(255),
    file_url VARCHAR(512),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appraisal_id) REFERENCES appraisals(appraisal_id) ON DELETE CASCADE,
    INDEX idx_appraisal_documents_appraisal (appraisal_id),
    INDEX idx_appraisal_documents_type (document_type)
);

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_properties_location ON properties(city, state_province, zip_postal_code);
CREATE INDEX IF NOT EXISTS idx_appraisals_company_access ON appraisals(appraiser_id, property_id);
CREATE INDEX IF NOT EXISTS idx_appraisals_value ON appraisals(appraised_value);

-- Add constraints for data integrity
ALTER TABLE properties 
ADD CONSTRAINT chk_properties_year_built 
CHECK (year_built IS NULL OR (year_built > 1800 AND year_built <= YEAR(CURDATE())));

ALTER TABLE properties 
ADD CONSTRAINT chk_properties_lot_size 
CHECK (lot_size_sqft IS NULL OR lot_size_sqft > 0);

ALTER TABLE properties 
ADD CONSTRAINT chk_properties_living_area 
CHECK (living_area_sqft IS NULL OR living_area_sqft > 0);

ALTER TABLE property_features 
ADD CONSTRAINT chk_features_bedroom_count 
CHECK (bedroom_count IS NULL OR bedroom_count >= 0);

ALTER TABLE property_features 
ADD CONSTRAINT chk_features_bathroom_count 
CHECK (bathroom_count IS NULL OR bathroom_count >= 0);

ALTER TABLE property_features 
ADD CONSTRAINT chk_features_garage_spaces 
CHECK (garage_spaces IS NULL OR garage_spaces >= 0);

ALTER TABLE appraisals 
ADD CONSTRAINT chk_appraisals_value 
CHECK (appraised_value IS NULL OR appraised_value > 0);

ALTER TABLE appraisals 
ADD CONSTRAINT chk_appraisals_dates 
CHECK (report_date IS NULL OR effective_date IS NULL OR report_date >= effective_date);