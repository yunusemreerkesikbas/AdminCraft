---
title: SmartEdit
description: SmartEdit'in canlı sayfa bağlamında güvenli önizleme, düzenleme ve yayın akışı için geliştirici entegrasyon modelini açıklar.
keywords:
  - SmartEdit
  - editor preview
  - yayın akışı
  - Craftive geliştirici
---

# SmartEdit

SmartEdit, editörlerin sayfayı gerçek storefront bağlamında görmesini ve düzenlemesini sağlayan yönetimli editör deneyimidir.

## Ne iş yapar?

SmartEdit, klasik form ekranları ile canlı sayfa bağlamı arasındaki kopukluğu azaltır. Editör, hangi componenti değiştirdiğini sayfa üzerindeki konumuyla birlikte görür.

## Teknik akış

1. Editör bir sayfayı SmartEdit ile açar.
2. Storefront deneyimi editör bağlamında önizlenir.
3. Seçilebilir component ve slot bilgileri editör arayüzüne yansır.
4. Editör component içeriğini veya entry değerlerini düzenler.
5. Değişiklikler önce taslak çalışma alanında tutulur.
6. Editör inceleme yapar, gerekirse değişikliği geri alır veya yayına alır.

## Draft ve publish modeli

SmartEdit'in temel değeri, canlı içeriği korurken editörlere güvenli bir çalışma alanı sunmasıdır.

Public seviyede bilinmesi gereken prensip: SmartEdit değişiklikleri, yayın kararı verilene kadar ziyaretçi deneyimini doğrudan değiştirmez.

SmartEdit önizleme oturumu kısa ömürlü bir preview ticket ile çalışır. Ticket yalnızca admin tarafından başlatılan editör akışında kullanılır; canlı ziyaretçi oturumlarının parçası değildir.

Public delivery ve preview uçları ayrı düşünülmelidir: `/cms/**` ziyaretçi deneyimine içerik sağlar, `/cms/preview/**` ise kimlik doğrulamalı admin önizleme akışına aittir. Public doküman seviyesinde entegrasyonun bilmesi gereken sınır budur; imza formatı ve iç oturum ayrıntıları public frontend sözleşmesine dahil değildir.

| Alan | Preview | Live |
| --- | --- | --- |
| Kitle | Editör ve admin kullanıcıları | Ziyaretçiler |
| İçerik durumu | Taslak değişiklikleri gösterebilir | Yayına uygun içerik gösterir |
| Cache | Taze veri tercih edilir | Kontrollü cache kullanılabilir |
| Paylaşım | Ekip içi kontrol içindir | Ziyaretçilerle paylaşılır |
| Hata toleransı | Editöre yönlendirici hata gösterilebilir | Ziyaretçi deneyimi kırılmadan fallback uygulanır |

## Geliştirici için notlar

- Frontend, editor preview durumunda seçilebilir alanları destekleyebilir.
- Componentlerin anlamlı isimlendirilmesi editör deneyimini doğrudan iyileştirir.
- Boş, eksik veya taslak içerik durumları UI tarafında sakin ve anlaşılır ele alınmalıdır.
- SmartEdit deneyimi, public ziyaretçi performansını ve yayın güvenliğini bozmamalıdır.
- Preview istekleri public cache veya analytics olaylarıyla live trafik gibi karıştırılmamalıdır.
