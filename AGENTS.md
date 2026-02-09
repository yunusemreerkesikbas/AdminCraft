# Agent Selection Rules (Codex)

This project uses agent profiles stored in `.codex/agents`. When a new request arrives, select and apply the most relevant agent(s) based on the user's prompt.

## Project Context (from CLAUDE.md)
- Stack: Spring Boot 3.3.5, Java 21, Spring Data JPA, MySQL, Flyway, Resilience4j
- Frontend: Angular 19, TypeScript 5.6.3, Signals, RxJS 7, Material Design, TailwindCSS
- Architecture: Multi-Tenant Clean Architecture (DB-per-tenant). Business logic must live in Application layer.
- Windows OS: Prefer PowerShell/CMD-friendly commands.
- Flyway: Global sequential versions per module, no idempotent DDL, module execution order is critical.

## Primary Selection Heuristics
- Frontend UI, CSS, layout, component styling, SPA pages -> `.codex/agents/frontend-developer.md`
- UI/UX reviews, design critique, visual polish, accessibility, design systems -> `.codex/agents/ui-ux-designer.md`
- Backend APIs, services, infrastructure code -> `.codex/agents/backend-developer.md`
- Database schema, migrations, indexing, query design -> `.codex/agents/database-architect.md`
- Debugging errors, stack traces, failing tests -> `.codex/agents/debug-specialist.md`
- Code review requests, PR review, audit for bugs -> `.codex/agents/code-reviewer.md`
- Deployment, CI/CD, build pipelines -> `.codex/agents/deployment-engineer.md`
- Prompt writing or system prompt design -> `.codex/agents/prompt-engineer.md`
- Search/lookup strategies, research-heavy tasks -> `.codex/agents/search-specialist.md`
- Task breakdowns, planning, project scoping -> `.codex/agents/task-decomposition-expert.md`
- TypeScript-specific issues, typing, tsconfig -> `.codex/agents/typescript-pro.md`
- Context handling across many files or sessions -> `.codex/agents/context-manager.md`
- General software engineering when none of the above fits -> `.codex/agents/ai-engineer.md`

## Multi-Agent Rules
- Use the minimal set of agents required to cover the request.
- If multiple agents apply, use them in this order:
  1. `task-decomposition-expert` (if the task is large or ambiguous)
  2. Primary domain agent (frontend/backend/database/etc.)
  3. `code-reviewer` (only when explicitly asked to review)

## Architecture Guardrails
- Backend changes must respect Clean Architecture boundaries (Presentation/Application/Domain/Infrastructure).
- All business logic belongs in Application layer; frontend is display and data capture only.
- Multi-tenant DB-per-tenant: no `tenant_id` columns; context is via `TenantContext`.
- For migrations, follow module order and update seeds when schema changes.

## Tie-Breaks
- If the user mentions "design" or "UX" explicitly, prefer `ui-ux-designer` over `frontend-developer`.
- If the user mentions "TypeScript" or `.ts` errors, include `typescript-pro`.
- If the user asks "why isn't X working", include `debug-specialist`.
