# eClaims Backend Java/Spring Boot Implementation Specification

**Project:** nagarro eClaims — Electronic Auto Insurance Claims Processing MVP  
**Document Type:** Backend Implementation Specification  
**Target Audience:** Backend engineers, full-stack engineers, architects, reviewers, GitHub Copilot/Claude implementation agents  
**Final Backend Stack:** Java 25, Spring Boot 4.1.0, Spring Security, Spring Data JPA/Hibernate, PostgreSQL, Flyway, MapStruct, Bean Validation, OpenAPI/Swagger, JUnit 5, Testcontainers, Docker

---

## 1. Purpose

This document defines the backend-specific implementation plan for the eClaims MVP. It translates the assignment requirements into a production-style Spring Boot modular monolith backend that supports digital claim submission, claim workflow, document management, notifications, reporting, auditability, and role-based authorization. The original case study requires digitizing claim submission, customer tracking, surveyor assessment, adjustor adjudication, workshop updates, reporting, document archival, and alerts/notifications. citeturn1search2

The implementation should demonstrate clear layers, modules, folder structures, security, scalability, extensibility, testability, and deployment readiness. The assignment specifically evaluates application layers, tiers, modules, and folder structures for presentation, business, and data access concerns. citeturn1search2

---

## 2. Backend Scope

### 2.1 In Scope

- Spring Boot backend API.
- JWT-based authentication.
- DB-backed roles and permissions.
- Policy lookup and customer registration support.
- Claim creation, status tracking, lifecycle timeline.
- DB-driven workflow transition validation.
- Assignment of case manager, surveyor, and adjustor.
- Surveyor assessment submission.
- Adjustor approval/rejection.
- Workshop selection, work order, repair updates, final invoice.
- Document upload/download using MinIO/S3-compatible abstraction.
- Notification event persistence and email simulation.
- Payment simulation.
- Claims reporting.
- Audit logging.
- Flyway migration scripts.
- Swagger/OpenAPI.
- Unit, integration, security, workflow, and architecture tests.
- Docker Compose-compatible runtime.

### 2.2 Out of Scope for Backend MVP

- Real payment gateway settlement.
- Real SMS gateway.
- Real external workshop, rental, or policy-core integration.
- Native mobile APIs beyond responsive web API support.
- Fraud ML model.
- Camunda/Temporal production workflow engine integration.
- Full enterprise DMS integration.

External integrations should still be represented using clean adapter interfaces so the MVP can evolve into the target architecture.

---

## 3. Architecture Style

### 3.1 Selected Style

Use a **modular monolith** with DDD-style bounded modules.

This is intentionally selected for MVP because it keeps implementation and deployment simple while still showing clear component boundaries. The assignment expects componentized functionality, flexible design, reliability, logging, security, and cloud/on-premise deployability. citeturn1search2

### 3.2 Layering Rules

```text
Controller Layer
  -> Application Service Layer
      -> Domain Service / Policy / Workflow Rules
          -> Repository Layer
              -> PostgreSQL

Integration Adapter Layer
  -> MinIO/S3
  -> MailHog/Email
  -> Payment Gateway Mock
  -> Future Partner APIs
```

Rules:

- Controllers must not call repositories directly.
- Controllers validate request shape and delegate use cases to services.
- Application services define transaction boundaries.
- Domain services own business rules.
- Repositories only handle persistence.
- Integration adapters hide external technology details.
- Audit and notification must be triggered through consistent services/events.

---

## 4. Backend Project Structure

```text
eclaims-backend/
  pom.xml
  Dockerfile
  README.md
  src/main/java/com/nagarro/eclaims/
    EclaimsApplication.java

    common/
      config/
      exception/
      response/
      security/
      validation/
      mapper/
      pagination/
      constants/
      util/

    auth/
      controller/
      service/
      dto/

    user/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/

    rbac/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/

    policy/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/
      enums/

    claim/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/
      enums/

    workflow/
      service/
      repository/
      entity/
      dto/
      enums/

    assignment/
      service/
      repository/
      entity/
      dto/
      mapper/

    survey/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/

    adjudication/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/

    workshop/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/

    document/
      controller/
      service/
      storage/
      repository/
      entity/
      dto/
      mapper/

    notification/
      controller/
      service/
      repository/
      entity/
      dto/
      event/
      listener/

    payment/
      controller/
      service/
      repository/
      entity/
      dto/
      mapper/

    reporting/
      controller/
      service/
      repository/
      dto/

    audit/
      service/
      repository/
      entity/
      dto/
      listener/

  src/main/resources/
    application.yml
    application-local.yml
    application-docker.yml
    application-test.yml
    db/migration/
```

---

## 5. Maven Dependencies

Use Maven with the following dependency groups:

