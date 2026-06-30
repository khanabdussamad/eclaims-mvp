
# eClaims Backend — RBAC API Matrix and Workflow Transition Matrix

---

# Part 1 — RBAC API Matrix

## 1. Permission Principles

- Access control is permission-first, role-seeded.
- Roles map to permissions through `role_permissions`.
- APIs must use method-level authorization such as `@PreAuthorize`.
- Ownership rules must be enforced in service layer, not only controller layer.
- Auditor and reporting users must be read-only unless explicitly granted write permissions.

---

## 2. Endpoint to Permission Matrix

| Module | Endpoint | Method | Permission | Roles | Ownership / Business Rule |
|---|---|---:|---|---|---|
| Auth | `/api/v1/auth/login` | POST | Public | All | Valid active user required |
| Auth | `/api/v1/auth/register-customer` | POST | Public | Customer prospect | Policy must exist and match supplied identity |
| Auth | `/api/v1/auth/me` | GET | Authenticated | All | Own profile only |
| Customer | `/api/v1/customers/me` | GET | CUSTOMER role | CUSTOMER | Own customer profile only |
| Customer | `/api/v1/customers/me/correspondence-address` | PATCH | CUSTOMER role | CUSTOMER | Own profile only |
| Customer | `/api/v1/customers/me/billing-cycle` | PATCH | CUSTOMER role | CUSTOMER | Own profile only |
| Policy | `/api/v1/policies/me` | GET | CUSTOMER role | CUSTOMER | Own policies only |
| Policy | `/api/v1/policies/{policyNumber}` | GET | CLAIM_VIEW_SELF / CLAIM_VIEW_ALL | CUSTOMER, internal | Customer only own policy; internal users all if authorized |
| Claim | `/api/v1/claims` | POST | CLAIM_CREATE | CUSTOMER | Own active policy only |
| Claim | `/api/v1/claims` | GET | CLAIM_VIEW_SELF / CLAIM_VIEW_ASSIGNED / CLAIM_VIEW_ALL | CUSTOMER, CASE_MANAGER, SURVEYOR, ADJUSTOR, AUDITOR, REGIONAL_MANAGER | Filter by access scope |
| Claim | `/api/v1/claims/{claimId}` | GET | CLAIM_VIEW_SELF / CLAIM_VIEW_ASSIGNED / CLAIM_VIEW_ALL | All authorized roles | Access service validates scope |
| Claim | `/api/v1/claims/{claimId}/timeline` | GET | CLAIM_VIEW_SELF / CLAIM_VIEW_ASSIGNED / CLAIM_VIEW_ALL | All authorized roles | Access service validates scope |
| Claim | `/api/v1/claims/{claimId}/status` | PATCH | CLAIM_STATUS_UPDATE or transition permission | CASE_MANAGER, SURVEYOR, ADJUSTOR, WORKSHOP_USER, CUSTOMER | Workflow service validates transition |
| Assignment | `/api/v1/claims/{claimId}/assignments/auto` | POST | CLAIM_ASSIGN | CASE_MANAGER, ADMIN | Claim must be submitted or assignment-eligible |
| Assignment | `/api/v1/claims/{claimId}/assignments/reassign` | POST | CLAIM_REASSIGN | CASE_MANAGER, ADMIN | Old assignment deactivated, new active assignment created |
| Assignment | `/api/v1/claims/{claimId}/assignments` | GET | CLAIM_VIEW_ASSIGNED / CLAIM_VIEW_ALL | Internal roles | Access service validates scope |
| Survey | `/api/v1/surveyor/claims` | GET | SURVEY_VIEW_ASSIGNED | SURVEYOR | Only assigned claims |
| Survey | `/api/v1/claims/{claimId}/survey-reports` | POST | SURVEY_SUBMIT | SURVEYOR | Surveyor must be assigned to claim |
| Survey | `/api/v1/claims/{claimId}/survey-reports` | GET | CLAIM_VIEW_ASSIGNED / CLAIM_VIEW_ALL | SURVEYOR, ADJUSTOR, CASE_MANAGER, AUDITOR | Access service validates scope |
| Adjudication | `/api/v1/adjustor/claims` | GET | ADJUDICATION_VIEW_ASSIGNED | ADJUSTOR | Only assigned claims |
| Adjudication | `/api/v1/claims/{claimId}/adjudications` | POST | ADJUDICATION_APPROVE or ADJUDICATION_REJECT | ADJUSTOR | Adjustor must be assigned; survey report required |
| Workshop | `/api/v1/workshops/search` | GET | Authenticated | CUSTOMER, CASE_MANAGER | Can search active partner workshops |
| Workshop | `/api/v1/claims/{claimId}/workshop-selection` | POST | WORKSHOP_SELECT | CUSTOMER | Customer must own approved claim |
| Workshop | `/api/v1/workshop/claims/{claimId}/work-orders` | POST | WORKSHOP_UPDATE | WORKSHOP_USER | Claim must be selected for user's workshop |
| Workshop | `/api/v1/workshop/claims/{claimId}/repair-updates` | POST | WORKSHOP_UPDATE | WORKSHOP_USER | Claim must be selected for user's workshop |
| Workshop | `/api/v1/workshop/claims/{claimId}/final-invoice` | POST | WORKSHOP_UPDATE | WORKSHOP_USER | Final invoice should be submitted once |
| Document | `/api/v1/claims/{claimId}/documents` | POST | DOCUMENT_UPLOAD | CUSTOMER, SURVEYOR, WORKSHOP_USER | Access scope and allowed document type validated |
| Document | `/api/v1/claims/{claimId}/documents` | GET | DOCUMENT_VIEW | Authorized roles | Access service validates claim scope |
| Document | `/api/v1/documents/{documentId}/download` | GET | DOCUMENT_VIEW | Authorized roles | Access service validates claim scope |
| Document | `/api/v1/documents/{documentId}` | DELETE | DOCUMENT_DELETE | ADMIN, uploader if allowed | Soft-delete recommended |
| Notification | `/api/v1/notifications` | GET | NOTIFICATION_VIEW | All roles | Own notifications only |
| Notification | `/api/v1/notifications/{id}/read` | PATCH | NOTIFICATION_VIEW | All roles | Own notification only |
| Payment | `/api/v1/claims/{claimId}/payments/initiate` | POST | PAYMENT_INITIATE | CUSTOMER | Own claim and payment pending |
| Payment | `/api/v1/claims/{claimId}/payments/mock-complete` | POST | PAYMENT_COMPLETE | CUSTOMER, ADMIN | Own payment for MVP |
| Reporting | `/api/v1/reports/*` | GET | REPORT_VIEW | CASE_MANAGER, AUDITOR, REGIONAL_MANAGER, ADMIN | Region filtering for regional manager |
| Audit | `/api/v1/audit/claims/{claimId}` | GET | AUDIT_VIEW | AUDITOR, ADMIN | Read-only |
| Admin | `/api/v1/admin/users` | GET | ADMIN_MANAGE_USERS | ADMIN | Admin only |
| Admin | `/api/v1/admin/roles` | GET | ADMIN_MANAGE_ROLES | ADMIN | Admin only |
| Admin | `/api/v1/admin/permissions` | GET | ADMIN_MANAGE_ROLES | ADMIN | Admin only |

