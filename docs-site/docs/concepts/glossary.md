---
title: Sözlük
description: Craftive ekipleri için tenant, page, template, slot, component, entry, media, preview ve publish kavramlarının ortak açıklamaları.
keywords:
  - Craftive sözlük
  - CMS kavramları
  - içerik modeli
  - headless delivery
---

# Sözlük

Bu sözlük, ürün ekipleri, editörler ve geliştiricilerin aynı kavramları aynı anlamda kullanması için hazırlanmıştır.

| Kavram | Kısa açıklama | Nerede kullanılır? |
| --- | --- | --- |
| Tenant | İzole çalışan müşteri veya proje alanı. | Admin workspace, public delivery, site ayarları |
| Page | Ziyaretçinin göreceği içerik ekranı. | Page Builder, SmartEdit, storefront routing |
| Template | Sayfanın hangi bölgelere sahip olacağını belirleyen yapı. | Page Builder, frontend layout |
| Slot | Template içindeki component yerleşim alanı. | Sayfa kompozisyonu, storefront rendering |
| Component | Ziyaretçiye görünen içerik bloğu. | Component Library, Page Builder |
| Entry | Component içindeki tekrar eden alt içerik. | Kart listeleri, galeri öğeleri, SSS satırları |
| Media | Görsel, video veya dosya varlığı. | Media Library, component ve entry alanları |
| Preview | Editörün yayına almadan önce gördüğü çalışma görünümü. | SmartEdit, yayın kontrolü |
| Publish | İçeriğin ziyaretçi deneyimine uygun hale getirilmesi. | Page Builder, SmartEdit, delivery |
| Delivery | Yayındaki içeriğin storefront veya başka frontend yüzeyine aktarılması. | Headless storefront, mobil, kampanya sayfaları |

## Sık karıştırılanlar

| Karışan kavramlar | Ayrım |
| --- | --- |
| Template / Page | Template yapı tarifidir; Page o yapıyla yayınlanan içerik ekranıdır. |
| Slot / Component | Slot yerleşim bölgesidir; Component o bölgede render edilen içeriktir. |
| Component / Entry | Component ana bloktur; Entry bu bloğun tekrar eden satır veya kartlarıdır. |
| Preview / Live | Preview editör çalışma bağlamıdır; Live ziyaretçinin gördüğü yayın davranışıdır. |
| Media URL / Media bilgisi | URL yalnızca render girdilerinden biridir; alternatif metin, responsive varyasyon ve bağlam da önemlidir. |

## Kullanım ipucu

Yeni bir sayfa planlarken önce template ve slotları, sonra component tiplerini, en son entry ve medya ihtiyaçlarını netleştirin. Bu sıra hem editör akışını hem frontend rendering davranışını sadeleştirir.
