-- V16__add_missing_document_version_columns.sql

-- Add missing BaseEntity columns to document_versions table
ALTER TABLE document_versions
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

