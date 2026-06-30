
# eClaims Backend — Full Database DDL and Flyway Migration Specification

**Database:** PostgreSQL  
**Migration Tool:** Flyway  
**Schema Principle:** All backend DB changes must be implemented using Flyway migrations only.  

---

## 1. Migration File Order

```text
V1__create_rbac_schema.sql
V2__create_user_customer_policy_schema.sql
V3__create_claim_schema.sql
V4__create_workflow_assignment_schema.sql
V5__create_survey_adjudication_schema.sql
V6__create_workshop_schema.sql
V7__create_document_schema.sql
V8__create_notification_payment_audit_schema.sql
V9__create_reporting_indexes.sql
V10__seed_reference_data.sql
V11__seed_demo_users.sql
```

---

# V1__create_rbac_schema.sql

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
```

---

# V2__create_user_customer_policy_schema.sql

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE customers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    customer_number VARCHAR(64) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(32),
    address_line1 VARCHAR(255),
    city VARCHAR(128),
    state VARCHAR(128),
    zip_code VARCHAR(32),
    country VARCHAR(128),
    billing_cycle VARCHAR(32) NOT NULL DEFAULT 'MONTHLY',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE policies (
    id UUID PRIMARY KEY,
    policy_number VARCHAR(64) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id),
    vehicle_vin VARCHAR(64) NOT NULL,
    vehicle_make VARCHAR(100) NOT NULL,
    vehicle_model VARCHAR(100) NOT NULL,
    vehicle_year INT NOT NULL,
    policy_status VARCHAR(32) NOT NULL,
    effective_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    deductible_amount NUMERIC(14,2) NOT NULL,
    coverage_limit NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_policy_status CHECK (policy_status IN ('ACTIVE', 'INACTIVE', 'EXPIRED', 'CANCELLED'))
);

CREATE TABLE policy_coverages (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL REFERENCES policies(id) ON DELETE CASCADE,
    coverage_type VARCHAR(64) NOT NULL,
    coverage_limit NUMERIC(14,2) NOT NULL,
    deductible_amount NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

# V3__create_claim_schema.sql

```sql
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
```

---

# V4__create_workflow_assignment_schema.sql

```sql
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
```

---

# V5__create_survey_adjudication_schema.sql

```sql
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
```

---

# V6__create_workshop_schema.sql

```sql
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
```

---

# V7__create_document_schema.sql

```sql
CREATE TABLE documents (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    uploaded_by_user_id UUID NOT NULL REFERENCES users(id),
    document_type VARCHAR(64) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_provider VARCHAR(32) NOT NULL,
    storage_bucket VARCHAR(128) NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    checksum VARCHAR(128),
    version INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE document_versions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version INT NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    uploaded_by_user_id UUID NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_document_version UNIQUE(document_id, version)
);
```

---

# V8__create_notification_payment_audit_schema.sql

```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL REFERENCES users(id),
    channel VARCHAR(32) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    related_entity_type VARCHAR(64),
    related_entity_id UUID,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(128) NOT NULL,
    payload_json JSONB NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL REFERENCES claims(id) ON DELETE CASCADE,
    payer_customer_id UUID NOT NULL REFERENCES customers(id),
    amount NUMERIC(14,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(32) NOT NULL,
    payment_reference VARCHAR(128),
    initiated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id),
    actor_role VARCHAR(64),
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id UUID,
    old_value_json JSONB,
    new_value_json JSONB,
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

# V9__create_reporting_indexes.sql

```sql
CREATE INDEX idx_claims_policy_id ON claims(policy_id);
CREATE INDEX idx_claims_customer_id ON claims(customer_id);
CREATE INDEX idx_claims_status ON claims(current_status);
CREATE INDEX idx_claims_zip ON claims(incident_zip_code);
CREATE INDEX idx_claims_submitted_at ON claims(submitted_at);
CREATE INDEX idx_claims_closed_at ON claims(closed_at);

CREATE INDEX idx_claim_status_history_claim ON claim_status_history(claim_id);
CREATE INDEX idx_claim_status_history_created ON claim_status_history(created_at);

CREATE INDEX idx_claim_assignments_claim ON claim_assignments(claim_id);
CREATE INDEX idx_claim_assignments_user ON claim_assignments(assigned_user_id);
CREATE INDEX idx_claim_assignments_role ON claim_assignments(assignment_role);

CREATE INDEX idx_documents_claim ON documents(claim_id);
CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_actor ON audit_logs(actor_user_id);
CREATE INDEX idx_notification_events_status ON notification_events(status);
```

