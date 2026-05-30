---
title: Media Library
description: Learn how Craftive Media Library manages files, alternative text, language metadata, and component relationships.
keywords:
  - Media Library
  - media management
  - alt text
  - Craftive assets
---

# Media Library

Media Library is Craftive's digital asset management layer. It lets editors upload, manage, and use image, video, and file assets in components.

## What it does

Media Library is more than file upload. It manages information that affects the editor experience, such as title, alternative text, language metadata, responsive usage, and component relationships.

## Technical flow

1. The editor uploads media.
2. The asset is enriched with basic information and language-specific descriptions when needed.
3. Media is selected in the context of a component or component entry.
4. If responsive behavior is needed, suitable assets are chosen for different screens.
5. Public delivery exposes a safe media representation that the frontend can use.

## Developer mental model

Media should not be treated as a fixed file path in the frontend. It is part of the content model.

Expected frontend behavior:

- Layout should not break when media is missing.
- Alternative text and descriptions should support accessibility.
- Responsive media should use the suitable asset for the screen context.
- Components should provide meaningful fallbacks when media cannot load.

## Public delivery behavior

The public frontend should use only the media representation it receives. File storage paths, CDN routing, and transformation strategy should not become frontend assumptions.

Media fields can be empty, and some components can be published without media. Before rendering image, video, or file links, evaluate asset presence, alternative text, and responsive variation data together.

## Impact on editor experience

A good media model lets editors preserve page quality without relying on developers for every change. Choosing images in context improves both SEO and accessibility.
