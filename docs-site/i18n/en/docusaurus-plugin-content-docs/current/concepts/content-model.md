---
title: Content Model
description: Understand Craftive pages, templates, slots, components, entries, and media as the shared model for flexible content delivery.
keywords:
  - content model
  - Page Builder
  - component library
  - media library
---

# Content Model

Craftive's content model is built around pages, templates, slots, components, entries, and media. These concepts help editors build flexible pages while giving developers predictable rendering behavior.

## Page

A page represents a visitor-facing content screen. It usually carries title behavior, address behavior, language variations, and publication state.

## Template

A template describes which regions a page can have. For example, a landing page may include hero, content, testimonials, and footer regions.

Templates give editors a controlled structure instead of a completely empty canvas.

## Slot

A slot is a placement region inside a template. Components are added to slots and ordered inside them.

Slot logic makes it easier to reuse the same component types across different pages.

## Component

A component is a content block shown on the page. Hero sections, card lists, media galleries, FAQs, and CTAs are examples of components.

Components belong to both the design system and the content model. Editors fill them; the frontend renders them according to their type.

## Entry

An entry is repeated child content inside a component. In a card list, each card can be an entry.

This separation makes it easier to manage multiple content rows inside one component.

## Media

Media represents image and file assets. Components and entries can reference media. Responsive media behavior helps choose suitable assets for different screens.

## Shared decisions

In this model, the page, content blocks, and media assets are managed separately. That separation gives editors flexibility while giving the frontend a predictable contract.

A good implementation defines expected fields, empty-state behavior, language fallback, and media usage for each component type ahead of time. This keeps the visitor experience consistent while content teams change pages.
