-- V12__add_missing_adjustor_decision_columns.sql

ALTER TABLE adjustor_decisions
ADD COLUMN IF NOT EXISTS rationale TEXT,
ADD COLUMN IF NOT EXISTS denial_reason VARCHAR(500),
ADD COLUMN IF NOT EXISTS decision_date TIMESTAMP,
ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS coverage_limit NUMERIC(14,2),
ADD COLUMN IF NOT EXISTS deductible_amount NUMERIC(14,2);