---

## 3. Role to Permission Matrix

| Role | Permissions |
|---|---|
| CUSTOMER | CLAIM_CREATE, CLAIM_VIEW_SELF, WORKSHOP_SELECT, DOCUMENT_UPLOAD, DOCUMENT_VIEW, PAYMENT_INITIATE, PAYMENT_COMPLETE, NOTIFICATION_VIEW |
| CASE_MANAGER | CLAIM_VIEW_ASSIGNED, CLAIM_VIEW_ALL, CLAIM_ASSIGN, CLAIM_REASSIGN, CLAIM_STATUS_UPDATE, DOCUMENT_VIEW, REPORT_VIEW |
| SURVEYOR | CLAIM_VIEW_ASSIGNED, SURVEY_VIEW_ASSIGNED, SURVEY_SUBMIT, DOCUMENT_UPLOAD, DOCUMENT_VIEW |
| ADJUSTOR | CLAIM_VIEW_ASSIGNED, ADJUDICATION_VIEW_ASSIGNED, ADJUDICATION_APPROVE, ADJUDICATION_REJECT, DOCUMENT_VIEW |
| WORKSHOP_USER | WORKSHOP_VIEW_ASSIGNED, WORKSHOP_UPDATE, DOCUMENT_UPLOAD, DOCUMENT_VIEW |
| AUDITOR | CLAIM_VIEW_ALL, DOCUMENT_VIEW, AUDIT_VIEW, REPORT_VIEW |
| REGIONAL_MANAGER | CLAIM_VIEW_ALL, REPORT_VIEW |
| ADMIN | All permissions |

