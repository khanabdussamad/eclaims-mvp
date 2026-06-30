-- V9__create_reporting_indexes.sql

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

