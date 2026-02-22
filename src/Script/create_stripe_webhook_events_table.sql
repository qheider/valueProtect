-- Create table for persistent Stripe webhook idempotency tracking
-- Run this script in the same database used by the application

USE actpro;

CREATE TABLE IF NOT EXISTS stripe_webhook_events (
    event_id VARCHAR(255) PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    appraisal_id CHAR(36) NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stripe_webhook_events_appraisal (appraisal_id),
    INDEX idx_stripe_webhook_events_processed_at (processed_at)
);

SELECT 'stripe_webhook_events table is ready' AS status;