---

# Part 2 — Workflow Transition Matrix

## 4. Workflow Principles

- Every claim state change goes through `ClaimWorkflowService`.
- Transitions are DB-configurable through `workflow_transitions`.
- Domain preconditions are validated in code.
- Every transition writes `claim_status_history`.
- Every transition writes `audit_logs`.
- Key transitions publish notification events.

---

## 5. Transition Matrix

| From Status | To Status | Actor | Permission | Preconditions | Event | Notification Recipients |
|---|---|---|---|---|---|---|
| null | SUBMITTED | CUSTOMER | CLAIM_CREATE | Active policy, policy owned by customer | CLAIM_SUBMITTED | Customer, case manager pool |
| SUBMITTED | CASE_ASSIGNED | CASE_MANAGER | CLAIM_ASSIGN | Case manager assignment exists | CASE_ASSIGNED | Customer, case manager |
| CASE_ASSIGNED | SURVEYOR_ASSIGNED | CASE_MANAGER | CLAIM_ASSIGN | Surveyor assignment exists | SURVEYOR_ASSIGNED | Customer, surveyor |
| SURVEYOR_ASSIGNED | SURVEY_IN_PROGRESS | SURVEYOR | SURVEY_SUBMIT | Surveyor assigned to claim | SURVEY_STARTED | Customer, case manager |
| SURVEY_IN_PROGRESS | SURVEY_SUBMITTED | SURVEYOR | SURVEY_SUBMIT | Survey report saved | SURVEY_SUBMITTED | Adjustor, case manager |
| SURVEY_SUBMITTED | ADJUSTOR_REVIEW | SYSTEM/ADJUSTOR | ADJUDICATION_VIEW_ASSIGNED | Adjustor assigned, survey report exists | ADJUSTOR_REVIEW_STARTED | Adjustor |
| ADJUSTOR_REVIEW | APPROVED | ADJUSTOR | ADJUDICATION_APPROVE | Approved amount present, coverage validated | CLAIM_APPROVED | Customer, case manager |
| ADJUSTOR_REVIEW | REJECTED | ADJUSTOR | ADJUDICATION_REJECT | Rejection remarks present | CLAIM_REJECTED | Customer, case manager |
| APPROVED | WORKSHOP_SELECTED | CUSTOMER | WORKSHOP_SELECT | Customer owns claim, workshop active | WORKSHOP_SELECTED | Workshop, customer, case manager |
| WORKSHOP_SELECTED | REPAIR_IN_PROGRESS | WORKSHOP_USER | WORKSHOP_UPDATE | Work order exists | REPAIR_STATUS_UPDATED | Customer, case manager |
| REPAIR_IN_PROGRESS | REPAIR_COMPLETED | WORKSHOP_USER | WORKSHOP_UPDATE | Repair progress 100 or status completed | REPAIR_COMPLETED | Customer, case manager |
| REPAIR_COMPLETED | PAYMENT_PENDING | WORKSHOP_USER/SYSTEM | WORKSHOP_UPDATE | Final invoice exists | PAYMENT_PENDING | Customer |
| PAYMENT_PENDING | PAYMENT_COMPLETED | CUSTOMER | PAYMENT_COMPLETE | Payment record completed | PAYMENT_COMPLETED | Customer, workshop, case manager |
| PAYMENT_COMPLETED | CLOSED | CASE_MANAGER/SYSTEM | CLAIM_STATUS_UPDATE | Payment completed and no pending tasks | CLAIM_CLOSED | Customer, case manager |
| Any non-final | CANCELLED | CASE_MANAGER/ADMIN | CLAIM_STATUS_UPDATE | Cancellation reason required | CLAIM_CANCELLED | Customer, assigned users |

