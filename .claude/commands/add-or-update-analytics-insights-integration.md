---
name: add-or-update-analytics-insights-integration
description: Workflow command scaffold for add-or-update-analytics-insights-integration in Craftive.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /add-or-update-analytics-insights-integration

Use this workflow when working on **add-or-update-analytics-insights-integration** in `Craftive`.

## Goal

Implements or refactors analytics/insights integrations, including service, port, adapter, DTO, controller, and documentation updates.

## Common Files

- `backend/src/main/java/com/backend/application/dto/response/SiteAnalyticsSummaryAppDto.java`
- `backend/src/main/java/com/backend/application/dto/response/SiteInsightsSummaryAppDto.java`
- `backend/src/main/java/com/backend/application/service/SiteAnalyticsService.java`
- `backend/src/main/java/com/backend/application/service/SiteAnalyticsServiceImpl.java`
- `backend/src/main/java/com/backend/application/service/SiteInsightsService.java`
- `backend/src/main/java/com/backend/application/service/SiteInsightsServiceImpl.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Create or update DTOs for analytics/insights data transfer.
- Implement or modify service and service implementation classes for analytics/insights.
- Add or update domain ports and infrastructure adapters for external integrations.
- Update configuration files and properties related to analytics/insights.
- Modify controllers to expose new or updated endpoints.

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.