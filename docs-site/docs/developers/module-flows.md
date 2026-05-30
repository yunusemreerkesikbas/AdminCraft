---
title: Geliştirici Modül Akışları
description: Craftive modüllerinin Page Builder, Component Library, Media Library, SmartEdit ve headless delivery üzerinden nasıl aktığını görün.
keywords:
  - geliştirici modül akışları
  - Craftive modülleri
  - SmartEdit
  - headless delivery
---

# Geliştirici Modül Akışları

Bu bölüm, Craftive modüllerinin geliştirici bakış açısıyla nasıl birlikte çalıştığını anlatır. Amaç, modüller arasındaki teknik akışı ve içerik yaşam döngüsünü anlaşılır hale getirmektir.

## Ana akış

Craftive içindeki tipik içerik akışı şu şekildedir:

<ol className="doc-flow">
  <li>Editör, Admin workspace üzerinden sayfa veya component içeriği hazırlar.</li>
  <li>Page Builder, sayfanın template ve slot yapısını belirler.</li>
  <li>Component Library, slotlara eklenen içerik bloklarının davranış modelini sağlar.</li>
  <li>Media Library, componentlerin ihtiyaç duyduğu görsel ve dosya varlıklarını yönetir.</li>
  <li>SmartEdit, editörün sayfayı gerçek bağlamında önizlemesine ve düzenlemesine yardım eder.</li>
  <li>Yayın kararı verildiğinde public delivery katmanı güncel içeriği storefront veya diğer frontend deneyimlerine aktarır.</li>
</ol>

## Geliştirici için mental model

Craftive'te geliştirici, sayfa HTML'ini tek tek sabitlemek yerine içerik modelini render eden bir deneyim kurar.

Pratikte düşünülmesi gereken sorular:

- Bu sayfa hangi template ile temsil edilmeli?
- Template hangi slotlara ihtiyaç duyar?
- Her slotta hangi component tipleri kullanılabilir?
- Component içinde tekrar eden entry var mı?
- Medya tek bir component'e mi, yoksa entry bazında mı bağlanmalı?
- Editörler değişikliği yayına almadan önce nasıl önizleyecek?

## Güvenli entegrasyon prensipleri

<ul className="doc-checklist">
  <li>Admin işlemleri ve public delivery davranışı ayrı düşünülmelidir.</li>
  <li>Public frontend sadece yayınlanması uygun içeriği göstermelidir.</li>
  <li>Editor preview davranışı canlı ziyaretçi davranışıyla karıştırılmamalıdır.</li>
  <li>Medya, dil ve yayın durumu gibi alanlar frontend'de varsayımla değil, gelen içerik sözleşmesine göre ele alınmalıdır.</li>
  <li>Boş içerik, eksik medya ve yayına alınmamış dil varyasyonları için dayanıklı UI tasarlanmalıdır.</li>
</ul>