---

## 6. Workflow Domain Preconditions

### 6.1 Claim Submission

```text
- Policy exists.
- Policy is ACTIVE.
- Customer owns policy.
- Incident date is not future.
- Required accident details are present.
```

### 6.2 Assignment

```text
- CASE_ASSIGNED requires active CASE_MANAGER assignment.
- SURVEYOR_ASSIGNED requires active SURVEYOR assignment.
- ADJUSTOR_REVIEW requires active ADJUSTOR assignment.
```

### 6.3 Survey

```text
- Surveyor must be assigned.
- Survey report must be created before SURVEY_SUBMITTED.
- Estimated repair amount must be >= 0.
```

### 6.4 Adjudication

```text
- Adjustor must be assigned.
- Survey report must exist.
- Approved claim requires approved amount.
- Approved amount should not exceed coverage unless override is implemented.
- Rejected claim requires remarks.
```

### 6.5 Workshop

```text
- Workshop can be selected only after APPROVED.
- Workshop must be active.
- Repair update allowed only for selected workshop.
- Final invoice required before PAYMENT_PENDING.
```

### 6.6 Payment and Closure

```text
- Payment initiation requires PAYMENT_PENDING.
- Payment completion requires initiated payment.
- Closure requires PAYMENT_COMPLETED.
```

---

## 7. Workflow Seed SQL

```sql
INSERT INTO workflow_transitions (id, from_status, to_status, required_permission, actor_role) VALUES
('70000000-0000-0000-0000-000000000001', 'SUBMITTED', 'CASE_ASSIGNED', 'CLAIM_ASSIGN', 'CASE_MANAGER'),
('70000000-0000-0000-0000-000000000002', 'CASE_ASSIGNED', 'SURVEYOR_ASSIGNED', 'CLAIM_ASSIGN', 'CASE_MANAGER'),
('70000000-0000-0000-0000-000000000003', 'SURVEYOR_ASSIGNED', 'SURVEY_IN_PROGRESS', 'SURVEY_SUBMIT', 'SURVEYOR'),
('70000000-0000-0000-0000-000000000004', 'SURVEY_IN_PROGRESS', 'SURVEY_SUBMITTED', 'SURVEY_SUBMIT', 'SURVEYOR'),
('70000000-0000-0000-0000-000000000005', 'SURVEY_SUBMITTED', 'ADJUSTOR_REVIEW', 'ADJUDICATION_VIEW_ASSIGNED', 'ADJUSTOR'),
('70000000-0000-0000-0000-000000000006', 'ADJUSTOR_REVIEW', 'APPROVED', 'ADJUDICATION_APPROVE', 'ADJUSTOR'),
('70000000-0000-0000-0000-000000000007', 'ADJUSTOR_REVIEW', 'REJECTED', 'ADJUDICATION_REJECT', 'ADJUSTOR'),
('70000000-0000-0000-0000-000000000008', 'APPROVED', 'WORKSHOP_SELECTED', 'WORKSHOP_SELECT', 'CUSTOMER'),
('70000000-0000-0000-0000-000000000009', 'WORKSHOP_SELECTED', 'REPAIR_IN_PROGRESS', 'WORKSHOP_UPDATE', 'WORKSHOP_USER'),
('70000000-0000-0000-0000-000000000010', 'REPAIR_IN_PROGRESS', 'REPAIR_COMPLETED', 'WORKSHOP_UPDATE', 'WORKSHOP_USER'),
('70000000-0000-0000-0000-000000000011', 'REPAIR_COMPLETED', 'PAYMENT_PENDING', 'WORKSHOP_UPDATE', 'WORKSHOP_USER'),
('70000000-0000-0000-0000-000000000012', 'PAYMENT_PENDING', 'PAYMENT_COMPLETED', 'PAYMENT_COMPLETE', 'CUSTOMER'),
('70000000-0000-0000-0000-000000000013', 'PAYMENT_COMPLETED', 'CLOSED', 'CLAIM_STATUS_UPDATE', 'CASE_MANAGER');
```
