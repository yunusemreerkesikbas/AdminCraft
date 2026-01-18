# Dialogs & Shared UI

This project standardizes dialog UX and form layouts to keep the Admin UI consistent across modules.

## Dialog base classes

The codebase uses reusable dialog base classes (Angular) to unify behavior:

- `SpaDialogBase` → lifecycle helpers (close/cancel, submitting state, active tab)
- `SpaFormDialog` → form submission wrapper (create/edit)
- `SpaLocalizedFormDialog` → multi-language form structure (general + language tabs)

The legacy documentation contains canonical examples; in new implementations, prefer these base classes instead of ad-hoc dialog logic.

Code locations (source of truth):

- Base: `storefront/src/app/shared/components/spa-dialog-base/`
- Form dialog: `storefront/src/app/shared/components/spa-form-dialog/`
- Localized form dialog: `storefront/src/app/shared/components/spa-localized-form-dialog/`

## `spa-dialog` wrapper

Use the shared `spa-dialog` component as the standard wrapper for dialog layout:

- Title/subtitle
- Footer actions (save/cancel)
- Loading/submitting state
- Content modes (form/tabbed/picker)

Code location:

- `storefront/src/app/shared/components/spa-dialog/`

## Schema-driven CRUD dialog (`ItemDialog`)

For simple Create/Edit dialogs (especially with TR/EN tabs), prefer the schema-driven dialog approach:

- A schema defines **general fields** and **i18n fields**
- The dialog produces a typed DTO payload suitable for backend composite endpoints

This reduces boilerplate and keeps validation rules consistent.

Code location:

- `storefront/src/app/shared/components/item-dialog/`

## Generic modal (`SpaGenericModalComponent`)

Use the generic modal for:

- Credential display (one-time view patterns)
- Confirmation dialogs
- Alerts with structured sections (info box, copyable fields, alert box)

## Shared form components (`custom-ui`)

For form inputs, always use existing shared components under:

- `storefront/src/app/shared/components/custom-ui/`

Examples include:

- `SpaInputComponent`, `SpaTextareaComponent`, `SpaSelectComponent`
- `SpaMediaPickerComponent` (supports single/multiple and responsive modes)
- `SpaSearchInputComponent` (debounced search)
- `SpaReorderListComponent` (drag & drop reorder)
