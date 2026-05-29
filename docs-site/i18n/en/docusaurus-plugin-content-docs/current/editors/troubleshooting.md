---
title: Troubleshooting
description: A quick guide for Craftive editors and project teams to diagnose publishing, preview, media, and language issues.
keywords:
  - troubleshooting
  - publish checklist
  - SmartEdit preview
  - missing media
---

# Troubleshooting

Use this guide to quickly separate the most common issues that appear while preparing content for publication.

<div className="doc-pill-row">
  <span className="doc-pill doc-pill--preview">Check in preview</span>
  <span className="doc-pill doc-pill--live">Verify live behavior</span>
  <span className="doc-pill doc-pill--public">Report clear context</span>
</div>

## Quick diagnosis

| Symptom | Likely cause | First check |
| --- | --- | --- |
| Page is not visible on the live site | The page or language variation is not published. | Publishing state and selected language |
| It exists in preview but not live | The change is still in draft. | SmartEdit change summary and publish step |
| Image area looks empty | Media is missing, unavailable, or missing a mobile variation. | Media Library details and mobile preview |
| Copy appears in another language | Language variation is missing or fallback behavior is active. | Page and component language content |
| Layout looks broken | Empty slot, missing component, or unexpected entry count. | Page Builder slot and component order |

## Pre-publish flow

<ol className="doc-flow">
  <li>Confirm that you are working in the right tenant and on the right page.</li>
  <li>Review page, component, and media changes in preview.</li>
  <li>Make sure the selected language is the language you intend to publish.</li>
  <li>Check mobile view and critical media.</li>
  <li>Complete the publish checklist before making the publish decision.</li>
</ol>

## Safe sharing

<ul className="doc-checklist">
  <li>Share only live page links with visitors.</li>
  <li>Use preview links only for internal editorial review.</li>
  <li>If a component is reused across pages, check the scope of the change.</li>
  <li>If the fix is unclear, report the page, language, component name, and expected behavior to a developer.</li>
</ul>
