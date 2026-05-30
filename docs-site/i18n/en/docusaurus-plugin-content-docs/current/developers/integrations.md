---
title: Integration Principles
description: Core principles for Craftive API contracts, fault tolerance, data flow, and headless frontend compatibility.
keywords:
  - Craftive integration
  - API contract
  - headless frontend
  - delivery API
---

# Integration Principles

Craftive can provide content to different frontend experiences through a headless delivery model. This page summarizes the core principles for designing integrations.

## Core principles

- The frontend should render content according to the contract provided by the source system.
- Admin behavior and public delivery behavior should stay separate.
- Preview and live experiences should not be handled with the same assumptions.
- Language, media, publication state, and empty content states should be part of the design decision.

## Storefront behavior

The storefront reads the page and component tree to create the user experience. Component type determines which presentation the frontend uses.

This model allows the same content base to support different frontend designs.

## Data boundaries

Integration code should treat tenant, language, and publication state as part of the content contract. The frontend should not assume a fixed tenant, a hardcoded language list, or the same fields on every component.

Public delivery calls are for visitor-facing experiences. Admin operations, editor preview, and content mutations should remain separate flows. This lets the storefront optimize around published-content behavior only.

## Resilient UI

The goal is to build interfaces that look good with ideal data and remain stable with incomplete data.

Check these states:

- Empty component list.
- Missing media.
- Unpublished language variation.
- Long title or description.
- Fewer or more entries than expected.
- Slow or temporarily unavailable media.