---

# V10__seed_reference_data.sql

```sql
-- Roles
INSERT INTO roles (id, code, name, description) VALUES
('00000000-0000-0000-0000-000000000001', 'CUSTOMER', 'Customer', 'Insurance customer'),
('00000000-0000-0000-0000-000000000002', 'CASE_MANAGER', 'Case Manager', 'Internal case manager'),
('00000000-0000-0000-0000-000000000003', 'SURVEYOR', 'Surveyor', 'Damage surveyor'),
('00000000-0000-0000-0000-000000000004', 'ADJUSTOR', 'Adjustor', 'Claim adjustor'),
('00000000-0000-0000-0000-000000000005', 'WORKSHOP_USER', 'Workshop User', 'Partner workshop user'),
('00000000-0000-0000-0000-000000000006', 'AUDITOR', 'Auditor', 'Audit user'),
('00000000-0000-0000-0000-000000000007', 'REGIONAL_MANAGER', 'Regional Manager', 'Regional reporting user'),
('00000000-0000-0000-0000-000000000008', 'ADMIN', 'Admin', 'System administrator');

-- Permissions
INSERT INTO permissions (id, code, name, description) VALUES
('10000000-0000-0000-0000-000000000001', 'CLAIM_CREATE', 'Create Claim', 'Create new claim'),
('10000000-0000-0000-0000-000000000002', 'CLAIM_VIEW_SELF', 'View Own Claims', 'View own customer claims'),
('10000000-0000-0000-0000-000000000003', 'CLAIM_VIEW_ASSIGNED', 'View Assigned Claims', 'View claims assigned to user'),
('10000000-0000-0000-0000-000000000004', 'CLAIM_VIEW_ALL', 'View All Claims', 'View all claims'),
('10000000-0000-0000-0000-000000000005', 'CLAIM_ASSIGN', 'Assign Claim', 'Assign claim'),
('10000000-0000-0000-0000-000000000006', 'CLAIM_REASSIGN', 'Reassign Claim', 'Reassign claim'),
('10000000-0000-0000-0000-000000000007', 'CLAIM_STATUS_UPDATE', 'Update Claim Status', 'Update claim status'),
('10000000-0000-0000-0000-000000000008', 'SURVEY_VIEW_ASSIGNED', 'View Survey Assigned', 'View assigned survey claims'),
('10000000-0000-0000-0000-000000000009', 'SURVEY_SUBMIT', 'Submit Survey', 'Submit survey report'),
('10000000-0000-0000-0000-000000000010', 'ADJUDICATION_VIEW_ASSIGNED', 'View Adjudication Assigned', 'View assigned adjudication claims'),
('10000000-0000-0000-0000-000000000011', 'ADJUDICATION_APPROVE', 'Approve Claim', 'Approve claim'),
('10000000-0000-0000-0000-000000000012', 'ADJUDICATION_REJECT', 'Reject Claim', 'Reject claim'),
('10000000-0000-0000-0000-000000000013', 'WORKSHOP_SELECT', 'Select Workshop', 'Select workshop'),
('10000000-0000-0000-0000-000000000014', 'WORKSHOP_VIEW_ASSIGNED', 'View Workshop Claims', 'View workshop assigned claims'),
('10000000-0000-0000-0000-000000000015', 'WORKSHOP_UPDATE', 'Update Workshop Claim', 'Update repair status'),
('10000000-0000-0000-0000-000000000016', 'DOCUMENT_UPLOAD', 'Upload Document', 'Upload document'),
('10000000-0000-0000-0000-000000000017', 'DOCUMENT_VIEW', 'View Document', 'View/download document'),
('10000000-0000-0000-0000-000000000018', 'PAYMENT_INITIATE', 'Initiate Payment', 'Initiate payment'),
('10000000-0000-0000-0000-000000000019', 'PAYMENT_COMPLETE', 'Complete Payment', 'Complete payment'),
('10000000-0000-0000-0000-000000000020', 'NOTIFICATION_VIEW', 'View Notifications', 'View notifications'),
('10000000-0000-0000-0000-000000000021', 'REPORT_VIEW', 'View Reports', 'View reports'),
('10000000-0000-0000-0000-000000000022', 'AUDIT_VIEW', 'View Audit', 'View audit logs'),
('10000000-0000-0000-0000-000000000023', 'ADMIN_MANAGE_USERS', 'Manage Users', 'Manage users'),
('10000000-0000-0000-0000-000000000024', 'ADMIN_MANAGE_ROLES', 'Manage Roles', 'Manage roles');

-- Region and service area
INSERT INTO regions (id, code, name) VALUES
('20000000-0000-0000-0000-000000000001', 'NE', 'Northeast'),
('20000000-0000-0000-0000-000000000002', 'MW', 'Midwest'),
('20000000-0000-0000-0000-000000000003', 'SO', 'South'),
('20000000-0000-0000-0000-000000000004', 'WE', 'West');

INSERT INTO service_areas (id, region_id, zip_code) VALUES
('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '10001'),
('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '10002');

-- Workshops
INSERT INTO workshops (id, name, partner_code, address_line1, city, state, zip_code, phone, email) VALUES
('30000000-0000-0000-0000-000000000001', 'Partner Auto Repair NYC', 'WS-NYC-001', '100 Repair Ave', 'New York', 'NY', '10001', '+12025550888', 'nyc-workshop@nagarro.com'),
('30000000-0000-0000-0000-000000000002', 'FastFix Collision Center', 'WS-NYC-002', '200 Service Road', 'New York', 'NY', '10002', '+12025550999', 'fastfix@nagarro.com');
```

