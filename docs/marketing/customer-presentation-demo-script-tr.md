# Craftive — Canlı demo senaryosu (8–10 dk, TR)

İş tarafı izleyicisi için; teknik detayı minimumda tutar. Süreler yaklaşıktır.

**Varsayımlar (demo ortamı):**

- **Tenant:** “Demo Holding” (örnek isim) — subdomain veya test host adı önceden açılmış.
- **Modüller:** `core` (içeride medya, bileşen kütüphanesi, sayfa oluşturucu ile), isteğe bağlı `product`, isteğe bağlı `mail_marketing`. Senaryo **tam paket** varsayar; sadece `core` ise 4. ve 6. adımları atlayın veya kısaltın.
- **Roller:** Sunum yapan kişi `TENANT_ADMIN` veya eşdeğeri tam yetki; SmartEdit için admin gerekir.
- **Ön koşul:** En az bir **yayınlanmış** ana sayfa + header/footer bileşenleri; ürün senaryosu için birkaç örnek ürün ve kategori.

---

## 0:00–0:45 — Giriş ve çerçeve

**Ne gösterilir:** Giriş ekranı veya boş dashboard; kısa bağlam.

**Söylenecek:**

- “Bugün Craftive’i üç açıdan göstereceğim: içeriği nasıl yönettiğiniz, vitrine nasıl yansıdığı ve siteyi nasıl kontrol ettiğiniz.”
- “Bu ortam sizin sektörünüze özel hazırlanmış demo verileri içeriyor; canlıda aynı yapı sizin markanızla dolar.”

**Risk notu:** Oturum açma / MFA politikası açıksa önceden giriş yapılmış sekme kullanın.

---

## 0:45–2:00 — Site özeti (tek panel)

**Ne gösterilir:** `/:lang/site` Site Dashboard — Genel özet sekmesi.

**Söylenecek:**

- “Site adı, durum ve son aktiviteler tek yerde; ekip ‘site sağlıklı mı’ sorusuna hızlı bakar.”
- “SEO ve performans kartları entegrasyon yapıldıysa burada özetlenir; yoksa önce kurulum yapılır — vaat olarak ‘otomatik dolu’ demiyoruz.”

**Tıklanacak yerler:** Varsa analitik / içgörü sekmelerine **dokunmadan** geçin veya tek cümleyle işaret edin (süre için).

---

## 2:00–3:30 — Sayfa ve şablon mantığı

**Ne gösterilir:** `/:lang/pages` veya sayfa listesi → tek bir içerik sayfası; gerekirse `/:lang/page-templates` ile şablon listesine üst düzey bakış.

**Söylenecek:**

- “Şablon, sayfanın iskeletini; slotlar, yerleşim bölgelerini; bileşenler, gerçek içerik bloklarını temsil eder — kurumsal tutarlılık buradan gelir.”
- “Yeni sayfa açmak çoğu zaman yeni bir icat değil, doğru şablon ve bileşen seçimi demektir.”

**Kaçının:** Alan adları ve API’leri okumak.

---

## 3:30–5:30 — SmartEdit (önizleme ile düzenleme)

**Ne gösterilir:** `/:lang/smartedit` — yayınlanmış bir sayfayı açın; küçük bir metin veya görsel alanında **kaydedilebilir taslak** değişikliği yapın (canlıyı bozmadığınızı vurgulayın).

**Söylenecek:**

- “Editör, ziyaretçinin göreceği düzen üzerinde çalışır; değişiklikler önce önizleme / taslak katmanında kalır.”
- “Yayına alma ayrı bir adımdır; yanlışlıkla canlıyı düzeltme stresi azalır.”

**Yedek plan:** SmartEdit kapalıysa veya bilet hatası varsa, klasik bileşen düzenleme diyaloğuna geçin ve “aynı model, farklı kabuk” deyin.

---

## 5:30–6:45 — Medya ve bileşen kütüphanesi (hızlı)

**Ne gösterilir:** `/:lang/media` — bir klasör veya etiket; `/:lang/components` — bir bileşen tipi listesi.

**Söylenecek:**

- “Medya merkezi, tüm sayfaların ortak görsel disiplinine hizmet eder.”
- “Bileşen kütüphanesi, tekrarlayan UI bloklarını standartlaştırır — ajans veya çok dilli ekip için kritik.”

**Süre kısaysa:** Sadece medyada 1 dosya önizlemesi yeter.

---

## 6:45–8:00 — Ürün kataloğu (modül açıksa)

**Ne gösterilir:** `/:lang/products` — kategori veya ürün listesi; tek ürün detayına girmeden liste kalitesini gösterin.

**Söylenecek:**

- “Katalog Craftive’te yönetilir; mağaza vitrininiz headless API ile beslenir — ileride vitrin teknolojisini değiştirmek daha kolay olur.”

**Modül yoksa:** “Bu tenant’ta katalog modülü kapalı; ihtiyaç halinde aynı panelde açılır” deyip atlayın.

---

## 8:00–9:15 — İletişim / pazarlama yüzeyi (opsiyonel kısa)

**Ne gösterilir:** `/:lang/contact-requests` (formdan gelen talepler) veya `/:lang/mail-marketing` abone listesine **maskeli** örnek.

**Söylenecek:**

- “Ziyaretçi talepleri tenant içinde toplanır; e-posta modülü açıksa kampanya ve şablonlar aynı operasyonel çatı altında kalır.”

**Modül yoksa:** Sadece iletişim talepleri veya bu adımı tamamen atlayın.

---

## 9:15–10:00 — Vitrin (headless kanıt)

**Ne gösterilir:** `storefront-nextjs` tabanlı demo vitrin veya müşteri vitrini — ana sayfayı yenileyin; dil değiştirici varsa bir kez değiştirin.

**Söylenecek:**

- “Gördüğünüz sayfa, az önce panelde gördüğünüz içerik modelinin son kullanıcı çıktısıdır.”
- “Teknik ekip Next.js, Angular veya başka bir istemci seçebilir; sözleşme API tarafında kalır.”

**Kapanış cümlesi:** “Sonraki adım: pilot kapsam, entegrasyon listesi ve destek seviyesini birlikte netleştirmek.”

---

## Soru çıkması muhtemel anlar — 10 saniyelik cevaplar

| Soru | Kısa cevap |
|------|------------|
| “SaaS mı?” | “Self-serve sabit paket değil; platform + proje bazlı kurulum ve işletme.” |
| “Veri ayrımı?” | “Kiracı başına ayrı veritabanı; klasik çok kiracılı tek DB modelinden farklı.” |
| “Fiyat?” | “Modül seti, içerik karmaşıklığı, vitrin uyarlama ve entegrasyona göre teklif.” |
| “WordPress’ten fark?” | “API öncelikli, modüler platform; içerik modeli ve operasyon tek elde toplanır.” |

---

## Demo öncesi kontrol listesi

- [ ] Tenant girişi ve dil yolu (`/:lang/...`) çalışıyor.
- [ ] En az bir yayınlanmış sayfa + SmartEdit için uygun sayfa.
- [ ] Ürün modülü gösterilecekse örnek veri mevcut.
- [ ] İletişim formu veya talep listesi boş değilse gizlilik (maskeleme) hazır.
- [ ] Vitrin URL’si ve admin URL’si ayrı sekmelerde açık.