```xml
<!-- Spring Boot Starters -->
spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-data-jpa
spring-boot-starter-validation
spring-boot-starter-actuator
spring-boot-starter-mail

<!-- Database -->
postgresql
flyway-core
flyway-database-postgresql

<!-- Security -->
io.jsonwebtoken:jjwt-api
io.jsonwebtoken:jjwt-impl
io.jsonwebtoken:jjwt-jackson

<!-- Mapping / Utilities -->
org.mapstruct:mapstruct
org.mapstruct:mapstruct-processor
org.projectlombok:lombok

<!-- OpenAPI -->
org.springdoc:springdoc-openapi-starter-webmvc-ui

<!-- MinIO -->
io.minio:minio

<!-- Testing -->
spring-boot-starter-test
spring-security-test
org.testcontainers:junit-jupiter
org.testcontainers:postgresql
com.tngtech.archunit:archunit-junit5
```

---

## 6. Application Configuration

### 6.1 Profiles

```text
local   -> local developer execution
 docker -> docker-compose runtime
test    -> testcontainers/integration tests
```

### 6.2 Required Configuration Keys

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5435/eclaims
    username: eclaims
    password: eclaims
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true

security:
  jwt:
    secret: ${JWT_SECRET:change-this-secret-for-local-only}
    access-token-expiration-minutes: 60

storage:
  provider: minio
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: eclaims-documents

mail:
  from: no-reply@nagarro.com
```

---

## 7. Common Backend Standards

### 7.1 Standard API Response

```java
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    ApiError error,
    Instant timestamp
) {}
```

### 7.2 Error Response

```java
public record ApiError(
    String code,
    String message,
    List<FieldErrorDetail> fieldErrors
) {}
```

### 7.3 Pagination Response

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {}
```

### 7.4 Exception Types

```text
BusinessException
ResourceNotFoundException
AccessDeniedBusinessException
InvalidWorkflowTransitionException
DuplicateResourceException
FileStorageException
ValidationException
```

### 7.5 Global Exception Handler

Implement `@RestControllerAdvice` for:

- Validation errors.
- Authentication/authorization errors.
- Business errors.
- Resource not found.
- Invalid workflow transition.
- File storage failures.
- Generic internal errors.

Do not expose stack traces in API responses.

---

## 8. Database Migration Plan

Use Flyway. Every DB change must be versioned.

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

## 9. Entity Model

### 9.1 Common Base Entity

```java
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
```

### 9.2 User/RBAC Entities

```text
User
- id
- fullName
- email
- passwordHash
- status
- lastLoginAt
- roles

Role
- id
- code
- name
- description
- active
- permissions

Permission
- id
- code
- name
- description
- active
```

Permissions should be DB-backed because the assignment requires configurable authorization actions without code changes. citeturn1search2

### 9.3 Customer and Policy Entities

```text
Customer
- id
- userId
- customerNumber
- firstName
- lastName
- phone
- correspondenceAddress
- billingCycle

Policy
- id
- policyNumber
- customerId
- vehicleVin
- vehicleMake
- vehicleModel
- vehicleYear
- policyStatus
- effectiveDate
- expiryDate
- deductibleAmount
- coverageLimit

PolicyCoverage
- id
- policyId
- coverageType
- coverageLimit
- deductibleAmount
```

### 9.4 Claim Entities

```text
Claim
- id
- claimNumber
- policyId
- customerId
- claimType
- currentStatus
- incidentDate
- incidentTime
- incidentAddressLine1
- incidentCity
- incidentState
- incidentZipCode
- incidentCountry
- description
- vehicleDrivable
- policeReportAvailable
- submittedAt
- closedAt

ClaimStatusHistory
- id
- claimId
- fromStatus
- toStatus
- changedByUserId
- changedByRole
- reason
- comments
- createdAt
```

### 9.5 Workflow Entity

```text
WorkflowTransition
- id
- fromStatus
- toStatus
- requiredPermission
- actorRole
- active
```

### 9.6 Assignment Entity

```text
ClaimAssignment
- id
- claimId
- assignedUserId
- assignmentRole
- assignedByUserId
- active
- assignedAt
- releasedAt
- reason
```

### 9.7 Survey Entity

```text
SurveyReport
- id
- claimId
- surveyorUserId
- damageSeverity
- assessmentSummary
- estimatedRepairAmount
- currency
- recommendation
- remarks
- submittedAt
```

### 9.8 Adjudication Entity

```text
AdjustorDecision
- id
- claimId
- adjustorUserId
- decision
- approvedAmount
- currency
- remarks
- decidedAt
```

### 9.9 Workshop Entities

```text
Workshop
- id
- name
- partnerCode
- addressLine1
- city
- state
- zipCode
- phone
- email
- active

WorkshopClaim
- id
- claimId
- workshopId
- selectedByCustomerId
- selectedAt
- status

WorkOrder
- id
- claimId
- workshopId
- estimateAmount
- currency
- description
- submittedAt

RepairUpdate
- id
- claimId
- workshopId
- repairStatus
- progressPercentage
- expectedDeliveryDate
- remarks
- createdAt

FinalInvoice
- id
- claimId
- workshopId
- invoiceAmount
- currency
- invoiceNumber
- submittedAt
```

