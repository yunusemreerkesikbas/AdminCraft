---
name: refactor-and-standardize-service-dtos-and-validation
description: Workflow command scaffold for refactor-and-standardize-service-dtos-and-validation in Craftive.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /refactor-and-standardize-service-dtos-and-validation

Use this workflow when working on **refactor-and-standardize-service-dtos-and-validation** in `Craftive`.

## Goal

Refactors DTOs, validation, and service logic for clarity, consistency, and maintainability, often deprecating old structures and introducing new standardized classes.

## Common Files

- `backend/src/main/java/com/backend/application/dto/impex/ImpExRequest.java`
- `backend/src/main/java/com/backend/application/service/SiteAnalyticsServiceImpl.java`
- `backend/src/main/java/com/backend/application/service/SiteInsightsServiceImpl.java`
- `backend/src/main/java/com/backend/application/service/analytics/SiteDataStatus.java`
- `backend/src/main/java/com/backend/presentation/controller/ImpExController.java`
- `backend/src/main/java/com/backend/presentation/dto/request/impex/ImpExRequest.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Deprecate or update DTOs, removing or adjusting validation annotations.
- Introduce new classes (e.g., status enums or constants) to standardize logic.
- Refactor service implementations to use new standardized structures.
- Update related controllers and helper classes for new logic.
- Update or add tests for affected services.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.