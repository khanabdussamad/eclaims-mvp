-- V15__add_missing_claim_assignment_columns.sql

-- Add missing columns to claim_assignments table to match JPA entity
ALTER TABLE claim_assignments
ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS completion_notes VARCHAR(500),
ADD COLUMN IF NOT EXISTS sequence_number INTEGER;

-- Rename 'active' column to 'is_active' to match JPA entity naming
ALTER TABLE claim_assignments
RENAME COLUMN active TO is_active;

-- Rename 'released_at' and related columns for consistency
ALTER TABLE claim_assignments
RENAME COLUMN released_at TO completed_at_old;

-- Drop the old completed_at_old if it's not needed, or keep for data migration
ALTER TABLE claim_assignments
DROP COLUMN IF EXISTS completed_at_old;

DROP INDEX IF EXISTS uq_active_claim_assignment_role;

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_claim_assignment_role
ON claim_assignments(claim_id, assignment_role)
WHERE is_active = TRUE;