### 9.10 Document Entity

```text
Document
- id
- claimId
- uploadedByUserId
- documentType
- originalFileName
- contentType
- fileSize
- storageProvider
- storageBucket
- storageKey
- checksum
- version
- active
- uploadedAt

DocumentVersion
- id
- documentId
- version
- storageKey
- uploadedByUserId
- uploadedAt
```

Document management is a key requirement because claim documents must be stored centrally for audit and compliance. citeturn1search2

### 9.11 Notification, Payment, Audit Entities

```text
Notification
- id
- recipientUserId
- channel
- subject
- message
- status
- relatedEntityType
- relatedEntityId
- readAt
- createdAt

NotificationEvent
- id
- eventType
- payloadJson
- status
- retryCount
- lastError
- createdAt
- processedAt

Payment
- id
- claimId
- payerCustomerId
- amount
- currency
- status
- paymentReference
- initiatedAt
- completedAt

AuditLog
- id
- actorUserId
- actorRole
- action
- entityType
- entityId
- oldValueJson
- newValueJson
- ipAddress
- userAgent
- createdAt
```

---

## 10. Enums

```java
public enum ClaimStatus {
    DRAFT,
    SUBMITTED,
    CASE_ASSIGNED,
    SURVEYOR_ASSIGNED,
    SURVEY_IN_PROGRESS,
    SURVEY_SUBMITTED,
    ADJUSTOR_REVIEW,
    APPROVED,
    REJECTED,
    WORKSHOP_SELECTED,
    REPAIR_IN_PROGRESS,
    REPAIR_COMPLETED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    CLOSED,
    CANCELLED
}
```

```java
public enum ClaimType {
    ACCIDENT, THEFT, FIRE, FLOOD, VANDALISM, OTHER
}
```

```java
public enum UserStatus {
    ACTIVE, INACTIVE, LOCKED
}
```

```java
public enum AssignmentRole {
    CASE_MANAGER, SURVEYOR, ADJUSTOR
}
```

```java
public enum DocumentType {
    CLAIM_PHOTO,
    POLICE_REPORT,
    SURVEY_REPORT,
    WORK_ORDER,
    REPAIR_ESTIMATE,
    FINAL_INVOICE,
    PAYMENT_RECEIPT,
    COMMUNICATION_RECORD,
    OTHER
}
```

```java
public enum RepairStatus {
    NOT_STARTED,
    ESTIMATE_SUBMITTED,
    IN_PROGRESS,
    PARTS_WAITING,
    QUALITY_CHECK,
    COMPLETED,
    DELIVERED
}
```

```java
public enum PaymentStatus {
    PENDING, INITIATED, COMPLETED, FAILED, REFUNDED
}
```

```java
public enum NotificationStatus {
    PENDING, SENT, FAILED, READ
}
```

---

## 11. RBAC Permission Matrix

### 11.1 Permissions

```text
CLAIM_CREATE
CLAIM_VIEW_SELF
CLAIM_VIEW_ASSIGNED
CLAIM_VIEW_ALL
CLAIM_ASSIGN
CLAIM_REASSIGN
CLAIM_STATUS_UPDATE
SURVEY_VIEW_ASSIGNED
SURVEY_SUBMIT
ADJUDICATION_VIEW_ASSIGNED
ADJUDICATION_REVIEW
ADJUDICATION_APPROVE
ADJUDICATION_REJECT
WORKSHOP_SELECT
WORKSHOP_VIEW_ASSIGNED
WORKSHOP_UPDATE
DOCUMENT_UPLOAD
DOCUMENT_VIEW
DOCUMENT_DELETE
PAYMENT_INITIATE
PAYMENT_COMPLETE
NOTIFICATION_VIEW
REPORT_VIEW
AUDIT_VIEW
ADMIN_MANAGE_USERS
ADMIN_MANAGE_ROLES
ADMIN_MANAGE_REFERENCE_DATA
```

### 11.2 Role to Permission Mapping

```text
CUSTOMER:
- CLAIM_CREATE
- CLAIM_VIEW_SELF
- WORKSHOP_SELECT
- DOCUMENT_UPLOAD
- DOCUMENT_VIEW
- PAYMENT_INITIATE
- PAYMENT_COMPLETE
- NOTIFICATION_VIEW

CASE_MANAGER:
- CLAIM_VIEW_ASSIGNED
- CLAIM_VIEW_ALL
- CLAIM_ASSIGN
- CLAIM_REASSIGN
- CLAIM_STATUS_UPDATE
- DOCUMENT_VIEW
- REPORT_VIEW

SURVEYOR:
- CLAIM_VIEW_ASSIGNED
- SURVEY_VIEW_ASSIGNED
- SURVEY_SUBMIT
- DOCUMENT_UPLOAD
- DOCUMENT_VIEW

ADJUSTOR:
- CLAIM_VIEW_ASSIGNED
- ADJUDICATION_VIEW_ASSIGNED
- ADJUDICATION_REVIEW
- ADJUDICATION_APPROVE
- ADJUDICATION_REJECT
- DOCUMENT_VIEW

WORKSHOP_USER:
- WORKSHOP_VIEW_ASSIGNED
- WORKSHOP_UPDATE
- DOCUMENT_UPLOAD
- DOCUMENT_VIEW

AUDITOR:
- CLAIM_VIEW_ALL
- DOCUMENT_VIEW
- AUDIT_VIEW
- REPORT_VIEW

REGIONAL_MANAGER:
- CLAIM_VIEW_ALL
- REPORT_VIEW

ADMIN:
- all permissions
```

