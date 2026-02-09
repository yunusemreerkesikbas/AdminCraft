## Description

<!-- Provide a brief description of the changes in this PR -->

## Type of Change

- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] 📝 Documentation update
- [ ] ♻️ Code refactoring
- [ ] 🎨 UI/UX improvement

## Related Issue

<!-- Link to the issue this PR addresses -->
Closes #

## Changes Made

<!-- List the main changes -->

- 
- 
- 

## Architecture Checklist

### Clean Architecture (CRITICAL)
- [ ] No layer boundary violations (Presentation → Application → Domain ← Infrastructure)
- [ ] Business logic is in Application layer only
- [ ] Domain layer has no external dependencies
- [ ] Infrastructure doesn't import Presentation

### Multi-Tenancy
- [ ] No `tenant_id` columns added (using database-per-tenant)
- [ ] TenantContext properly set/cleared in try-finally
- [ ] Platform entities use `@Qualifier("platformDataSource")` if applicable
- [ ] MDC logging includes tenantId, tenantDb, correlationId

## Code Quality Checklist

### Backend (Java/Spring Boot)
- [ ] Constructor injection only (no @Autowired)
- [ ] Using `jakarta.*` packages (not `javax.*`)
- [ ] DTOs have Request/Response suffix
- [ ] @Valid on controller parameters
- [ ] JPQL uses parameterized queries
- [ ] No System.out.println or e.printStackTrace()
- [ ] @Transactional for multi-step operations

### Frontend (Angular/TypeScript)
- [ ] Using Angular 19 control flow (`@if`, `@for`, not `*ngIf`/`*ngFor`/`*ngSwitch`)
- [ ] Signals for state management
- [ ] OnPush change detection
- [ ] Standalone components (no NgModules)
- [ ] Component prefix: spa-
- [ ] Signal variables end with Sig suffix
- [ ] Private fields use # prefix
- [ ] Subscriptions cleaned up with takeUntil or take(1)
- [ ] No console.log statements
- [ ] trackBy in @for loops

### Database (Flyway)
- [ ] Sequential version number
- [ ] utf8mb4 charset with utf8mb4_unicode_ci collation
- [ ] No idempotent DDL logic
- [ ] Seeds updated if schema changed

## Security Checklist

- [ ] Input validation with Bean Validation (@NotNull, @Size, @Pattern)
- [ ] No SQL injection vulnerabilities (parameterized queries)
- [ ] No sensitive data logged (passwords, tokens, PII)
- [ ] API errors truncated (500 chars max)
- [ ] Authorization checks added where needed (@PreAuthorize)

## Testing

- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] Edge cases considered

### Test Coverage
<!-- Describe what was tested -->

- 
- 

## Screenshots (if applicable)

<!-- Add screenshots for UI changes -->

## Performance Impact

<!-- Does this PR affect performance? -->

- [ ] No performance impact
- [ ] Performance improved
- [ ] Performance impact acceptable (explain below)

## Migration Required

- [ ] Database migration included
- [ ] Data migration required
- [ ] Configuration changes needed
- [ ] No migration needed

## Reviewer Notes

<!-- Any specific areas you want reviewers to focus on? -->

---

## Pre-Merge Checklist (Reviewer)

- [ ] Code follows all architecture rules
- [ ] No security vulnerabilities introduced
- [ ] Tests pass and coverage is adequate
- [ ] Documentation updated if needed
- [ ] Breaking changes documented
- [ ] Ready to merge

---

**cc: @copilot please review this PR according to `.github/copilot-instructions.md`**
