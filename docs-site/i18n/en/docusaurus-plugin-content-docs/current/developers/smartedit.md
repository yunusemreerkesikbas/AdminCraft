---
title: SmartEdit
description: Learn the developer integration model for SmartEdit preview, safe editing, and publishing in live page context.
keywords:
  - SmartEdit
  - editor preview
  - publishing workflow
  - Craftive developers
---

# SmartEdit

SmartEdit is a managed editing experience that lets editors see and adjust a page in its real storefront context.

## What it does

SmartEdit reduces the gap between traditional form screens and the live page context. Editors can see which component they are changing together with its placement on the page.

## Technical flow

1. The editor opens a page with SmartEdit.
2. The storefront experience is previewed in an editor context.
3. Selectable component and slot information is reflected in the editor UI.
4. The editor updates component content or entry values.
5. Changes are kept in a draft workspace first.
6. The editor reviews, discards, or prepares the changes for publishing.

## Draft and publish model

SmartEdit's main value is giving editors a safe workspace while protecting live content.

At public level, the key principle is: SmartEdit changes do not directly change the visitor experience until a publish decision is made.

A SmartEdit preview session uses a short-lived preview ticket. The ticket belongs only to an admin-started editor flow; it is not part of live visitor sessions.

Public delivery and preview endpoints should be treated separately: `/cms/**` serves visitor-facing content, while `/cms/preview/**` belongs to the authenticated admin preview flow. At public documentation level, that boundary is the important contract; signing format and internal session details are not part of the public frontend contract.

| Area | Preview | Live |
| --- | --- | --- |
| Audience | Editors and admin users | Visitors |
| Content state | Can show draft changes | Shows content approved for publication |
| Cache | Fresh data is preferred | Controlled cache can be used |
| Sharing | For internal review | Shared with visitors |
| Error tolerance | Can show guidance to editors | Uses fallback without breaking visitor experience |

## Developer notes

- The frontend can support selectable areas during editor preview.
- Clear component naming directly improves the editor experience.
- Empty, missing, or draft content states should be handled calmly in the UI.
- SmartEdit should not compromise public visitor performance or publication safety.
- Preview requests should not be mixed into public cache or analytics behavior as live traffic.
