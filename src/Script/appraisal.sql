CREATE TABLE properties (
    property_id CHAR(36) PRIMARY KEY,
    apn VARCHAR(50) UNIQUE NOT NULL, -- Assessor’s Parcel Number
    address_line1 VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(50) NOT NULL,
    zip_postal_code VARCHAR(20) NOT NULL,
    property_type ENUM('Single Family', 'Condo', 'Multi-Family', 'Commercial', 'Industrial') NOT NULL,
    year_built INT,
    lot_size_sqft DECIMAL(12, 2),
    living_area_sqft DECIMAL(12, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE property_features (
    feature_id CHAR(36) PRIMARY KEY,
    property_id CHAR(36) NOT NULL,
    bedroom_count INT,
    bathroom_count DECIMAL(3, 1), -- Supports half-baths like 2.5
    basement_type VARCHAR(50),
    garage_spaces INT,
    hvac_type VARCHAR(100),
    exterior_material VARCHAR(100),
    condition_rating CHAR(2), -- UAD Ratings: C1, C2, etc.
    quality_rating CHAR(2),    -- UAD Ratings: Q1, Q2, etc.
    FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE
);

CREATE TABLE appraisals (
    appraisal_id CHAR(36) PRIMARY KEY,
    property_id CHAR(36) NOT NULL,
    appraiser_id CHAR(36) NOT NULL,
    effective_date DATE NOT NULL,
    report_date DATE NOT NULL,
    appraised_value DECIMAL(15, 2),
    purpose VARCHAR(100),
    status ENUM('Draft', 'Review', 'Completed', 'Cancelled') DEFAULT 'Draft',
    -- Link to the final signed PDF/DOCX on your file server
    final_report_url VARCHAR(512), 
    FOREIGN KEY (property_id) REFERENCES properties(property_id) ON DELETE CASCADE
);

-- IMPORTANT: Enum values must match Java enum constants exactly (uppercase with underscores)
CREATE TABLE appraisal_documents (
    document_id CHAR(36) PRIMARY KEY,
    appraisal_id CHAR(36) NOT NULL,
    document_type ENUM('TITLE_DEED', 'FLOOR_PLAN', 'PLAT_MAP', 'PROPERTY_PHOTO', 'TAX_RECORD', 'OTHER') NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    -- The absolute URL or path to the file server
    file_url VARCHAR(512) NOT NULL, 
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (appraisal_id) REFERENCES appraisals(appraisal_id) ON DELETE CASCADE
);