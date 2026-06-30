
# eClaims Backend — Copilot/Claude Implementation Prompts

Use these prompts sequentially. Do not implement later modules before foundational modules are complete. Each prompt assumes Java 25, Spring Boot 4.1.0, PostgreSQL, Flyway, Spring Security JWT, Spring Data JPA, MapStruct, Lombok, Bean Validation, OpenAPI, and Docker.

---

## Prompt 1 — Create Backend Foundation

```text
You are implementing the eClaims backend MVP using Java 25 and Spring Boot 4.1.0.

Create a Spring Boot modular monolith project named eclaims-backend with Maven.

Add dependencies:
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- Spring Actuator
- PostgreSQL
- Flyway
- Lombok
- MapStruct
- springdoc-openapi
- JJWT
- MinIO
- Spring Mail
- Spring Boot Test
- Spring Security Test
- Testcontainers PostgreSQL
- ArchUnit

Create packages under com.nagarro.eclaims:
common, auth, user, rbac, policy, claim, workflow, assignment, survey, adjudication, workshop, document, notification, payment, reporting, audit.

Add application.yml, application-local.yml, application-docker.yml, application-test.yml.

Add a health endpoint through actuator and Swagger UI configuration.

Do not implement business modules yet.
```

---

## Prompt 2 — Common Response and Exception Framework

```text
Implement common backend framework for eClaims.

Create:
- ApiResponse<T>
- ApiError
- FieldErrorDetail
- PageResponse<T>
- BusinessException
- ResourceNotFoundException
- InvalidWorkflowTransitionException
- AccessDeniedBusinessException
- FileStorageException
- GlobalExceptionHandler using @RestControllerAdvice

All REST controllers must return ApiResponse<T>.
Validation errors should return code VALIDATION_ERROR.
Unauthorized should return 401.
Forbidden should return 403.
Business rule failures should return 400 or 422 based on context.
Do not expose stack traces.
```

---

## Prompt 3 — Flyway Migrations

```text
Implement Flyway migrations for eClaims backend.

Create migration files:
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

Use PostgreSQL UUID primary keys.
Include created_at and updated_at on all mutable tables.
Use foreign keys and indexes.
Do not use Hibernate ddl-auto create/update; use validate only.
```

---

## Prompt 4 — RBAC Entities and Repositories

```text
Implement RBAC module.

Entities:
- Role
- Permission

Relationships:
- Role many-to-many Permission through role_permissions.

Implement repositories:
- RoleRepository findByCode
- PermissionRepository findByCode/findByCodeIn

Implement RbacService:
- getPermissionsForUser(userId)
- roleExists(code)
- listRoles()
- listPermissions()

Use Lombok and JPA annotations.
Keep business logic out of repositories.
```

---

## Prompt 5 — User, Customer, Policy Entities

```text
Implement user, customer, and policy modules.

Entities:
- User
- Customer
- Policy
- PolicyCoverage

User has many roles.
Customer maps one-to-one to User.
Policy belongs to Customer.
PolicyCoverage belongs to Policy.

Repositories:
- UserRepository findByEmail
- CustomerRepository findByUserId
- PolicyRepository findByPolicyNumber, findByCustomerId

Add enums:
- UserStatus
- PolicyStatus
- BillingCycle

Implement services for user lookup and policy ownership validation.
```

---

## Prompt 6 — JWT Security

```text
Implement Spring Security JWT authentication.

Components:
- SecurityConfig
- JwtAuthenticationFilter
- JwtTokenProvider
- CustomUserDetails
- CustomUserDetailsService
- AuthService
- AuthController
- LoginRequest
- LoginResponse
- CurrentUserResponse

JWT claims must include:
- userId
- email
- roles
- permissions

Implement POST /api/v1/auth/login and GET /api/v1/auth/me.
Use BCryptPasswordEncoder.
Enable @EnableMethodSecurity.
Controllers should be protected by default except login/register.
```

---

## Prompt 7 — Permission-Based Authorization

```text
Implement permission-based authorization.

Use Spring Security authorities as permission codes, not only ROLE_ values.
When loading user details, flatten all role permissions into GrantedAuthority.

Add utility:
- CurrentUserProvider
- SecurityUtils

Add method-level security examples:
@PreAuthorize("hasAuthority('CLAIM_CREATE')")
@PreAuthorize("hasAuthority('REPORT_VIEW')")

Ensure 401 and 403 responses use standard ApiResponse error format.
```

---

## Prompt 8 — Claim Module Core

