# AdminCraft Copilot Instructions

## Big picture
- Monorepo: backend in `backend/` (Spring Boot 3.3.5, Java 21) and frontend in `storefront/` (Angular 19, TS 5.6).
- Multi-tenant, database-per-tenant: platform DB `platform_management` + tenant DBs `ac_tenant_{id}`; no `tenant_id` columns.
- Clean Architecture backend: Presentation → Application → Domain ← Infrastructure. Business logic lives in Application services only.

## Critical workflows (Windows)
- Infra: `docker compose up -d`
- Backend: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- Frontend: `cd storefront; npm install; npm run start` (Angular dev server on 4200)
- Swagger: `http://localhost:8080/api/swagger-ui/index.html`

## Backend conventions (Spring Boot)
- Layering and packages: `com.backend.presentation`, `com.backend.application`, `com.backend.domain`, `com.backend.infrastructure`.
- Controllers return `ResponseEntity<ApiResponse<T>>` and use `@Valid` DTOs.
- Tenant context is set/cleared in `TenantFilter` with `try/finally`; MDC includes `tenantId`, `tenantDb`, `correlationId`.
- Entities extend `BaseEntity` (and i18n entities extend `BaseI18nEntity`).
- Migrations: platform in `backend/src/main/resources/db/platform/`; tenant modules in `backend/src/main/resources/db/tenant/{module}/` with global sequential versioning.

## Frontend conventions (Angular 19)
- Standalone components, `OnPush`, selector prefix `spa-`.
- Use Angular 19 control flow (`@if`, `@for`, `@switch`) and Signals (`itemsSig`, `isLoadingSig`).
- Services extend `CrudHttpService`; lists/forms extend `BaseCrudListComponent` / `BaseCrudFormComponent` (see `storefront/src/app/core/crud/`).
- Use shared form controls from `storefront/src/app/shared/components/custom-ui/`.

## Integration points & docs
- Tenant modules are defined in `backend/src/main/java/com/backend/domain/enums/ModuleCode.java`.
- Architecture and cross-cutting patterns are documented in `docs/global/` (start at `docs/README.md`).
- Public delivery APIs live under `/api/cms/**` (see `docs/modules/cms-delivery.md`).
