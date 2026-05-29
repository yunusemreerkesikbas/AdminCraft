---
title: Page Builder Kullanımı
description: Editörler için Craftive Page Builder ile template, slot, component ve entry kullanarak sayfa oluşturma rehberi.
keywords:
  - Page Builder kullanımı
  - editör rehberi
  - sayfa oluşturma
  - Craftive CMS
---

# Page Builder Kullanımı

Page Builder, sayfaları hazır yapılar üzerinden oluşturmanızı sağlar. Amaç, tasarımı bozmadan yeni sayfa ve içerik blokları yönetebilmenizdir.

## Sayfa oluşturma mantığı

Bir sayfa genellikle şu parçalardan oluşur:

- **Template:** Sayfanın genel iskeleti.
- **Slot:** Sayfanın içindeki yerleştirme bölgeleri.
- **Component:** Ziyaretçinin göreceği içerik bloğu.
- **Entry:** Component içindeki tekrar eden kart veya satırlar.

## Tipik kullanım akışı

<ol className="doc-flow">
  <li>Yeni sayfa oluşturun veya mevcut sayfayı açın.</li>
  <li>Sayfa amacına uygun template seçin.</li>
  <li>Template'in sunduğu slotları kontrol edin.</li>
  <li>İhtiyaç duyulan componentleri ilgili slotlara ekleyin.</li>
  <li>Component metinlerini, medya seçimlerini ve sıralamasını düzenleyin.</li>
  <li>Önizleme yapın.</li>
  <li>Yayına almadan önce kontrol listesini tamamlayın.</li>
</ol>

## Component mi entry mi?

| İhtiyaç | Ne kullanmalı? | Neden? |
| --- | --- | --- |
| Sayfada ayrı bir bölüm oluşturmak | Component | Kendi başlığı, medyası ve görünüm davranışı olur. |
| Aynı bölüm içinde tekrar eden kartlar | Entry | Sıralama ve tekrar eden içerik yönetimi kolaylaşır. |
| Tek bir görsel veya CTA alanı | Component | Alanın amacı ve tasarım davranışı daha nettir. |
| Liste, galeri veya SSS satırları | Entry | İçerik satırları aynı yapı içinde çoğaltılır. |

## İyi pratikler

<ul className="doc-checklist">
  <li>Her sayfada tek bir ana amaç belirleyin.</li>
  <li>Hero alanında kısa ve net metin kullanın.</li>
  <li>Uzun listelerde entry sayısını kontrol edin.</li>
  <li>Her medya için anlamlı alternatif metin ekleyin.</li>
  <li>Boş veya eksik componentleri yayına almadan önce temizleyin.</li>
</ul>
