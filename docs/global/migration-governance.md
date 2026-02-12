# Migration Governance

This document defines the permanent rules for safe tenant migrations in AdminCraft.

## Core Policy

1. `Forward-only`: Existing versioned migrations (`V*.sql`) are immutable after merge.
2. `Repair via new versions`: Legacy drift fixes must be implemented as new `V*__repair_*.sql` migrations.
3. `Repeatable safety`: `R__*.sql` must use explicit column lists for every `INSERT`.
4. `Module order`: `core -> media -> component_library -> pagebuilder -> product` is mandatory.

## Why this policy exists

Legacy tenants may have partial/manual schema changes. Editing old migrations creates checksum churn and unstable sync behavior. Forward-only repairs keep history deterministic.

## Repair Migration Standard

Use `INFORMATION_SCHEMA` checks to guard:

- `AddColumnIfNotExists`
- `AddIndexIfNotExists`
- `CreateTableIfNotExists`
- FK existence checks via `REFERENTIAL_CONSTRAINTS`

Repair migrations must be idempotent and non-destructive by default.

## Prohibited

- Modifying or deleting previously released `V*.sql` files
- Relying on implicit column order in repeatable seeds
- Cross-module FK changes that violate module order

## CI Requirements

Every PR touching migration files must pass:

1. Versioned migration immutability check
2. Migration lint checks
3. Backend compile sanity

Workflow: `.github/workflows/migration-guardrails.yml`

Scripts:

- `scripts/migrations/check-versioned-immutability.sh`
- `scripts/migrations/lint-migrations.sh`

## Manual Execution (Local)

Run these before opening a migration PR:

```bash
bash scripts/migrations/check-versioned-immutability.sh
bash scripts/migrations/lint-migrations.sh
```

Notes:

- `check-versioned-immutability.sh` compares your branch against `origin/main` by default.
- If your base branch is different, pass it explicitly:

```bash
bash scripts/migrations/check-versioned-immutability.sh release/2026.02
```

- On Windows, use Git Bash or WSL for `bash` scripts.
