# README for Copilot - eClaims Backend Implementation

You are implementing the eClaims backend MVP.

Use the following documents as mandatory implementation references:

1. docs/backend/00_Backend_Master_Implementation_Spec.md
2. docs/backend/01_API_Contracts_Full.md
3. docs/backend/02_Flyway_Migrations_Full.md
4. docs/backend/03_RBAC_Workflow_Matrices.md
5. docs/backend/04_Copilot_Claude_Backend_Implementation_Prompts.md

Important project rules:

- Use package name: com.nagarro.eclaims
- Use Java 25
- Use Spring Boot 4.1.0
- Use Maven
- Use YAML configuration
- Use PostgreSQL
- Use Flyway for all database changes
- Do not use Hibernate ddl-auto create/update
- Use ddl-auto validate only
- Use DB-backed roles and permissions
- Use permission-based authorization, not only hardcoded roles
- Every claim status change must go through ClaimWorkflowService
- Every important business action must write audit logs
- Use ApiResponse<T> wrapper for all JSON APIs
- Controllers must not call repositories directly
- Services own transaction boundaries
- Repositories must not contain business logic
- Follow the implementation prompts sequentially from 04_Copilot_Claude_Backend_Implementation_Prompts.md