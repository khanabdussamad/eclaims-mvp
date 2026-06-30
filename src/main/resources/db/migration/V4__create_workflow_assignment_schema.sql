-- V4__create_workflow_assignment_schema.sql

CREATE TABLE workflow_transitions (
    id UUID PRIMARY KEY,
    from_status VARCHAR(64) NOT NULL,
    to_status VARCHAR(64) NOT NULL,
    required_permission VARCHAR(128) NOT NULL,
    actor_role VARCHAR(64),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_workflow_transition UNIQUE(from_status, to_status, required_permission)
);

CREATE TABLE regions (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_areas (
    id UUID PRIMARY KEY,
    region_id UUID NOT NULL REFERENCES regions(id),
    zip_code VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_service_area_zip UNIQUE(zip_code)
);

CREATE TABLE claim_assignments (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    assigned_user_id UUID NOT NULL REFERENCES users(id),
    assignment_role VARCHAR(64) NOT NULL,
    assigned_by_user_id UUID REFERENCES users(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assignment_role CHECK (assignment_role IN ('CASE_MANAGER', 'SURVEYOR', 'ADJUSTOR'))
);

CREATE UNIQUE INDEX uq_active_claim_assignment_role
ON claim_assignments(claim_id, assignment_role)
WHERE active = TRUE;

