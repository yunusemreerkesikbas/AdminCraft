---
title: Page Builder
description: Craftive Page Builder'ın template, slot, component ve yayın yaşam döngüsüyle geliştirici tarafında nasıl modellenmesi gerektiğini öğrenin.
keywords:
  - Page Builder
  - template slot component
  - Craftive geliştirici
  - içerik yayını
---

# Page Builder

Page Builder, Craftive'in sayfa kompozisyon katmanıdır. Geliştirici açısından görevi, editörlerin kontrollü bir yapı içinde esnek sayfalar oluşturmasını sağlamaktır.

## Ne iş yapar?

Page Builder sayfaları template ve slot yapısı üzerinden organize eder. Template sayfanın iskeletini belirler; slotlar ise componentlerin yerleştirileceği bölgelerdir.

Bu model, frontend tarafında tutarlı rendering davranışı sağlar. Her sayfa aynı şekilde veri almayabilir, ancak template ve slot yapısı sayfanın nasıl okunacağını açık hale getirir.

## Teknik akış

1. Bir sayfa oluşturulur veya var olan sayfa seçilir.
2. Sayfa bir template ile ilişkilendirilir.
3. Template, sayfanın kullanabileceği slotları belirler.
4. Editör, slotlara component ekler ve sıralama yapar.
5. Component içerikleri dil, medya ve entry bilgileriyle zenginleşir.
6. Yayınlama sonrası public delivery katmanı sayfa ağacını frontend'e sunar.

## Rendering sözleşmesi

Frontend template, slot ve component bilgisini bir layout sözleşmesi gibi okumalıdır. Slot isimleri sayfanın bölge mantığını; component tipi ise hangi renderer'ın kullanılacağını belirler.

Renderer'lar eksik slot, boş component listesi veya beklenmeyen entry sayısını hata olarak ziyaretçiye yansıtmamalıdır. Bu durumlar tasarım sisteminde sessizce atlanmalı veya anlamlı fallback ile ele alınmalıdır.

## Tasarım etkisi

Page Builder, frontend tasarımını tamamen serbest bir canvas yerine kural tabanlı bir sistem haline getirir. Bu yaklaşım:

- Marka tutarlılığını korur.
- Editör hatalarını azaltır.
- Farklı sayfalar arasında tekrar kullanımı artırır.
- Storefront rendering kodunu daha tahmin edilebilir yapar.

## Dikkat edilmesi gerekenler

- Template değişikliği sayfanın slot davranışını etkileyebilir.
- Component sırası, ziyaretçinin sayfayı okuma akışını belirler.
- Boş slotlar frontend'de kırık alan gibi görünmemelidir.
- Dil varyasyonu olmayan içerikler için fallback davranışı ürün kararına göre tasarlanmalıdır.