```text
Implement claim module.

Entities:
- Claim
- ClaimStatusHistory

Enums:
- ClaimStatus
- ClaimType

DTOs:
- CreateClaimRequest
- IncidentLocationRequest
- ClaimCreatedResponse
- ClaimListItemResponse
- ClaimDetailResponse
- ClaimTimelineItemResponse
- UpdateClaimStatusRequest

Repositories:
- ClaimRepository
- ClaimStatusHistoryRepository

Services:
- ClaimService
- ClaimNumberGenerator
- ClaimAccessService
- ClaimTimelineService

Rules:
- Customer can create claim only for own active policy.
- Incident date cannot be future.
- Claim number format CLM-{YEAR}-{6 digit sequence}.
- New claim status is SUBMITTED.
- Insert claim_status_history on creation.
- Publish ClaimSubmittedEvent.
- Write audit log.

APIs:
POST /api/v1/claims
GET /api/v1/claims
GET /api/v1/claims/{claimId}
GET /api/v1/claims/{claimId}/timeline
```

---

## Prompt 9 — Workflow Module

```text
Implement DB-driven workflow module.

Entity:
- WorkflowTransition

Repository:
- WorkflowTransitionRepository findByFromStatusAndToStatusAndActiveTrue

Service:
- ClaimWorkflowService

Methods:
- transitionClaim(UUID claimId, ClaimStatus targetStatus, String reason, String comments)
- canTransition(...)
- getAvailableTransitions(UUID claimId)

Rules:
- Load current claim status.
- Validate active transition exists.
- Validate required permission.
- Validate actor role if configured.
- Validate domain preconditions.
- Update claim status.
- Insert claim_status_history.
- Publish domain event.
- Write audit log.

Add API PATCH /api/v1/claims/{claimId}/status.
```

---

## Prompt 10 — Assignment Module

```text
Implement claim assignment module.

Entity:
- ClaimAssignment

Enum:
- AssignmentRole

Services:
- AssignmentService
- AssignmentRuleService
- UserAvailabilityService

APIs:
POST /api/v1/claims/{claimId}/assignments/auto
POST /api/v1/claims/{claimId}/assignments/reassign
GET /api/v1/claims/{claimId}/assignments

Rules:
- Case manager selected by least active assignment.
- Surveyor selected by incident ZIP/service area and least active assignment.
- Adjustor selected by least active adjudication queue.
- Reassignment deactivates previous active assignment.
- Assignment writes audit log and publishes ClaimAssignedEvent.
```

---

## Prompt 11 — Survey Module

```text
Implement survey module.

Entity:
- SurveyReport

DTOs:
- SubmitSurveyReportRequest
- SurveyReportResponse

APIs:
GET /api/v1/surveyor/claims
POST /api/v1/claims/{claimId}/survey-reports
GET /api/v1/claims/{claimId}/survey-reports

Rules:
- User must have SURVEY_SUBMIT.
- User must be active assigned surveyor for claim.
- Claim must be in SURVEYOR_ASSIGNED or SURVEY_IN_PROGRESS.
- Save survey report.
- Transition claim to SURVEY_SUBMITTED and ADJUSTOR_REVIEW as appropriate.
- Publish SurveySubmittedEvent.
- Notify adjustor.
```

---

## Prompt 12 — Adjudication Module

```text
Implement adjudication module.

Entity:
- AdjustorDecision

Enum:
- AdjustorDecisionType APPROVED, REJECTED

APIs:
GET /api/v1/adjustor/claims
POST /api/v1/claims/{claimId}/adjudications
GET /api/v1/claims/{claimId}/adjudications

Rules:
- User must be assigned adjustor.
- Survey report must exist.
- Approval requires approvedAmount.
- approvedAmount should not exceed policy coverageLimit for MVP.
- Approval transitions claim to APPROVED.
- Rejection transitions claim to REJECTED.
- Publish ClaimApprovedEvent or ClaimRejectedEvent.
- Notify customer and case manager.
```

---

## Prompt 13 — Document Module with MinIO

```text
Implement document module.

Entities:
- Document
- DocumentVersion

Storage abstraction:
- DocumentStorageService
- DocumentUploadCommand
- StoredDocument
- MinioDocumentStorageService

APIs:
POST /api/v1/claims/{claimId}/documents multipart/form-data
GET /api/v1/claims/{claimId}/documents
GET /api/v1/documents/{documentId}/download
DELETE /api/v1/documents/{documentId}

Rules:
- Validate file size and content type.
- User must have access to claim.
- Store file in MinIO.
- Store metadata in PostgreSQL.
- Download checks access.
- Upload/download writes audit log.
```

---

## Prompt 14 — Notification Module

