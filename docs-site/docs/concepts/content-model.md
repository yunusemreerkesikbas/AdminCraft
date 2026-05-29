---
title: İçerik Modeli
description: Craftive page, template, slot, component, entry ve media kavramlarıyla esnek sayfa ve içerik yönetimi modelini açıklar.
keywords:
  - içerik modeli
  - Page Builder
  - component library
  - media library
---

# İçerik Modeli

Craftive içerik modeli; page, template, slot, component, entry ve media kavramları etrafında kurulur. Bu kavramlar editörlerin esnek sayfalar oluşturmasını, geliştiricilerin ise tutarlı rendering davranışı tasarlamasını sağlar.

## Page

Page, ziyaretçinin göreceği bir içerik ekranını temsil eder. Bir sayfa genellikle başlık, adres davranışı, dil varyasyonları ve yayın durumu gibi bilgiler taşır.

## Template

Template, bir sayfanın hangi bölgelere sahip olacağını tarif eder. Örneğin bir landing sayfası; hero, içerik, referanslar ve footer gibi bölgelerden oluşabilir.

Template, editörlere boş bir canvas vermek yerine kontrollü ve tutarlı bir yapı sunar.

## Slot

Slot, template içindeki yerleştirme bölgesidir. Componentler slotlara eklenir ve sıralanır.

Slot mantığı, aynı component türlerinin farklı sayfalarda tekrar kullanılmasını kolaylaştırır.

## Component

Component, sayfada görünen içerik bloğudur. Hero, kart listesi, medya galerisi, SSS veya CTA gibi yapılar component olarak düşünülebilir.

Componentler hem tasarım sistemine hem de içerik modeline bağlıdır. Bu nedenle editörler componentleri doldurur, frontend ise component tipine göre uygun sunumu yapar.

## Entry

Entry, component içindeki tekrar eden alt içeriktir. Örneğin kart listesinde her kart bir entry olabilir.

Bu ayrım, tek bir component içinde birden çok içerik satırı yönetmeyi kolaylaştırır.

## Media

Media, görsel ve dosya varlıklarını temsil eder. Componentler ve entryler medya ile ilişkilendirilebilir. Responsive medya modeli, farklı ekranlar için uygun varlık seçimini destekler.

## Ortak kararlar

Bu modelde sayfanın kendisi, içerik blokları ve medya varlıkları ayrı yönetilir. Bu ayrım editörlere esneklik verirken frontend'e de tahmin edilebilir bir sözleşme sağlar.

İyi bir uygulamada her component tipi için beklenen alanlar, boş durum davranışı, dil fallback'i ve medya kullanımı önceden tasarlanır. Böylece içerik ekibi sayfaları değiştirirken ziyaretçi deneyimi tutarlı kalır.
