
# eClaims Backend — Full API Contracts

**Project:** Nagarro eClaims MVP  
**Backend:** Java 25 + Spring Boot 4.1.0  
**API Base Path:** `/api/v1`  
**Auth:** Bearer JWT  
**Response Envelope:** All JSON APIs return `ApiResponse<T>` unless explicitly returning binary document stream.

---

## 1. Standard Response Models

### 1.1 Success Envelope

```json
{
  "success": true,
  "message": "Operation completed successfully.",
  "data": {},
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

### 1.2 Error Envelope

```json
{
  "success": false,
  "message": "Request failed.",
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "One or more fields are invalid.",
    "fieldErrors": [
      { "field": "policyNumber", "message": "must not be blank" }
    ]
  },
  "timestamp": "2026-06-30T10:30:00Z"
}
```

### 1.3 Common HTTP Codes

```text
200 OK                  Successful read/update
201 Created             Resource created
204 No Content           Successful delete/no body
400 Bad Request          Invalid request/business validation
401 Unauthorized         Missing/invalid token
403 Forbidden            Authenticated but unauthorized
404 Not Found            Resource not found
409 Conflict             Duplicate/invalid state conflict
422 Unprocessable Entity Workflow/business rule violation
500 Internal Server Error Unexpected server error
```

---

## 2. Auth APIs

## 2.1 Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

### Required Permission

```text
Public
```

### Request

```json
{
  "email": "customer@nagarro.com",
  "password": "Password@123"
}
```

### Response `200`

```json
{
  "success": true,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "11111111-1111-1111-1111-111111111111",
      "fullName": "John Customer",
      "email": "customer@nagarro.com",
      "roles": ["CUSTOMER"],
      "permissions": ["CLAIM_CREATE", "CLAIM_VIEW_SELF", "DOCUMENT_UPLOAD"]
    }
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

### Errors

```text
401 INVALID_CREDENTIALS
403 USER_INACTIVE
```

---

## 2.2 Register Customer Using Policy

```http
POST /api/v1/auth/register-customer
Content-Type: application/json
```

### Required Permission

```text
Public
```

### Request

```json
{
  "policyNumber": "POL-1000001",
  "lastName": "Customer",
  "email": "customer@nagarro.com",
  "password": "Password@123",
  "phone": "+12025550100"
}
```

### Response `201`

