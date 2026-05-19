# SmartEdit — Kısa rapor

Kaynak: [docs/modules/smartedit.md](../modules/smartedit.md).

---

## Nedir?

**SmartEdit**, Craftive yönetim panelinde CMS sayfalarını **gerçek vitrin görünümü** üzerinden düzenlemek için kullanılan özelliktir. Headless Next.js vitrin, yönetim arayüzünde bir **iframe** içinde açılır; editör, sayfadaki bileşenlere tıklayıp mevcut **sayfa / bileşen düzenleme** diyaloglarını aynı bağlamda kullanır. Canlı sitede ziyaretçiler **yalnızca yayınlanmış (PUBLISHED)** içeriği görür; önizleme ayrı bir “bilet” ile etkinleşir.

---

## İş değeri (tek cümle)

Yayını riske atmadan “sayfa gerçekte nasıl görünüyor?” sorusuna cevap verir; taslak değişiklikler **canlı satırları doğrudan değiştirmeden** tutulur, yayına alınca birleştirilir.

---

## Nasıl çalışır? (özet akış)

1. **Kısa ömürlü önizleme bileti:** Kiracı yöneticisi (`TENANT_ADMIN`) `POST /api/cms/preview/tickets` ile HMAC ile imzalı bir bilet alır; süre varsayılan **15 dakika** (`app.cms.preview.ttl-seconds`).
2. **Iframe adresi:** Bilet, vitrin URL’sine `?preview=...` (ve sayfa bağlıysa `previewPageId`) olarak eklenir. Geçerli bilet varken CMS API **önizleme modunda** yanıt verir.
3. **Taslak katmanı:** Yayınlı bileşenler yerinde kalır; düzenlemeler `cms_draft_overrides` tablosunda **çalışma kopyası** olarak tutulur. Önizlemede önce taslak, yoksa yayınlı içerik kullanılır (“taslak üstte, yayın yedek”).
4. **Yayın:** İlgili dil için yayın işlemi, bu taslakları yayınlı kayıtlara uygular ve ilgili override’ları temizler; bundan sonra canlı vitrin güncellenmiş içeriği gösterir.

---

## Faz 1 kapsamı (net sınırlar)

| Şu an kapsamda | Şu an dışında |
| ---------------- | --------------- |
| Bileşen içeriği (i18n metinleri, medya, bağlantılar) taslak ile düzenleme | Slotta bileşen ekleme / kaldırma / sıralama |
| Yayınlı sayfayı SmartEdit’te açma (yayınlı içerikle yedekleme) | Tam slot taslak modeli, zamanlanmış yayın + SmartEdit birleşimi (desteklenmez sayılır) |
| Dil bazında yayın | Onay iş akışı, sürüm geçmişi, kişiselleştirme / çok varyant |

Ayrıntılı tablo ve teknik notlar: [smartedit.md](../modules/smartedit.md#purpose).

---

## Güvenlik ve izolasyon (özet)

- Bilet üretimi **kimlik doğrulamalı** ve **TENANT_ADMIN** ile sınırlıdır; yükteki kiracı ile istek kiracısı eşleşmezse önizleme **401** ile reddedilir.
- Geçersiz veya süresi dolmuş bilet: **401 — Invalid CMS preview ticket**. Biletsiz istekler canlı kurallarla **yalnız PUBLISHED** döner.
- `JwtAuthenticationFilter` → `TenantFilter` → `CmsPreviewFilter` sırası kritiktir (kiracı doğrulaması için).
- Önizleme sırrı `app.cms.preview.secret` — **en az 32 bayt**; aksi halde uygulama başlamaz. Sır şu an **platform genelidir**; sızıntıda tüm kiracılar için sahte bilet riski vardır; dokümanda Faz 2 olarak kiracı başına türetme notu geçer.
- Iframe güvenliği: vitrin tarafında `NEXT_PUBLIC_SMARTEDIT_ALLOWED_ORIGINS` ile **yalnız izinli admin kökenleri** çerçevelenebilir; canlı vitrinde SmartEdit kullanılmıyorsa bu değişken boş bırakılabilir.

---

## Devreye alma ön koşulları

- Tenant için **Site Dashboard → SEO → Canonical Base URL** tanımlı olmalı; aksi halde bilet üretilemez (iframe hangi vitrine gideceği buradan çözülür).
- Admin ve vitrin kökenlerinin **CORS** listesinde olması gerekir; `X-Cms-Preview-Ticket` izin verilen başlıklar arasındadır.
- Ortamda `CMS_PREVIEW_SECRET` / `app.cms.preview.secret` güçlü ve benzersiz olmalıdır.

---

## Sonuç

SmartEdit, headless mimaride **“görsel bağlamda düzenleme”** ihtiyacını, canlı içeriği koruyan **biletli önizleme + taslak override** modeliyle karşılar. Kapsam **faz 1** ile sınırlıdır; slot yapısı değişiklikleri ve gelişmiş yayın iş akışları ayrı ürün / mimari kararlarıdır. Tüm teknik sözleşme, akış diyagramları ve doğrulama komutları için bkz. [smartedit.md](../modules/smartedit.md).
