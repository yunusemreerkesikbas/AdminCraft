---
title: Headless Delivery
description: Craftive içeriğini storefront, mobil uygulama ve kampanya yüzeylerine headless delivery modeliyle aktarma rehberi.
keywords:
  - headless delivery
  - headless CMS Türkiye
  - public delivery API
  - storefront içerik
---

# Headless Delivery

Craftive, içeriği farklı frontend deneyimlerine headless delivery modeliyle aktarabilir. Bu model, içerik yönetimini sunum katmanından ayırır.

## Ne sağlar?

- Aynı içerik modeliyle birden fazla frontend deneyimi.
- Storefront, mobil uygulama veya kampanya sayfası gibi farklı yüzeylerde tutarlı içerik.
- Editörlerin içeriği merkezi olarak yönetebilmesi.
- Geliştiricilerin frontend tasarımını içerik modeline göre özelleştirebilmesi.

## Rendering mantığı

Frontend, sayfa yapısını ve component listesini okur. Her component tipi için uygun bir sunum belirlenir. Bu sayede içerik ekibi componentleri yönetirken frontend de tutarlı bir tasarım sistemi içinde kalır.

<ol className="doc-flow">
  <li>Tenant bağlamı hostname veya güvenilir header bilgisiyle çözülür.</li>
  <li>Frontend site konfigürasyonunu ve desteklenen dil bilgisini okur.</li>
  <li>Sayfa ağacı slot ve component ilişkileriyle alınır.</li>
  <li>Her component tipi kendi renderer'ına yönlendirilir.</li>
  <li>Eksik içerik, medya veya dil varyasyonu fallback davranışıyla ele alınır.</li>
</ol>

## Public delivery sözleşmesi

Craftive CMS delivery uçları ziyaretçi deneyimleri için tasarlanmıştır. Bu nedenle public delivery çağrıları kullanıcı girişi gerektirmez; ancak her çağrı yine de doğru tenant bağlamında çözülmelidir.

Tenant bağlamı genellikle yayındaki hostname üzerinden anlaşılır. Geliştirme, test veya proxy senaryolarında entegrasyon `X-Tenant-Subdomain` ya da `X-Tenant-ID` header'larından birini gönderebilir. Public frontend, farklı tenant içeriklerini tek runtime varsayımıyla karıştırmamalıdır.

Live delivery yalnızca yayına uygun içeriği göstermelidir. Editor preview ise ayrı bir admin akışıdır; preview bilgisi public ziyaretçi oturumlarında kullanılmamalı ve cache'e alınmamalıdır.

<div className="doc-do-dont">
  <div className="doc-do">
    <h3>Do</h3>
    <ul>
      <li>Tenant ve dil bilgisini delivery sözleşmesinden oku.</li>
      <li>Eksik içerik için kontrollü fallback tasarla.</li>
      <li>Preview isteklerinde taze veri kullan.</li>
    </ul>
  </div>
  <div className="doc-dont">
    <h3>Don't</h3>
    <ul>
      <li>Tek tenant veya sabit dil listesi varsayma.</li>
      <li>Preview verisini public cache'e koyma.</li>
      <li>Eksik medyayı kırık layout olarak gösterme.</li>
    </ul>
  </div>
</div>

## Cache ve hata toleransı

Storefront entegrasyonları canlı içerikte cache kullanabilir; fakat preview veya editör oturumlarında taze veri alınmalıdır. Eksik sayfa, boş slot, eksik medya veya yayınlanmamış dil varyasyonu kullanıcı arayüzünü kırmamalıdır.

| Durum | Önerilen davranış |
| --- | --- |
| Live yayın içeriği | Cache kullanılabilir; yayın sonrası yenileme stratejisi planlanmalıdır. |
| Preview veya editör oturumu | Taze veri alınmalı; public cache kullanılmamalıdır. |
| Eksik sayfa | Ziyaretçiye kontrollü boş durum veya 404 deneyimi gösterilmelidir. |
| Eksik medya | Layout korunmalı; anlamlı fallback veya alanı gizleme tercih edilmelidir. |
| Eksik dil varyasyonu | Ürün kararına göre fallback veya boş durum uygulanmalıdır. |

## Entegrasyon kalitesi

İyi bir headless entegrasyon:

- Eksik içerikte bozulmaz.
- Uzun metinleri tasarım içinde taşır.
- Medya yüklenemese bile anlamlı fallback sunar.
- Dil seçimini ve yayın durumunu dikkate alır.
- Editor preview ve live deneyimi karıştırmaz.
