---
name: enhance-admin-site-or-media-components
description: Workflow command scaffold for enhance-admin-site-or-media-components in Craftive.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /enhance-admin-site-or-media-components

Use this workflow when working on **enhance-admin-site-or-media-components** in `Craftive`.

## Goal

Add or improve features in admin site or media components, often involving changes to multiple related Angular component files (HTML, TS, SCSS) and types.

## Common Files

- `storefront/src/app/modules/admin/custom/site/**/*.component.ts`
- `storefront/src/app/modules/admin/custom/site/**/*.component.html`
- `storefront/src/app/modules/admin/custom/site/**/*.component.scss`
- `storefront/src/app/modules/admin/custom/site/**/*.service.ts`
- `storefront/src/app/modules/admin/custom/site/**/*.types.ts`
- `storefront/src/app/modules/admin/custom/media/**/*.component.html`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Update or create component TypeScript files for new logic or features
- Update corresponding HTML and SCSS files for UI changes
- Modify or add related types and services
- Update i18n files if new text is introduced

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.