---

## 12. Security Implementation

### 12.1 Authentication Flow

```text
1. User submits email/password.
2. AuthService validates credentials.
3. Password checked using BCrypt.
4. User roles and permissions are loaded.
5. JWT is generated with subject, userId, roles, permissions.
6. API returns access token and user profile.
```

### 12.2 Spring Security Components

```text
JwtAuthenticationFilter
JwtTokenProvider
CustomUserDetailsService
PermissionEvaluator or SecurityExpressionRoot extension
AuthenticationEntryPoint
AccessDeniedHandler
SecurityConfig
```

### 12.3 Method-Level Authorization

Use method-level annotations for critical operations.

```java
@PreAuthorize("hasAuthority('CLAIM_CREATE')")
public ClaimResponse createClaim(CreateClaimRequest request) { ... }
```

### 12.4 Security Rules

- Only customer can view own claim unless role has `CLAIM_VIEW_ALL` or assignment.
- Surveyor can submit report only for assigned claim.
- Adjustor can decide only assigned claim.
- Workshop user can update only selected/assigned workshop claim.
- Auditor can view but not mutate claim workflow.
- Document download must check claim access.
- Admin-only APIs require admin permissions.

---

## 13. Workflow Implementation

### 13.1 Workflow Service Contract

```java
public interface ClaimWorkflowService {
    void transitionClaim(UUID claimId, ClaimStatus targetStatus, String reason, String comments);
    boolean canTransition(ClaimStatus from, ClaimStatus to, Set<String> permissions, Set<String> roles);
    List<WorkflowTransitionResponse> getAvailableTransitions(UUID claimId);
}
```

### 13.2 Transition Logic

```text
1. Load claim.
2. Read current status.
3. Find active transition from current status to target status.
4. Validate actor role and required permission.
5. Validate domain preconditions.
6. Update claim current status.
7. Insert claim_status_history.
8. Write audit log.
9. Publish domain event.
```

### 13.3 Domain Preconditions

```text
SUBMITTED -> CASE_ASSIGNED:
- claim must have valid policy

CASE_ASSIGNED -> SURVEYOR_ASSIGNED:
- active case manager assignment exists
- surveyor assignment must exist

SURVEY_IN_PROGRESS -> SURVEY_SUBMITTED:
- survey report must be present

SURVEY_SUBMITTED -> ADJUSTOR_REVIEW:
- adjustor assignment must exist

ADJUSTOR_REVIEW -> APPROVED:
- approved amount must be present
- approved amount must not exceed policy coverage without override

APPROVED -> WORKSHOP_SELECTED:
- workshop must be selected by customer

REPAIR_COMPLETED -> PAYMENT_PENDING:
- final invoice must be present

PAYMENT_PENDING -> PAYMENT_COMPLETED:
- payment record must be completed
```

---

## 14. Backend Module Implementation Details

## 14.1 Auth Module

### APIs

```http
POST /api/v1/auth/login
POST /api/v1/auth/register-customer
GET  /api/v1/auth/me
POST /api/v1/auth/logout
```

### DTOs

```java
public record LoginRequest(
    @NotBlank @Email String email,
    @NotBlank String password
) {}
```

```java
public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    AuthenticatedUserResponse user
) {}
```

### Services

```text
AuthService
JwtTokenService
CustomerRegistrationService
```

---

## 14.2 Claim Module

### APIs

```http
POST /api/v1/claims
GET  /api/v1/claims?page=0&size=20&status=SUBMITTED
GET  /api/v1/claims/{claimId}
GET  /api/v1/claims/{claimId}/timeline
PATCH /api/v1/claims/{claimId}/status
```

### Create Claim Request

```java
public record CreateClaimRequest(
    @NotBlank String policyNumber,
    @NotNull ClaimType claimType,
    @NotNull LocalDate incidentDate,
    @NotNull LocalTime incidentTime,
    @Valid IncidentLocationRequest incidentLocation,
    @NotBlank @Size(max = 4000) String description,
    boolean vehicleDrivable,
    boolean policeReportAvailable
) {}
```

### Create Claim Response

```java
public record ClaimCreatedResponse(
    UUID claimId,
    String claimNumber,
    ClaimStatus status,
    Instant createdAt
) {}
```

