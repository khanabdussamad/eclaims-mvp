-- V3__create_claim_schema.sql

CREATE TABLE claims (
    id UUID PRIMARY KEY,
    claim_number VARCHAR(64) NOT NULL UNIQUE,
    policy_id UUID NOT NULL REFERENCES policies(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    claim_type VARCHAR(32) NOT NULL,
    current_status VARCHAR(64) NOT NULL,
    incident_date DATE NOT NULL,
    incident_time TIME NOT NULL,
    incident_address_line1 VARCHAR(255) NOT NULL,
    incident_city VARCHAR(128) NOT NULL,
    incident_state VARCHAR(128) NOT NULL,
    incident_zip_code VARCHAR(32) NOT NULL,
    incident_country VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    vehicle_drivable BOOLEAN NOT NULL,
    police_report_available BOOLEAN NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_claim_type CHECK (claim_type IN ('ACCIDENT', 'THEFT', 'FIRE', 'FLOOD', 'VANDALISM', 'OTHER'))
);

CREATE TABLE claim_status_history (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    from_status VARCHAR(64),
    to_status VARCHAR(64) NOT NULL,
    changed_by_user_id UUID NOT NULL REFERENCES users(id),
    changed_by_role VARCHAR(64),
    reason TEXT,
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

