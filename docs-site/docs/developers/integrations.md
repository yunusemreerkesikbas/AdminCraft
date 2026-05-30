---
title: Entegrasyon İlkeleri
description: Craftive entegrasyonlarında API sözleşmeleri, hata toleransı, veri akışı ve headless frontend uyumluluğu için temel ilkeler.
keywords:
  - Craftive entegrasyon
  - API sözleşmesi
  - headless frontend
  - delivery API
---

# Entegrasyon İlkeleri

Craftive, headless delivery modeliyle farklı frontend deneyimlerine içerik sağlayabilir. Bu sayfa, entegrasyon tasarlarken kullanılacak temel prensipleri özetler.

## Temel prensipler

- Frontend, içeriği kaynak sistemden gelen sözleşmeye göre render etmelidir.
- Admin ve public delivery davranışları birbirinden ayrılmalıdır.
- Preview ve live deneyim aynı varsayımlarla ele alınmamalıdır.
- Dil, medya, yayın durumu ve boş içerik halleri tasarım kararının parçası olmalıdır.

## Storefront davranışı

Storefront, sayfa ve component ağacını okuyarak kullanıcıya uygun deneyimi oluşturur. Component tipi, frontend tarafında hangi sunumun kullanılacağını belirler.

Bu model, aynı içerik tabanının farklı frontend tasarımlarıyla kullanılmasını mümkün kılar.

## Veri sınırları

Entegrasyon kodu tenant, dil ve yayın durumu bilgisini içerik sözleşmesinin parçası olarak ele almalıdır. Frontend tarafında sabit tenant, sabit dil listesi veya her componentte aynı alanların var olduğu varsayımı yapılmamalıdır.

Public delivery çağrıları ziyaretçi deneyimi içindir. Admin işlemleri, editör preview ve içerik mutasyonları ayrı akışlar olarak kalmalıdır. Böylece storefront yalnızca yayınlanmış içerik davranışına göre optimize edilebilir.

## Dayanıklı UI

Entegrasyonlarda hedef, ideal veri geldiğinde güzel görünen ama eksik veri geldiğinde de bozulmayan arayüzler tasarlamaktır.

Kontrol edilmesi gereken durumlar:

- Boş component listesi.
- Eksik medya.
- Yayınlanmamış dil varyasyonu.
- Uzun başlık veya açıklama.
- Beklenenden az veya çok entry.
- Yavaş veya geçici olarak erişilemeyen medya.
