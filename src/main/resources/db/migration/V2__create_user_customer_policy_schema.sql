-- V2__create_user_customer_policy_schema.sql

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