---

# V11__seed_demo_users.sql

```sql
-- NOTE: Replace password_hash values with BCrypt hash for Password@123 generated by application or migration utility.

INSERT INTO users (id, full_name, email, password_hash, status) VALUES
('40000000-0000-0000-0000-000000000001', 'John Customer', 'customer@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE'),
('40000000-0000-0000-0000-000000000002', 'Case Manager One', 'case.manager@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE'),
('40000000-0000-0000-0000-000000000003', 'Surveyor One', 'surveyor@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE'),
('40000000-0000-0000-0000-000000000004', 'Adjustor One', 'adjustor@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE'),
('40000000-0000-0000-0000-000000000005', 'Workshop User One', 'workshop@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE'),
('40000000-0000-0000-0000-000000000006', 'Auditor One', 'auditor@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE'),
('40000000-0000-0000-0000-000000000007', 'Regional Manager One', 'regional.manager@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE'),
('40000000-0000-0000-0000-000000000008', 'Admin One', 'admin@nagarro.com', '$2a$10$REPLACE_WITH_BCRYPT_HASH', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id) VALUES
('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001'),
('40000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002'),
('40000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003'),
('40000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000004'),
('40000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000005'),
('40000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000006'),
('40000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000007'),
('40000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000008');

INSERT INTO customers (id, user_id, customer_number, first_name, last_name, phone, address_line1, city, state, zip_code, country, billing_cycle) VALUES
('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', 'CUST-10001', 'John', 'Customer', '+12025550100', '10 Main Street', 'New York', 'NY', '10001', 'USA', 'MONTHLY');

INSERT INTO policies (id, policy_number, customer_id, vehicle_vin, vehicle_make, vehicle_model, vehicle_year, policy_status, effective_date, expiry_date, deductible_amount, coverage_limit) VALUES
('60000000-0000-0000-0000-000000000001', 'POL-1000001', '50000000-0000-0000-0000-000000000001', '1HGCM82633A004352', 'Toyota', 'Camry', 2022, 'ACTIVE', '2026-01-01', '2026-12-31', 500.00, 25000.00);

INSERT INTO policy_coverages (id, policy_id, coverage_type, coverage_limit, deductible_amount) VALUES
('61000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'COLLISION', 25000.00, 500.00),
('61000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000001', 'COMPREHENSIVE', 15000.00, 500.00);
```

---

## 2. Post-Migration Implementation Notes

- Use application startup or an additional seed migration to insert `role_permissions` mappings.
- For BCrypt hashes, generate actual hashes through a one-time utility or replace in SQL before running.
- Add workflow transition seed records for every transition defined in the workflow matrix.
- Keep enum values aligned between Java enums and database check constraints.
