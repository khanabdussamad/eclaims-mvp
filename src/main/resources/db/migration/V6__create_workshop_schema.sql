-- V6__create_workshop_schema.sql

CREATE TABLE workshops (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    partner_code VARCHAR(64) NOT NULL UNIQUE,
    address_line1 VARCHAR(255) NOT NULL,
    city VARCHAR(128) NOT NULL,
    state VARCHAR(128) NOT NULL,
    zip_code VARCHAR(32) NOT NULL,
    phone VARCHAR(32),
    email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE workshop_claims (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    workshop_id UUID NOT NULL REFERENCES workshops(id),
    selected_by_customer_id UUID NOT NULL REFERENCES customers(id),
    selected_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(64) NOT NULL DEFAULT 'SELECTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE work_orders (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    workshop_id UUID NOT NULL REFERENCES workshops(id),
    estimate_amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    description TEXT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE repair_updates (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    workshop_id UUID NOT NULL REFERENCES workshops(id),
    repair_status VARCHAR(64) NOT NULL,
    progress_percentage INT NOT NULL,
    expected_delivery_date DATE,
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_repair_progress CHECK (progress_percentage >= 0 AND progress_percentage <= 100)
);

CREATE TABLE final_invoices (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL UNIQUE REFERENCES claims(id) ON DELETE CASCADE,
    workshop_id UUID NOT NULL REFERENCES workshops(id),
    invoice_number VARCHAR(128) NOT NULL,
    invoice_amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