### Services

```text
ClaimService
ClaimNumberGenerator
ClaimAccessService
ClaimTimelineService
```

### Business Rules

- Policy must exist and be active.
- Customer must own policy.
- Incident date cannot be in the future.
- Claim number format: `CLM-{YEAR}-{SEQUENCE}`.
- Claim creation sets status to `SUBMITTED`.
- Claim creation triggers auto-assignment or assignment event.
- Claim creation writes history and audit events.

---

## 14.3 Assignment Module

### APIs

```http
POST /api/v1/claims/{claimId}/assignments/auto
POST /api/v1/claims/{claimId}/assignments/reassign
GET  /api/v1/claims/{claimId}/assignments
```

The assignment requires automatic case assignment based on availability, field-office/service-area coverage, and reassignment if surveyor/adjustor is unavailable. citeturn1search2

### Services

```text
AssignmentService
AssignmentRuleService
UserAvailabilityService
```

### MVP Auto Assignment Rules

```text
Case Manager:
- match customer/incident region
- choose least active assigned claims

Surveyor:
- match zip code/service area
- choose least active assigned claims

Adjustor:
- match region if possible
- choose least active pending adjudications
```

---

## 14.4 Survey Module

### APIs

```http
GET  /api/v1/surveyor/claims
POST /api/v1/claims/{claimId}/survey-reports
GET  /api/v1/claims/{claimId}/survey-reports
```

The surveyor must be able to submit an online assessment and notify the adjustor afterward. citeturn1search2

### Submit Survey Request

```java
public record SubmitSurveyReportRequest(
    @NotBlank String damageSeverity,
    @NotBlank @Size(max = 4000) String assessmentSummary,
    @NotNull @DecimalMin("0.0") BigDecimal estimatedRepairAmount,
    @NotBlank String currency,
    @NotBlank String recommendation,
    String remarks
) {}
```

### Business Rules

- Current user must be assigned surveyor.
- Claim must be in `SURVEYOR_ASSIGNED` or `SURVEY_IN_PROGRESS`.
- Survey submission creates report.
- Claim transitions to `SURVEY_SUBMITTED`, then `ADJUSTOR_REVIEW` when applicable.
- Notification event is created for adjustor.

---

## 14.5 Adjudication Module

### APIs

```http
GET  /api/v1/adjustor/claims
POST /api/v1/claims/{claimId}/adjudications
GET  /api/v1/claims/{claimId}/adjudications
```

The adjustor must review claim details, documents, assessment report, and adjudicate based on policy coverage. citeturn1search2

### Request

```java
public record SubmitAdjustorDecisionRequest(
    @NotNull AdjustorDecisionType decision,
    @DecimalMin("0.0") BigDecimal approvedAmount,
    @NotBlank String currency,
    @Size(max = 4000) String remarks
) {}
```

### Rules

- Current user must be assigned adjustor.
- Survey report must exist.
- Decision must be APPROVED or REJECTED.
- Approved amount is required for approval.
- Approved amount should not exceed coverage limit unless manager override is implemented.
- Approval transitions claim to `APPROVED`.
- Rejection transitions claim to `REJECTED`.
- Customer notification is generated.

---

## 14.6 Workshop Module

### APIs

```http
GET  /api/v1/workshops
GET  /api/v1/workshops/search?zipCode=10001
POST /api/v1/claims/{claimId}/workshop-selection
POST /api/v1/workshop/claims/{claimId}/work-orders
POST /api/v1/workshop/claims/{claimId}/repair-updates
POST /api/v1/workshop/claims/{claimId}/final-invoice
```

Partner workshops must upload work orders, estimates, progress updates, final bill, and track payment status. citeturn1search2

### MVP Rules

- Customer can select workshop only after claim approval.
- Workshop user can update only claims assigned to their workshop.
- Repair update generates customer notification.
- Final invoice changes claim to `PAYMENT_PENDING`.

---

## 14.7 Document Module

### APIs

```http
POST /api/v1/claims/{claimId}/documents
GET  /api/v1/claims/{claimId}/documents
GET  /api/v1/documents/{documentId}/download
DELETE /api/v1/documents/{documentId}
```

### Storage Interface

```java
public interface DocumentStorageService {
    StoredDocument upload(DocumentUploadCommand command);
    InputStream download(String storageKey);
    void delete(String storageKey);
}
```

### Rules

- Max file size configurable.
- Allowed content types configurable.
- File extension and content type validated.
- User must have access to claim.
- Metadata saved in PostgreSQL.
- File saved in MinIO.
- Document upload writes audit log.
- Download writes audit log.

---

## 14.8 Notification Module

### Events

```text
CLAIM_SUBMITTED
CASE_ASSIGNED
SURVEYOR_ASSIGNED
SURVEY_SUBMITTED
CLAIM_APPROVED
CLAIM_REJECTED
WORKSHOP_SELECTED
REPAIR_STATUS_UPDATED
PAYMENT_COMPLETED
CLAIM_CLOSED
```

