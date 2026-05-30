---
title: Platform Overview
description: Learn Craftive's modular platform model, admin workspace, public delivery layer, and tenant-isolated content architecture.
keywords:
  - Craftive platform
  - modular digital platform
  - tenant isolation
  - public delivery
---

# Platform Overview

Craftive is a modular platform for multi-tenant digital experiences. Content management, media management, page composition, and headless delivery work together around the same model.

<div className="doc-summary">
  <div className="doc-summary-item">
    <span className="doc-summary-label">Audience</span>
    <span className="doc-summary-value">Project teams and developers</span>
  </div>
  <div className="doc-summary-item">
    <span className="doc-summary-label">Focus</span>
    <span className="doc-summary-value">Content model and publishing behavior</span>
  </div>
  <div className="doc-summary-item">
    <span className="doc-summary-label">Scope</span>
    <span className="doc-summary-value">Technical overview</span>
  </div>
</div>

## Core idea

Craftive has two main surfaces:

- **Admin workspace:** Site owners and editors manage content, media, pages, and publishing.
- **Public delivery:** The live site or application consumes approved content in a controlled way.

This separation keeps the editor experience away from the live visitor experience. Editors can work on drafts while visitors continue to see the latest approved version.

## Module model

Platform modules behave like separate product capabilities, but they cooperate around a shared content model:

- Page Builder defines page structure and region logic.
- Component Library provides reusable content blocks.
- Media Library manages image and file assets.
- SmartEdit provides an in-context editing experience.
- Delivery exposes approved content to frontend experiences.

## Safe publishing model

Craftive separates draft behavior from published content behavior. This lets editors review, preview, and publish changes when they are ready.

At public documentation level, the important principle is simple: visitor-facing experiences receive content that is approved for publication; editor work does not change the live experience until a publish decision is made.

## Multilingual content

The content model supports multilingual work. Page and component copy can be managed by language, and editors can plan publishing per language.

This model keeps one site structure while allowing market-specific copy and media variations.
