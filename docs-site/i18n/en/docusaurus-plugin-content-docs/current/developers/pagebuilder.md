---
title: Page Builder
description: Learn how Craftive Page Builder models templates, slots, components, and publishing lifecycle behavior for developers.
keywords:
  - Page Builder
  - template slot component
  - Craftive developers
  - content publishing
---

# Page Builder

Page Builder is Craftive's page composition layer. For developers, its role is to let editors create flexible pages inside a controlled structure.

## What it does

Page Builder organizes pages through templates and slots. The template defines the page skeleton; slots are the regions where components are placed.

This model gives the frontend predictable rendering behavior. Not every page has the same data, but the template and slot structure makes the page readable.

## Technical flow

1. A page is created or selected.
2. The page is associated with a template.
3. The template defines the available slots.
4. The editor adds components to slots and orders them.
5. Component content is enriched with language, media, and entry information.
6. After publishing, public delivery exposes the page tree to the frontend.

## Rendering contract

The frontend should read template, slot, and component data as a layout contract. Slot names describe page regions; component type determines which renderer should be used.

Renderers should not expose missing slots, empty component lists, or unexpected entry counts as visitor-facing errors. These states should be skipped quietly or handled with meaningful fallbacks in the design system.

## Design impact

Page Builder turns frontend design into a rule-based system instead of a fully free canvas. This approach:

- Protects brand consistency.
- Reduces editor mistakes.
- Increases reuse across pages.
- Makes storefront rendering more predictable.

## Things to watch

- Template changes may affect slot behavior.
- Component order defines how visitors read the page.
- Empty slots should not appear as broken areas on the frontend.
- Missing language variations should follow the product fallback decision.
