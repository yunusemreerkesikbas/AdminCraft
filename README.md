# craftive

craftive is a configurable multi-tenant platform built with Clean Architecture, emphasizing modular delivery, security, scalability, and performance. The monorepo ships the backend control plane, the Angular admin application, and the Next.js demo/reference storefront used as the tenant storefront base.

Documentation: [`docs/README.md`](docs/README.md)

## Project Anatomy

The project is structured as a monorepo containing the backend, admin frontend, and demo/reference storefront:

```
craftive/
+-- backend/            # Spring Boot 3 application (API)
+-- storefront/         # Angular 19 admin application
+-- storefront-nextjs/  # Next.js demo/reference storefront
+-- docker/             # Docker configuration
+-- docs/               # Architecture, module, and ops documentation
+-- uploads/            # Tenant file uploads
```

## Tech Stack

### Backend

- **Core**: Java 21, Spring Boot 3.3
- **Data**: Spring Data JPA, Hibernate, MySQL
- **Migrations**: Flyway (Database-per-tenant strategy)
- **Security**: Spring Security, JWT (JJWT), OWASP Encoder, Jsoup
- **Resilience**: Resilience4j (Rate Limiting)
- **Testing**: JUnit 5, Testcontainers, Awaitility

### Frontend

- **Admin UI**: Angular 19, TypeScript, RxJS, Angular Signals
- **Headless Storefront**: Next.js App Router, React Server Components, next-intl
- **UI/UX**: Angular Material 19, Tailwind CSS
- **Charts**: ApexCharts
- **Editor**: Quill

## Architecture

### Multi-Tenancy (Database-per-Tenant)

craftive uses a robust **Database-per-Tenant** architecture for maximum data isolation and security.

- **Platform DB**: Manages tenant registry and global configurations.
- **Tenant DBs**: Each tenant has a dedicated physical database (`ac_subdomain_{id}`).
- **Routing**: `TenantContext` routes requests to the correct database dynamically using HikariCP connection pooling.

### Clean Architecture

The backend follows strict Clean Architecture principles to ensure maintainability and testability:

1. **Presentation**: Controllers, DTOs (Rest API)
2. **Application**: Service interfaces, Use Cases
3. **Domain**: Entities, Business Logic (Framework agnostic)
4. **Infrastructure**: Implementations (Persistence, Security, External Services)

## Storefront Model

- `storefront-nextjs/` is the demo/reference storefront deployed by this platform repository in stage and prod.
- Tenant storefronts are created by forking `storefront-nextjs/`, keeping the shared core CMS/runtime contract, and replacing theme-specific implementation under `components/theme/`.
- The Angular `storefront/` app remains the admin/control-panel frontend. Public tenant storefront delivery is handled by Next.js.

## Security Features

- **Input Validation**: Strict validation on all DTOs.
- **Sanitization**: XSS protection via OWASP Encoder and HTML sanitization.
- **Isolation**: Physical database separation prevents data leakage.
- **Rate Limiting**: API protection using Resilience4j.