```json
{
  "success": true,
  "message": "Customer registered successfully.",
  "data": {
    "userId": "11111111-1111-1111-1111-111111111111",
    "customerId": "22222222-2222-2222-2222-222222222222",
    "email": "customer@nagarro.com"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

### Errors

```text
400 POLICY_NOT_ACTIVE
404 POLICY_NOT_FOUND
409 EMAIL_ALREADY_REGISTERED
```

---

## 2.3 Current User Profile

```http
GET /api/v1/auth/me
Authorization: Bearer <token>
```

### Required Permission

```text
Authenticated user
```

### Response `200`

```json
{
  "success": true,
  "message": "Current user profile fetched.",
  "data": {
    "id": "11111111-1111-1111-1111-111111111111",
    "fullName": "John Customer",
    "email": "customer@nagarro.com",
    "roles": ["CUSTOMER"],
    "permissions": ["CLAIM_CREATE", "CLAIM_VIEW_SELF"]
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 3. Customer APIs

## 3.1 Get Customer Profile

```http
GET /api/v1/customers/me
Authorization: Bearer <token>
```

### Required Permission

```text
CUSTOMER role
```

### Response

```json
{
  "success": true,
  "message": "Customer profile fetched.",
  "data": {
    "customerId": "22222222-2222-2222-2222-222222222222",
    "customerNumber": "CUST-10001",
    "firstName": "John",
    "lastName": "Customer",
    "email": "customer@nagarro.com",
    "phone": "+12025550100",
    "correspondenceAddress": {
      "addressLine1": "10 Main Street",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "billingCycle": "MONTHLY"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

## 3.2 Update Correspondence Address

```http
PATCH /api/v1/customers/me/correspondence-address
Authorization: Bearer <token>
```

### Required Permission

```text
CUSTOMER
```

### Request

```json
{
  "addressLine1": "11 Updated Street",
  "city": "New York",
  "state": "NY",
  "zipCode": "10002",
  "country": "USA"
}
```

### Response

```json
{
  "success": true,
  "message": "Correspondence address updated.",
  "data": null,
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

## 3.3 Update Billing Cycle

```http
PATCH /api/v1/customers/me/billing-cycle
Authorization: Bearer <token>
```

### Request

```json
{ "billingCycle": "QUARTERLY" }
```

---

## 4. Policy APIs

## 4.1 Get My Policies

```http
GET /api/v1/policies/me
Authorization: Bearer <token>
```

### Required Permission

```text
CUSTOMER
```

### Response

```json
{
  "success": true,
  "message": "Policies fetched.",
  "data": [
    {
      "policyId": "33333333-3333-3333-3333-333333333333",
      "policyNumber": "POL-1000001",
      "policyStatus": "ACTIVE",
      "vehicleVin": "1HGCM82633A004352",
      "vehicleMake": "Toyota",
      "vehicleModel": "Camry",
      "vehicleYear": 2022,
      "effectiveDate": "2026-01-01",
      "expiryDate": "2026-12-31",
      "deductibleAmount": 500.00,
      "coverageLimit": 25000.00
    }
  ],
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

## 4.2 Get Policy by Number

```http
GET /api/v1/policies/{policyNumber}
Authorization: Bearer <token>
```

### Required Permission

```text
CUSTOMER for own policy, CLAIM_VIEW_ALL for internal users
```

---

## 5. Claim APIs

## 5.1 Create Claim

```http
POST /api/v1/claims
Authorization: Bearer <token>
Content-Type: application/json
```

### Required Permission

```text
CLAIM_CREATE
```

### Ownership Rule

```text
Customer can create claim only for own active policy.
```

### Request

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

### Response `201`

```json
{
  "success": true,
  "message": "Claim submitted successfully.",
  "data": {
    "claimId": "44444444-4444-4444-4444-444444444444",
    "claimNumber": "CLM-2026-000001",
    "status": "SUBMITTED",
    "createdAt": "2026-06-30T10:30:00Z"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

### Errors

```text
400 INCIDENT_DATE_IN_FUTURE
403 POLICY_NOT_OWNED_BY_CUSTOMER
404 POLICY_NOT_FOUND
409 DUPLICATE_ACTIVE_CLAIM_FOR_INCIDENT
```

---

## 5.2 List Claims

```http
GET /api/v1/claims?page=0&size=20&status=SUBMITTED&claimNumber=CLM-2026-000001
Authorization: Bearer <token>
```

### Required Permission

```text
CLAIM_VIEW_SELF, CLAIM_VIEW_ASSIGNED, or CLAIM_VIEW_ALL
```

### Query Params

```text
page: integer default 0
size: integer default 20 max 100
status: optional ClaimStatus
claimNumber: optional string
policyNumber: optional string
fromDate: optional yyyy-MM-dd
toDate: optional yyyy-MM-dd
region: optional string
assignedToMe: optional boolean
```

### Response

```json
{
  "success": true,
  "message": "Claims fetched.",
  "data": {
    "content": [
      {
        "claimId": "44444444-4444-4444-4444-444444444444",
        "claimNumber": "CLM-2026-000001",
        "policyNumber": "POL-1000001",
        "customerName": "John Customer",
        "claimType": "ACCIDENT",
        "status": "SUBMITTED",
        "incidentDate": "2026-06-20",
        "submittedAt": "2026-06-30T10:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 5.3 Get Claim Details

```http
GET /api/v1/claims/{claimId}
Authorization: Bearer <token>
```

### Required Permission

```text
CLAIM_VIEW_SELF, CLAIM_VIEW_ASSIGNED, or CLAIM_VIEW_ALL
```

### Response

```json
{
  "success": true,
  "message": "Claim details fetched.",
  "data": {
    "claimId": "44444444-4444-4444-4444-444444444444",
    "claimNumber": "CLM-2026-000001",
    "status": "SUBMITTED",
    "claimType": "ACCIDENT",
    "policy": {
      "policyNumber": "POL-1000001",
      "coverageLimit": 25000.00,
      "deductibleAmount": 500.00
    },
    "customer": {
      "customerNumber": "CUST-10001",
      "fullName": "John Customer"
    },
    "incident": {
      "incidentDate": "2026-06-20",
      "incidentTime": "15:30:00",
      "addressLine1": "5th Avenue",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA",
      "description": "Vehicle rear-ended at traffic signal."
    },
    "assignments": [],
    "surveyReport": null,
    "adjustorDecision": null,
    "workshop": null,
    "payment": null
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 5.4 Get Claim Timeline

```http
GET /api/v1/claims/{claimId}/timeline
Authorization: Bearer <token>
```

### Response

```json
{
  "success": true,
  "message": "Claim timeline fetched.",
  "data": [
    {
      "fromStatus": null,
      "toStatus": "SUBMITTED",
      "changedBy": "John Customer",
      "changedByRole": "CUSTOMER",
      "comments": "Claim submitted.",
      "createdAt": "2026-06-30T10:30:00Z"
    }
  ],
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 5.5 Update Claim Status

```http
PATCH /api/v1/claims/{claimId}/status
Authorization: Bearer <token>
```

### Required Permission

```text
CLAIM_STATUS_UPDATE or transition-specific permission
```

### Request

```json
{
  "targetStatus": "CASE_ASSIGNED",
  "reason": "Case manager assigned",
  "comments": "Initial assignment completed."
}
```

### Response

```json
{
  "success": true,
  "message": "Claim status updated.",
  "data": {
    "claimId": "44444444-4444-4444-4444-444444444444",
    "fromStatus": "SUBMITTED",
    "toStatus": "CASE_ASSIGNED"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 6. Assignment APIs

## 6.1 Auto Assign Claim

```http
POST /api/v1/claims/{claimId}/assignments/auto
Authorization: Bearer <token>
```

### Required Permission

```text
CLAIM_ASSIGN
```

### Response

```json
{
  "success": true,
  "message": "Claim assigned successfully.",
  "data": {
    "claimId": "44444444-4444-4444-4444-444444444444",
    "assignments": [
      {
        "assignmentRole": "CASE_MANAGER",
        "assignedUserId": "55555555-5555-5555-5555-555555555555",
        "assignedUserName": "Case Manager One"
      },
      {
        "assignmentRole": "SURVEYOR",
        "assignedUserId": "66666666-6666-6666-6666-666666666666",
        "assignedUserName": "Surveyor One"
      },
      {
        "assignmentRole": "ADJUSTOR",
        "assignedUserId": "77777777-7777-7777-7777-777777777777",
        "assignedUserName": "Adjustor One"
      }
    ]
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

## 6.2 Reassign Claim

```http
POST /api/v1/claims/{claimId}/assignments/reassign
Authorization: Bearer <token>
```

### Request

```json
{
  "assignmentRole": "SURVEYOR",
  "newAssignedUserId": "66666666-6666-6666-6666-666666666666",
  "reason": "Previous surveyor unavailable."
}
```

---

## 6.3 Get Claim Assignments

```http
GET /api/v1/claims/{claimId}/assignments
Authorization: Bearer <token>
```

---

## 7. Survey APIs

## 7.1 Get Surveyor Claim Queue

```http
GET /api/v1/surveyor/claims?page=0&size=20
Authorization: Bearer <token>
```

### Required Permission

```text
SURVEY_VIEW_ASSIGNED
```

## 7.2 Submit Survey Report

```http
POST /api/v1/claims/{claimId}/survey-reports
Authorization: Bearer <token>
```

### Required Permission

```text
SURVEY_SUBMIT
```

### Ownership Rule

```text
Surveyor can submit only for assigned claim.
```

### Request

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

### Response

```json
{
  "success": true,
  "message": "Survey report submitted.",
  "data": {
    "surveyReportId": "88888888-8888-8888-8888-888888888888",
    "claimId": "44444444-4444-4444-4444-444444444444",
    "claimStatus": "ADJUSTOR_REVIEW"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 8. Adjudication APIs

## 8.1 Get Adjustor Claim Queue

```http
GET /api/v1/adjustor/claims?page=0&size=20
Authorization: Bearer <token>
```

### Required Permission

```text
ADJUDICATION_VIEW_ASSIGNED
```

## 8.2 Submit Adjustor Decision

```http
POST /api/v1/claims/{claimId}/adjudications
Authorization: Bearer <token>
```

### Required Permission

```text
ADJUDICATION_APPROVE or ADJUDICATION_REJECT
```

### Request

```json
{
  "decision": "APPROVED",
  "approvedAmount": 1600.00,
  "currency": "USD",
  "remarks": "Approved as per collision coverage after deductible."
}
```

### Response

```json
{
  "success": true,
  "message": "Adjustor decision submitted.",
  "data": {
    "decisionId": "99999999-9999-9999-9999-999999999999",
    "claimId": "44444444-4444-4444-4444-444444444444",
    "decision": "APPROVED",
    "claimStatus": "APPROVED"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 9. Workshop APIs

## 9.1 Search Workshops

```http
GET /api/v1/workshops/search?zipCode=10001
Authorization: Bearer <token>
```

### Required Permission

```text
Authenticated user
```

### Response

```json
{
  "success": true,
  "message": "Workshops fetched.",
  "data": [
    {
      "workshopId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
      "name": "Partner Auto Repair NYC",
      "partnerCode": "WS-NYC-001",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "phone": "+12025550888"
    }
  ],
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

## 9.2 Select Workshop

```http
POST /api/v1/claims/{claimId}/workshop-selection
Authorization: Bearer <token>
```

### Required Permission

```text
WORKSHOP_SELECT
```

### Request

```json
{
  "workshopId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
}
```

## 9.3 Submit Work Order

```http
POST /api/v1/workshop/claims/{claimId}/work-orders
Authorization: Bearer <token>
```

### Request

```json
{
  "estimateAmount": 1750.00,
  "currency": "USD",
  "description": "Replace rear bumper, trunk alignment, repaint affected area."
}
```

## 9.4 Submit Repair Update

```http
POST /api/v1/workshop/claims/{claimId}/repair-updates
Authorization: Bearer <token>
```

### Request

```json
{
  "repairStatus": "IN_PROGRESS",
  "progressPercentage": 45,
  "expectedDeliveryDate": "2026-07-08",
  "remarks": "Bumper replacement completed, paint work pending."
}
```

## 9.5 Submit Final Invoice

```http
POST /api/v1/workshop/claims/{claimId}/final-invoice
Authorization: Bearer <token>
```

### Request

```json
{
  "invoiceNumber": "INV-WS-10001",
  "invoiceAmount": 1700.00,
  "currency": "USD"
}
```

---

## 10. Document APIs

## 10.1 Upload Claim Document

```http
POST /api/v1/claims/{claimId}/documents
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

### Required Permission

```text
DOCUMENT_UPLOAD
```

### Form Data

```text
file: binary
documentType: CLAIM_PHOTO | POLICE_REPORT | SURVEY_REPORT | WORK_ORDER | REPAIR_ESTIMATE | FINAL_INVOICE | OTHER
description: optional string
```

### Response

```json
{
  "success": true,
  "message": "Document uploaded successfully.",
  "data": {
    "documentId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
    "claimId": "44444444-4444-4444-4444-444444444444",
    "documentType": "CLAIM_PHOTO",
    "originalFileName": "accident-photo.jpg",
    "contentType": "image/jpeg",
    "fileSize": 204800,
    "version": 1,
    "uploadedAt": "2026-06-30T10:30:00Z"
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

## 10.2 List Claim Documents

```http
GET /api/v1/claims/{claimId}/documents
Authorization: Bearer <token>
```

## 10.3 Download Document

```http
GET /api/v1/documents/{documentId}/download
Authorization: Bearer <token>
```

### Response

```text
Binary stream with Content-Disposition attachment.
```

## 10.4 Delete Document

```http
DELETE /api/v1/documents/{documentId}
Authorization: Bearer <token>
```

---

## 11. Notification APIs

## 11.1 List My Notifications

```http
GET /api/v1/notifications?page=0&size=20&unreadOnly=false
Authorization: Bearer <token>
```

### Required Permission

```text
NOTIFICATION_VIEW
```

## 11.2 Mark Notification as Read

```http
PATCH /api/v1/notifications/{notificationId}/read
Authorization: Bearer <token>
```

---

## 12. Payment APIs

## 12.1 Initiate Payment

```http
POST /api/v1/claims/{claimId}/payments/initiate
Authorization: Bearer <token>
```

### Required Permission

```text
PAYMENT_INITIATE
```

### Request

```json
{
  "amount": 200.00,
  "currency": "USD",
  "paymentMethod": "CARD"
}
```

## 12.2 Mock Complete Payment

```http
POST /api/v1/claims/{claimId}/payments/mock-complete
Authorization: Bearer <token>
```

### Required Permission

```text
PAYMENT_COMPLETE
```

---

## 13. Reporting APIs

All reporting APIs require `REPORT_VIEW`.

## 13.1 Claims Summary

```http
GET /api/v1/reports/claims-summary?fromDate=2026-01-01&toDate=2026-06-30
Authorization: Bearer <token>
```

### Response

```json
{
  "success": true,
  "message": "Claims summary fetched.",
  "data": {
    "totalClaims": 120,
    "openClaims": 45,
    "closedClaims": 70,
    "rejectedClaims": 5,
    "totalApprovedAmount": 150000.00,
    "averageProcessingDays": 6.4
  },
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

## 13.2 Claims Ageing

```http
GET /api/v1/reports/claims-ageing
Authorization: Bearer <token>
```

## 13.3 Claims by Region

```http
GET /api/v1/reports/claims-by-region
Authorization: Bearer <token>
```

## 13.4 Claims Processing Time

```http
GET /api/v1/reports/claims-processing-time
Authorization: Bearer <token>
```

## 13.5 Claims by Role

```http
GET /api/v1/reports/claims-by-role?role=ADJUSTOR
Authorization: Bearer <token>
```

## 13.6 Fraud Indicators

```http
GET /api/v1/reports/fraud-indicators
Authorization: Bearer <token>
```

---

## 14. Audit APIs

## 14.1 Get Claim Audit Trail

```http
GET /api/v1/audit/claims/{claimId}
Authorization: Bearer <token>
```

### Required Permission

```text
AUDIT_VIEW
```

### Response

```json
{
  "success": true,
  "message": "Audit trail fetched.",
  "data": [
    {
      "action": "CLAIM_CREATED",
      "actorUserId": "11111111-1111-1111-1111-111111111111",
      "actorRole": "CUSTOMER",
      "entityType": "CLAIM",
      "entityId": "44444444-4444-4444-4444-444444444444",
      "createdAt": "2026-06-30T10:30:00Z"
    }
  ],
  "error": null,
  "timestamp": "2026-06-30T10:30:00Z"
}
```

---

## 15. Admin/RBAC APIs

## 15.1 List Users

```http
GET /api/v1/admin/users?page=0&size=20
Authorization: Bearer <token>
```

### Required Permission

```text
ADMIN_MANAGE_USERS
```

## 15.2 List Roles

```http
GET /api/v1/admin/roles
Authorization: Bearer <token>
```

## 15.3 List Permissions

```http
GET /api/v1/admin/permissions
Authorization: Bearer <token>
```

## 15.4 Assign Role to User

```http
POST /api/v1/admin/users/{userId}/roles
Authorization: Bearer <token>
```

### Request

```json
{ "roleCode": "SURVEYOR" }
```

## 15.5 Assign Permission to Role

```http
POST /api/v1/admin/roles/{roleCode}/permissions
Authorization: Bearer <token>
```

### Request

```json
{ "permissionCode": "CLAIM_ASSIGN" }
```
