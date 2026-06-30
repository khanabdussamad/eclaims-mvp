-- V5__create_survey_adjudication_schema.sql

CREATE TABLE survey_reports (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    surveyor_user_id UUID NOT NULL REFERENCES users(id),
    damage_severity VARCHAR(32) NOT NULL,
    assessment_summary TEXT NOT NULL,
    estimated_repair_amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    recommendation VARCHAR(255) NOT NULL,
    remarks TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_damage_severity CHECK (damage_severity IN ('LOW', 'MEDIUM', 'HIGH', 'TOTAL_LOSS'))
);

CREATE TABLE adjustor_decisions (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    adjustor_user_id UUID NOT NULL REFERENCES users(id),
    decision VARCHAR(32) NOT NULL,
    approved_amount NUMERIC(14,2),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    remarks TEXT,
    decided_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_adjustor_decision CHECK (decision IN ('APPROVED', 'REJECTED'))
);

