# AdminCraft

AdminCraft is a configurable project solutions platform built with Clean Architecture, emphasizing modular delivery, security, scalability, and performance.

Documentation: [`docs/README.md`](docs/README.md)

## ?? Project Anatomy

The project is structured as a monorepo containing both the backend and frontend applications:

```
AdminCraft/
+-- backend/            # Spring Boot 3 application (API)
+-- storefront/         # Angular 19 application (UI)
+-- docker/             # Docker configuration
+-- uploads/            # Tenant file uploads
```

## ?? Tech Stack

### Backend

- **Core**: Java 21, Spring Boot 3.3
- **Data**: Spring Data JPA, Hibernate, MySQL
- **Migrations**: Flyway (Database-per-tenant strategy)
- **Security**: Spring Security, JWT (JJWT), OWASP Encoder, Jsoup
- **Resilience**: Resilience4j (Rate Limiting)
- **Testing**: JUnit 5, Testcontainers, Awaitility

### Frontend

- **Core**: Angular 19, TypeScript, RxJS
- **UI/UX**: Angular Material 19, Tailwind CSS
- **State Management**: Angular Signals, RxJS
- **i18n**: Transloco
- **Charts**: ApexCharts
- **Editor**: Quill

## ?? Architecture

### Multi-Tenancy (Database-per-Tenant)

AdminCraft uses a robust **Database-per-Tenant** architecture for maximum data isolation and security.

- **Platform DB**: Manages tenant registry and global configurations.
- **Tenant DBs**: Each tenant has a dedicated physical database (`ac_subdomain_{id}`).
- **Routing**: `TenantContext` routes requests to the correct database dynamically using HikariCP connection pooling.

### Clean Architecture

The backend follows strict Clean Architecture principles to ensure maintainability and testability:

1. **Presentation**: Controllers, DTOs (Rest API)
2. **Application**: Service interfaces, Use Cases
3. **Domain**: Entities, Business Logic (Framework agnostic)
4. **Infrastructure**: Implementations (Persistence, Security, External Services)

## ?? Security Features

- **Input Validation**: Strict validation on all DTOs.
- **Sanitization**: XSS protection via OWASP Encoder and HTML sanitization.
- **Isolation**: Physical database separation prevents data leakage.
- **Rate Limiting**: API protection using Resilience4j.
