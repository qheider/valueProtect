-- Add lender preferred appraisal companies relationship and lender company reference on appraisals

USE actpro;

CREATE TABLE IF NOT EXISTS lender_preferred_appraisal (
    lender_id BIGINT NOT NULL,
    appraisal_company_id BIGINT NOT NULL,
    PRIMARY KEY (lender_id, appraisal_company_id),
    CONSTRAINT fk_lender_preferred_lender
        FOREIGN KEY (lender_id) REFERENCES company(id),
    CONSTRAINT fk_lender_preferred_appraisal
        FOREIGN KEY (appraisal_company_id) REFERENCES company(id)
);

ALTER TABLE appraisals
    ADD COLUMN lender_company_id BIGINT NULL;

ALTER TABLE appraisals
    ADD CONSTRAINT fk_appraisals_lender_company
        FOREIGN KEY (lender_company_id) REFERENCES company(id);