```text
Implement notification module.

Entities:
- Notification
- NotificationEvent

Events:
- ClaimSubmittedEvent
- ClaimAssignedEvent
- SurveySubmittedEvent
- ClaimApprovedEvent
- ClaimRejectedEvent
- RepairStatusUpdatedEvent
- PaymentCompletedEvent

Listeners:
- NotificationEventListener

APIs:
GET /api/v1/notifications
PATCH /api/v1/notifications/{notificationId}/read

MVP behavior:
- Store notification_event row.
- Create notification row for recipient.
- Send email through Spring Mail/MailHog.
- Update status SENT or FAILED.
- Only recipient can mark own notification read.
```

---

## Prompt 15 — Workshop Module

```text
Implement workshop module.

Entities:
- Workshop
- WorkshopClaim
- WorkOrder
- RepairUpdate
- FinalInvoice

APIs:
GET /api/v1/workshops/search
POST /api/v1/claims/{claimId}/workshop-selection
POST /api/v1/workshop/claims/{claimId}/work-orders
POST /api/v1/workshop/claims/{claimId}/repair-updates
POST /api/v1/workshop/claims/{claimId}/final-invoice

Rules:
- Workshop selection only after APPROVED.
- Customer must own claim.
- Workshop user can update only selected workshop claim.
- Work order transitions to REPAIR_IN_PROGRESS.
- Repair completion transitions to REPAIR_COMPLETED.
- Final invoice transitions to PAYMENT_PENDING.
- Repair updates notify customer.
```

---

## Prompt 16 — Payment Module

```text
Implement payment module.

Entity:
- Payment

APIs:
POST /api/v1/claims/{claimId}/payments/initiate
POST /api/v1/claims/{claimId}/payments/mock-complete
GET /api/v1/claims/{claimId}/payments

Rules:
- Payment requires PAYMENT_PENDING claim.
- Customer must own claim.
- Mock completion updates payment status COMPLETED.
- Transition claim to PAYMENT_COMPLETED.
- Optionally transition to CLOSED through case manager/system.
- Publish PaymentCompletedEvent.
```

---

## Prompt 17 — Reporting Module

```text
Implement reporting module.

APIs:
GET /api/v1/reports/claims-summary
GET /api/v1/reports/claims-ageing
GET /api/v1/reports/claims-by-region
GET /api/v1/reports/claims-processing-time
GET /api/v1/reports/claims-by-role
GET /api/v1/reports/fraud-indicators

Rules:
- Requires REPORT_VIEW.
- Regional manager should be filterable by region.
- Use repository projections or native SQL.
- Reports should not expose document binary data.

Fraud indicators:
- Multiple claims on same policy in short window.
- Claim amount exceeds threshold.
- Repeated claims from same workshop.
- Incident date older than configurable days.
```

---

## Prompt 18 — Audit Module

```text
Implement audit module.

Entity:
- AuditLog

Service:
- AuditService

API:
GET /api/v1/audit/claims/{claimId}

Rules:
- Requires AUDIT_VIEW.
- Audit claim creation, status changes, assignments, documents, survey, adjudication, workshop updates, payments, claim closure.
- Use explicit AuditService calls for sensitive actions.
- Use event listeners for lifecycle events where appropriate.
```

---

## Prompt 19 — Tests

```text
Create tests for eClaims backend.

Unit tests:
- ClaimServiceTest
- ClaimWorkflowServiceTest
- AssignmentRuleServiceTest
- SurveyReportServiceTest
- AdjudicationServiceTest
- DocumentServiceTest
- PaymentServiceTest

Integration tests using Testcontainers PostgreSQL:
- AuthControllerIT
- ClaimControllerIT
- WorkflowTransitionRepositoryIT
- ReportingRepositoryIT

Security tests:
- Customer cannot view another customer's claim.
- Surveyor cannot submit unassigned claim report.
- Adjustor cannot approve unassigned claim.
- Workshop cannot update another workshop's claim.
- Auditor cannot mutate claim.

ArchUnit tests:
- Controllers must not depend on repositories.
- Repositories must not depend on services/controllers.
- Common package must not depend on feature modules.
```

---

## Prompt 20 — Docker, README, and Demo Readiness

```text
Create Dockerfile for backend and docker-compose.yml including:
- postgres
- minio
- mailhog
- backend

Add README with:
- prerequisites
- run instructions
- local URLs
- demo users
- Swagger URL
- MinIO console URL
- MailHog URL
- sample demo flow

Ensure backend starts with docker-compose and Flyway migrations run automatically.
Ensure Swagger is available at /swagger-ui.html or /swagger-ui/index.html.
```
