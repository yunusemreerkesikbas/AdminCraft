---
title: Developer Module Flows
description: See how Craftive modules connect Page Builder, Component Library, Media Library, SmartEdit, and headless delivery.
keywords:
  - developer module flows
  - Craftive modules
  - SmartEdit
  - headless delivery
---

# Developer Module Flows

This section explains how Craftive modules work together from a developer perspective. The goal is to make module flow and the content lifecycle easy to understand.

## Main flow

A typical Craftive content flow works like this:

<ol className="doc-flow">
  <li>An editor prepares page or component content in the Admin workspace.</li>
  <li>Page Builder defines the template and slot structure of the page.</li>
  <li>Component Library provides the behavior model for content blocks added to slots.</li>
  <li>Media Library manages the visual and file assets needed by components.</li>
  <li>SmartEdit helps the editor preview and edit the page in its real context.</li>
  <li>After the publish decision, public delivery exposes current content to storefronts or other frontend experiences.</li>
</ol>

## Developer mental model

In Craftive, developers do not hardcode every page by hand. They build experiences that render a content model.

Useful questions:

- Which template should represent this page?
- Which slots does the template need?
- Which component types can be used in each slot?
- Does the component contain repeated entries?
- Should media be attached to the whole component or to individual entries?
- How will editors preview the change before publishing?

## Safe integration principles

<ul className="doc-checklist">
  <li>Admin operations and public delivery behavior should be considered separately.</li>
  <li>Public frontends should only show content that is approved for publication.</li>
  <li>Editor preview behavior should not be mixed with live visitor behavior.</li>
  <li>Media, language, and publication state should be handled from the content contract, not frontend assumptions.</li>
  <li>Empty content, missing media, and unpublished language variations should have resilient UI states.</li>
</ul>
