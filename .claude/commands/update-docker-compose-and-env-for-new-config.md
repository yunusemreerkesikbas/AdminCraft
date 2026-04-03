---
name: update-docker-compose-and-env-for-new-config
description: Workflow command scaffold for update-docker-compose-and-env-for-new-config in Craftive.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /update-docker-compose-and-env-for-new-config

Use this workflow when working on **update-docker-compose-and-env-for-new-config** in `Craftive`.

## Goal

Synchronize environment variables and configuration settings across docker-compose and .env files for new features or integrations (e.g., SEO API keys, rate limiting).

## Common Files

- `.env.example`
- `storefront-nextjs/.env.local.example`
- `docker-compose.prod.yml`
- `docker-compose.stage.yml`
- `storefront-nextjs/lib/core/config/runtime-env.ts`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Add or update environment variable in .env.example and/or .env.local.example
- Update docker-compose.prod.yml and docker-compose.stage.yml to include new variable or adjust settings
- Optionally update related runtime config files (e.g., runtime-env.ts)

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.