The assignment requires alerts/notifications through email/SMS on claim progress and status changes, and communication must be archived for audit/compliance. citeturn1search2

### Implementation

```text
Business service publishes domain event.
Notification listener receives event.
NotificationEvent row is inserted.
Notification row is created for recipient.
Email sent through Spring Mail/MailHog.
Status updated to SENT or FAILED.
```

For MVP, use Spring events. For target architecture, this can evolve to transactional outbox + message broker.

---

## 14.9 Payment Module

### APIs

```http
POST /api/v1/claims/{claimId}/payments/initiate
POST /api/v1/claims/{claimId}/payments/mock-complete
GET  /api/v1/claims/{claimId}/payments
```

### Rules

- Payment initiation requires `PAYMENT_PENDING` status.
- Mock completion marks payment `COMPLETED`.
- Claim transitions to `PAYMENT_COMPLETED`.
- If all closure preconditions pass, claim can transition to `CLOSED`.

---

## 14.10 Reporting Module

### APIs

```http
GET /api/v1/reports/claims-summary
GET /api/v1/reports/claims-ageing
GET /api/v1/reports/claims-by-region
GET /api/v1/reports/claims-processing-time
GET /api/v1/reports/claims-by-role
GET /api/v1/reports/fraud-indicators
```

Reports are required for claims processed, processing time, fraudulent claims, ageing matrix, claims by role, region, geography, and amount paid. citeturn1search2

### MVP Query Approach

- Use repository projections for simple aggregates.
- Use native SQL for ageing and processing-time reports if JPQL becomes complex.
- Use indexes on `claims.current_status`, `claims.incident_zip_code`, `claims.submitted_at`, `claims.closed_at`, `claim_status_history.created_at`.

---

## 14.11 Audit Module

### Audit Actions

```text
USER_LOGIN
CLAIM_CREATED
CLAIM_STATUS_CHANGED
ASSIGNMENT_CREATED
ASSIGNMENT_REASSIGNED
DOCUMENT_UPLOADED
DOCUMENT_DOWNLOADED
SURVEY_REPORT_SUBMITTED
ADJUSTOR_DECISION_SUBMITTED
WORKSHOP_UPDATE_SUBMITTED
PAYMENT_COMPLETED
CLAIM_CLOSED
```

### Implementation Options

- Explicit audit service calls inside application services.
- Event listener-based audit for domain events.

Recommended MVP approach:

```text
Use explicit AuditService for security-sensitive actions and domain event listener for lifecycle events.
```

---

## 15. API Contract Details

## 15.1 Create Claim

```http
POST /api/v1/claims
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "policyNumber": "POL-1000001",
  "claimType": "ACCIDENT",
  "incidentDate": "2026-06-20",
  "incidentTime": "15:30:00",
  "incidentLocation": {
    "addressLine1": "5th Avenue",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "description": "Vehicle rear-ended at traffic signal.",
  "vehicleDrivable": false,
  "policeReportAvailable": true
}
```

```json
{
  "success": true,
  "message": "Claim submitted successfully.",
  "data": {
    "claimId": "4a8d05d1-e1b4-4eaa-98f2-8138c0135410",
    "claimNumber": "CLM-2026-000001",
    "status": "SUBMITTED",
    "createdAt": "2026-06-30T10:30:00Z"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:01Z"
}
```

## 15.2 Submit Survey Report

```http
POST /api/v1/claims/{claimId}/survey-reports
Authorization: Bearer <surveyor-token>
```

```json
{
  "damageSeverity": "MEDIUM",
  "assessmentSummary": "Rear bumper and trunk damage observed.",
  "estimatedRepairAmount": 1800.00,
  "currency": "USD",
  "recommendation": "Repairable",
  "remarks": "Vehicle can be repaired at partner workshop."
}
```

## 15.3 Submit Adjustor Decision

```http
POST /api/v1/claims/{claimId}/adjudications
Authorization: Bearer <adjustor-token>
```

```json
{
  "decision": "APPROVED",
  "approvedAmount": 1600.00,
  "currency": "USD",
  "remarks": "Approved as per collision coverage after deductible."
}
```

## 15.4 Submit Repair Update

```http
POST /api/v1/workshop/claims/{claimId}/repair-updates
Authorization: Bearer <workshop-token>
```

```json
{
  "repairStatus": "IN_PROGRESS",
  "progressPercentage": 45,
  "expectedDeliveryDate": "2026-07-08",
  "remarks": "Bumper replacement completed, paint work pending."
}
```

---

## 16. Flyway DDL Draft

