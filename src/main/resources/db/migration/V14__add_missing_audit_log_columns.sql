-- V14__add_missing_audit_log_columns.sql

-- Add missing columns to audit_logs table
ALTER TABLE audit_logs
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS successful BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(500);

