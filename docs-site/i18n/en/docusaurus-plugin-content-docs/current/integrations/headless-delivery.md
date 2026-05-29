---
title: Headless Delivery
description: Guide to delivering Craftive content to storefronts, mobile apps, and campaign surfaces with the headless delivery model.
keywords:
  - headless delivery
  - headless CMS
  - public delivery API
  - storefront content
---

# Headless Delivery

Craftive can deliver content to different frontend experiences through a headless delivery model. This separates content management from presentation.

## What it enables

- Multiple frontend experiences from the same content model.
- Consistent content across storefronts, mobile apps, and campaign pages.
- Centralized content management for editors.
- Custom frontend design based on the content model.

## Rendering model

The frontend reads the page structure and component list. Each component type maps to a suitable presentation. This lets the content team manage components while the frontend stays inside a consistent design system.

<ol className="doc-flow">
  <li>Tenant context is resolved from hostname or trusted header information.</li>
  <li>The frontend reads site configuration and supported language information.</li>
  <li>The page tree is loaded with slot and component relationships.</li>
  <li>Each component type is routed to its renderer.</li>
  <li>Missing content, media, or language variations are handled through fallback behavior.</li>
</ol>

## Public delivery contract

Craftive CMS delivery endpoints are designed for visitor-facing experiences. Public delivery calls do not require user authentication, but every call must still resolve to the correct tenant context.

Tenant context is usually resolved from the published hostname. In development, testing, or proxy scenarios, an integration can send either the `X-Tenant-Subdomain` or `X-Tenant-ID` header. A public frontend should not mix content from different tenants through a single runtime assumption.

Live delivery should expose only content approved for publication. Editor preview is a separate admin flow; preview information should not be used in public visitor sessions or stored in public caches.

<div className="doc-do-dont">
  <div className="doc-do">
    <h3>Do</h3>
    <ul>
      <li>Read tenant and language information from the delivery contract.</li>
      <li>Design controlled fallbacks for missing content.</li>
      <li>Use fresh data for preview requests.</li>
    </ul>
  </div>
  <div className="doc-dont">
    <h3>Don't</h3>
    <ul>
      <li>Assume a single tenant or hardcoded language list.</li>
      <li>Put preview data into public cache.</li>
      <li>Render missing media as a broken layout.</li>
    </ul>
  </div>
</div>

## Cache and resilience

Storefront integrations can cache live content, but preview or editor sessions should fetch fresh data. Missing pages, empty slots, missing media, or unpublished language variations should not break the user interface.

| State | Recommended behavior |
| --- | --- |
| Live published content | Cache can be used; plan a refresh strategy after publishing. |
| Preview or editor session | Fetch fresh data; do not use public cache. |
| Missing page | Show a controlled empty state or 404 experience. |
| Missing media | Preserve layout; use a meaningful fallback or hide the area. |
| Missing language variation | Apply fallback or empty-state behavior according to product decision. |

## Integration quality

A good headless integration:

- Does not break when content is missing.
- Handles long copy inside the design.
- Provides useful fallbacks when media cannot load.
- Respects language selection and publication state.
- Keeps editor preview separate from the live experience.
