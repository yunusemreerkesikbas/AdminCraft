---
title: Platform Genel Bakışı
description: Craftive'ın modüler platform yapısını, admin workspace ve public delivery ayrımını, tenant-izole içerik modelini öğrenin.
keywords:
  - Craftive platform
  - modüler dijital platform
  - tenant izolasyonu
  - public delivery
---

# Platform Genel Bakışı

Craftive, çok kiracılı dijital deneyimler için tasarlanmış modüler bir platformdur. İçerik yönetimi, medya yönetimi, sayfa kompozisyonu ve headless yayınlama aynı model üzerinde birlikte çalışır.

<div className="doc-summary">
  <div className="doc-summary-item">
    <span className="doc-summary-label">Kitle</span>
    <span className="doc-summary-value">Proje ekipleri ve geliştiriciler</span>
  </div>
  <div className="doc-summary-item">
    <span className="doc-summary-label">Odak</span>
    <span className="doc-summary-value">İçerik modeli ve yayın davranışı</span>
  </div>
  <div className="doc-summary-item">
    <span className="doc-summary-label">Kapsam</span>
    <span className="doc-summary-value">Teknik genel bakış</span>
  </div>
</div>

## Temel fikir

Craftive iki ana yüzey sunar:

- **Admin workspace:** Site sahipleri ve editörler içerik, medya, sayfa ve yayın süreçlerini yönetir.
- **Public delivery:** Yayındaki site veya uygulama, hazırlanmış içeriği güvenli ve kontrollü şekilde tüketir.

Bu ayrım, editör deneyimini canlı ziyaretçi deneyiminden ayırır. Editörler taslak üzerinde çalışırken ziyaretçiler yayındaki son güvenli sürümü görmeye devam eder.

## Modül modeli

Platform modülleri birbirinden bağımsız ürün parçaları gibi davranır, ancak ortak içerik modeli etrafında birlikte çalışır:

- Page Builder sayfa yapısını ve bölge mantığını belirler.
- Component Library tekrar kullanılabilir içerik bloklarını sağlar.
- Media Library görsel ve dosya varlıklarını yönetir.
- SmartEdit yayındaki sayfa bağlamında editör deneyimi sunar.
- Delivery katmanı hazır içeriği frontend deneyimlerine aktarır.

## Güvenli yayınlama modeli

Craftive, taslak ve yayınlanmış içerik davranışını ayırır. Bu sayede editörler değişiklikleri inceleyebilir, önizleyebilir ve hazır olduğunda yayına alabilir.

Public dokümantasyon seviyesinde bilinmesi gereken prensip şudur: ziyaretçi tarafına yalnızca yayınlanması uygun içerik gider; editör çalışmaları ayrı bir yayın kararına kadar canlı deneyimi değiştirmez.

## Çok dilli içerik

İçerik yapısı çok dilli çalışmayı destekler. Sayfa ve component metinleri dil bazlı düzenlenebilir; editörler yayın kontrolünü dil bazında planlayabilir.

Bu model, tek bir site yapısını korurken farklı pazarlara özel metin ve medya varyasyonları hazırlamaya yardım eder.
