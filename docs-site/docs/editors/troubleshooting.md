---
title: Sorun Giderme
description: Craftive editörleri ve proje ekipleri için yayın, preview, medya ve dil sorunlarını hızlıca ayırt etme rehberi.
keywords:
  - sorun giderme
  - yayın kontrolü
  - SmartEdit preview
  - medya görünmüyor
---

# Sorun Giderme

Bu rehber, yayına hazırlanan içerikte en sık görülen durumları hızlıca ayırt etmek için kullanılır.

<div className="doc-pill-row">
  <span className="doc-pill doc-pill--preview">Önizlemede kontrol et</span>
  <span className="doc-pill doc-pill--live">Live davranışı doğrula</span>
  <span className="doc-pill doc-pill--public">Ekibe net bilgi ver</span>
</div>

## Hızlı teşhis

| Belirti | Muhtemel neden | İlk kontrol |
| --- | --- | --- |
| Sayfa live sitede görünmüyor | Sayfa veya dil varyasyonu yayında değildir. | Yayın durumu ve doğru dil seçimi |
| Preview'de var, live sitede yok | Değişiklik taslakta kalmıştır. | SmartEdit değişiklik özeti ve publish adımı |
| Görsel boş görünüyor | Medya seçilmemiş, erişilemiyor veya mobil varyasyon eksik. | Media Library bilgileri ve mobil preview |
| Metin farklı dilde geliyor | Dil varyasyonu eksik veya fallback davranışı devrededir. | Sayfa ve component dil içerikleri |
| Layout kırık gibi duruyor | Boş slot, eksik component veya beklenmeyen entry sayısı vardır. | Page Builder slot ve component sırası |

## Yayın öncesi akış

<ol className="doc-flow">
  <li>Doğru tenant ve doğru sayfa üzerinde çalıştığınızı kontrol edin.</li>
  <li>Sayfa, component ve medya değişikliklerini preview ile inceleyin.</li>
  <li>Dil seçiminin yayına alınacak dil olduğundan emin olun.</li>
  <li>Mobil görünümü ve kritik görselleri kontrol edin.</li>
  <li>Yayın kontrol listesini tamamladıktan sonra publish kararını verin.</li>
</ol>

## Güvenli paylaşım

<ul className="doc-checklist">
  <li>Canlı ziyaretçilerle yalnızca live sayfa linklerini paylaşın.</li>
  <li>Preview linklerini ekip içi editör kontrolü dışında kullanmayın.</li>
  <li>Bir component birden fazla sayfada kullanılıyorsa değişiklik kapsamını kontrol edin.</li>
  <li>Çözüm net değilse geliştiriciye sayfa, dil, component adı ve beklenen davranışla birlikte bildirin.</li>
</ul>