### 16.1 RBAC Schema

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(128) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);
```

### 16.2 User Schema

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### 16.3 Claim Schema

```sql
CREATE TABLE claims (
    id UUID PRIMARY KEY,
    claim_number VARCHAR(64) NOT NULL UNIQUE,
    policy_id UUID NOT NULL,
    customer_id UUID NOT NULL,
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
    submitted_at TIMESTAMP NOT NULL,
    closed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_claims_policy_id ON claims(policy_id);
CREATE INDEX idx_claims_customer_id ON claims(customer_id);
CREATE INDEX idx_claims_status ON claims(current_status);
CREATE INDEX idx_claims_zip ON claims(incident_zip_code);
CREATE INDEX idx_claims_submitted_at ON claims(submitted_at);
```

### 16.4 Claim Status History

```sql
CREATE TABLE claim_status_history (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL REFERENCES claims(id),
    from_status VARCHAR(64),
    to_status VARCHAR(64) NOT NULL,
    changed_by_user_id UUID NOT NULL REFERENCES users(id),
    changed_by_role VARCHAR(64),
    reason TEXT,
    comments TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_claim_status_history_claim ON claim_status_history(claim_id);
```

### 16.5 Workflow Transitions

```sql
CREATE TABLE workflow_transitions (
    id UUID PRIMARY KEY,
    from_status VARCHAR(64) NOT NULL,
    to_status VARCHAR(64) NOT NULL,
    required_permission VARCHAR(128) NOT NULL,
    actor_role VARCHAR(64),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_workflow_transition UNIQUE(from_status, to_status, required_permission)
);
```

---

## 17. Service Responsibility Matrix

```text
AuthService:
- login
- token generation
- current user profile

UserService:
- user lookup
- profile management
- admin user operations

RbacService:
- roles
- permissions
- permission lookup

ClaimService:
- claim creation
- claim listing
- claim details
- claim access validation

ClaimWorkflowService:
- workflow transition validation
- status history
- available transitions

AssignmentService:
- auto assignment
- manual reassignment
- active assignee lookup

SurveyReportService:
- assigned claim validation
- survey submission

AdjudicationService:
- adjustor validation
- approve/reject claim

WorkshopService:
- workshop search
- workshop claim lifecycle

DocumentService:
- metadata persistence
- authorization
- storage delegation

NotificationService:
- notification creation
- event processing
- email dispatch

PaymentService:
- mock payment initiation/completion

ReportingService:
- aggregates and report DTOs

AuditService:
- audit record creation
```

---

## 18. Domain Events

```java
public record ClaimSubmittedEvent(UUID claimId, UUID customerUserId, Instant occurredAt) {}
public record ClaimAssignedEvent(UUID claimId, UUID assignedUserId, AssignmentRole role, Instant occurredAt) {}
public record SurveySubmittedEvent(UUID claimId, UUID surveyorUserId, Instant occurredAt) {}
public record ClaimApprovedEvent(UUID claimId, UUID adjustorUserId, BigDecimal amount, Instant occurredAt) {}
public record ClaimRejectedEvent(UUID claimId, UUID adjustorUserId, String reason, Instant occurredAt) {}
public record RepairStatusUpdatedEvent(UUID claimId, UUID workshopId, RepairStatus status, Instant occurredAt) {}
public record PaymentCompletedEvent(UUID claimId, UUID paymentId, Instant occurredAt) {}
```

---

## 19. Testing Specification

### 19.1 Unit Tests

```text
ClaimServiceTest
ClaimWorkflowServiceTest
AssignmentRuleServiceTest
SurveyReportServiceTest
AdjudicationServiceTest
DocumentServiceTest
PaymentServiceTest
```

### 19.2 Integration Tests

Use Testcontainers PostgreSQL.

```text
ClaimRepositoryIT
WorkflowTransitionRepositoryIT
ReportingRepositoryIT
AuthControllerIT
ClaimControllerIT
DocumentControllerIT
```

### 19.3 Security Tests

```text
Customer cannot view another customer claim.
Surveyor cannot submit report for unassigned claim.
Adjustor cannot approve unassigned claim.
Workshop user cannot update claim assigned to another workshop.
Auditor cannot mutate claim.
Unauthenticated request returns 401.
Authenticated but unauthorized request returns 403.
```

### 19.4 Workflow Tests

```text
SUBMITTED -> CASE_ASSIGNED allowed for CASE_MANAGER with CLAIM_ASSIGN.
SUBMITTED -> APPROVED not allowed.
SURVEYOR_ASSIGNED -> SURVEY_SUBMITTED requires surveyor assignment.
ADJUSTOR_REVIEW -> APPROVED requires adjustor assignment and approved amount.
REPAIR_COMPLETED -> PAYMENT_PENDING requires final invoice.
PAYMENT_PENDING -> CLOSED not allowed before payment completion.
```

### 19.5 Architecture Tests with ArchUnit

```text
Controllers must not access repositories.
Repositories must not depend on controllers.
Services must not depend on web layer.
Domain modules must not use frontend/API-specific DTOs internally.
Common module must not depend on feature modules.
```

---

## 20. Docker Compose Runtime

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: eclaims
      POSTGRES_USER: eclaims
      POSTGRES_PASSWORD: eclaims
    ports:
      - "5435:5435"

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"

  mailhog:
    image: mailhog/mailhog
    ports:
      - "1025:1025"
      - "8025:8025"

  backend:
    build: ./eclaims-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - minio
      - mailhog
```

---

## 21. Seed Data Plan

### 21.1 Demo Users

```text
customer@nagarro.com / Password@123 / CUSTOMER
case.manager@nagarro.com / Password@123 / CASE_MANAGER
surveyor@nagarro.com / Password@123 / SURVEYOR
adjustor@nagarro.com / Password@123 / ADJUSTOR
workshop@nagarro.com / Password@123 / WORKSHOP_USER
auditor@nagarro.com / Password@123 / AUDITOR
regional.manager@nagarro.com / Password@123 / REGIONAL_MANAGER
admin@nagarro.com / Password@123 / ADMIN
```

### 21.2 Reference Data

```text
Regions:
- Northeast
- Midwest
- South
- West

Workshops:
- Partner Auto Repair NYC
- FastFix Collision Center
- United Auto Works

Policies:
- POL-1000001 assigned to customer demo user
- active auto policy
- collision coverage
- deductible amount
- coverage limit
```

---

## 22. Implementation Sequence for Copilot/Claude

Use this implementation sequence strictly:

```text
1. Generate base Spring Boot project and Maven dependencies.
2. Add application profiles and Docker Compose.
3. Implement common response, exceptions, and global exception handler.
4. Add Flyway migrations for RBAC, users, policies, claims.
5. Implement entities and repositories.
6. Implement auth and JWT security.
7. Seed users, roles, permissions, policies, workflow transitions.
8. Implement claim creation/list/detail/timeline.
9. Implement workflow transition service.
10. Implement assignment service.
11. Implement survey module.
12. Implement adjudication module.
13. Implement document module with MinIO storage.
14. Implement notification events and MailHog email sending.
15. Implement workshop module.
16. Implement payment mock module.
17. Implement reporting APIs.
18. Implement audit APIs.
19. Add Swagger annotations where useful.
20. Add unit and integration tests.
21. Add README and run instructions.
```

---

## 23. Backend Demo Flow

```text
1. Login as customer.
2. Create claim for POL-1000001.
3. Upload claim photo/police report.
4. Login as case manager.
5. Auto-assign case manager/surveyor/adjustor.
6. Login as surveyor.
7. Submit survey report.
8. Login as adjustor.
9. Approve claim amount.
10. Login as customer.
11. Select workshop.
12. Login as workshop user.
13. Submit work order and repair updates.
14. Submit final invoice.
15. Login as customer.
16. Complete mock payment.
17. Close claim.
18. Login as regional manager and view reports.
19. Login as auditor and view audit trail.
```

---

## 24. NFR Implementation Checklist

### Security

- BCrypt passwords.
- JWT validation.
- DB-backed permissions.
- Method-level authorization.
- Input validation.
- Secure file upload checks.
- Error response sanitization.
- Audit logs.

The solution must guard against common security threats such as OWASP Top 10 and use industry-proven security standards/protocols. citeturn1search2

### Scalability

- Stateless backend.
- Pagination.
- DB indexes.
- Object storage for files.
- Event-based notification.
- Future broker/outbox-ready design.

### Reliability

- Transaction boundaries in application services.
- Defensive workflow transition validation.
- Retry-ready notification events.
- Health endpoints.

### Performance

- Do not load binary documents in claim details.
- Use dedicated download endpoint.
- Index common reporting/filtering columns.
- Use pagination for all listing APIs.

The assignment expects 99% of services to complete within 5000 ms across peak and non-peak hours. citeturn1search2

### Observability

- Spring Actuator.
- Structured logs.
- Request correlation ID.
- Audit trail.
- Notification failure tracking.

---

## 25. Completion Criteria

Backend MVP is complete when:

```text
- Application starts with Docker Compose.
- Flyway migrations execute successfully.
- Seed users can log in.
- Swagger is available.
- Customer can submit claim.
- Documents can be uploaded and downloaded.
- Case manager can assign users.
- Surveyor can submit report.
- Adjustor can approve/reject claim.
- Workshop can update repair status.
- Payment can be simulated.
- Claim timeline shows full lifecycle.
- Notifications are created.
- Audit trail is visible.
- Reports return aggregate data.
- Security restrictions are validated.
- Critical tests pass.
```

---

## 26. Next Engineering Artifact

After this backend specification, generate the following files next:

```text
1. eClaims_API_Contracts.md
2. eClaims_Database_DDL_Flyway_Migrations.md
3. eClaims_Backend_Copilot_Implementation_Prompts.md
4. eClaims_Frontend_React_Implementation_Spec.md
5. eClaims_Solution_Approach_Document.md
6. eClaims_DAR_Workflow_Document_Management.md
7. eClaims_Effort_Estimation_WBS.md
```
