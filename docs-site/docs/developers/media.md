---
title: Media Library
description: Craftive Media Library ile dosya, alternatif metin, dil metadata'sı ve component ilişkilerinin geliştirici açısından nasıl yönetildiğini öğrenin.
keywords:
  - Media Library
  - medya yönetimi
  - alt text
  - Craftive assets
---

# Media Library

Media Library, Craftive'in dijital varlık yönetimi katmanıdır. Görsel, video ve dosya varlıklarının editörler tarafından yüklenmesini, düzenlenmesini ve componentlerde kullanılmasını sağlar.

## Ne iş yapar?

Media Library, sadece dosya yükleme alanı değildir. Medyanın başlık, alternatif metin, dil bilgisi, responsive kullanım ve component ilişkileri gibi editör deneyimini etkileyen bilgilerini yönetir.

## Teknik akış

1. Editör medya yükler.
2. Medya temel bilgileri ve gerekiyorsa dil bazlı açıklamalarla zenginleştirilir.
3. Medya, component veya component entry bağlamında seçilir.
4. Responsive kullanım gerekiyorsa farklı ekranlar için uygun medya varyasyonları belirlenir.
5. Public delivery katmanı, frontend'in kullanabileceği güvenli medya temsilini sunar.

## Geliştirici için mental model

Medya frontend'de sabit dosya yolu gibi düşünülmemelidir. Medya, içerik modelinin bir parçası olarak ele alınmalıdır.

Frontend tarafında beklenen davranışlar:

- Eksik medya durumunda layout kırılmamalı.
- Alternatif metin ve açıklama gibi editör girdileri erişilebilirlik için kullanılmalı.
- Responsive medya varsa ekran bağlamına uygun seçim yapılmalı.
- Medya yüklenemese bile component anlamlı bir fallback sunmalı.

## Public delivery davranışı

Public frontend yalnızca kendisine gelen medya temsilini kullanmalıdır. Dosya depolama yolu, CDN yönlendirmesi veya dönüştürme stratejisi frontend varsayımı olmamalıdır.

Medya alanları boş olabilir; bazı componentler medya olmadan da yayınlanabilir. Bu yüzden image, video ve dosya bağlantıları render edilmeden önce varlık, alternatif metin ve responsive varyasyon bilgisi birlikte değerlendirilmelidir.

## Editör deneyimine etkisi

Doğru medya modeli, editörlerin geliştiriciye bağımlı kalmadan sayfa kalitesini korumasını sağlar. Görsellerin bağlam içinde seçilmesi, hem SEO hem de erişilebilirlik için daha sağlıklı sonuçlar üretir.
