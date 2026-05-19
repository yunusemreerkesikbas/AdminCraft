# Craftive — Müşteri sunumu (TR)

İş tarafı odaklı, dengeli platform anlatımı. Her slayt için **başlık**, **ekrandaki ana mesaj** (kısa), **konuşmacı notları** (3–5 madde).

Kaynaklar: [docs/README.md](../README.md), [strategy.md](./strategy.md), canlı site [craftive.io/en/](https://craftive.io/en/).

---

## Slayt 1 — Kapak ve gündem

**Ekranda:** Craftive — Modüler dijital proje teslimi | Bugünün gündemi: vizyon → yetenekler → güven → süreç → sorular

**Konuşmacı notları:**

- Bugün Craftive’in ne olduğunu, hangi iş problemlerini çözdüğünü ve nasıl teslim edildiğini netleştireceğiz.
- Teknik detaylara boğulmadan; ihtiyaç halinde mimari ve entegrasyonları ayrı oturumda derinleştiririz.
- Amaç: “Bu bizim için doğru temel mi?” sorusuna somut cevap vermek.

---

## Slayt 2 — Başlangıç noktası: tekrarlayan maliyet

**Ekranda:** Her projede sıfırdan CMS + medya + yayın + (gerekirse) katalog = uzun süre, yüksek risk, sürdürülebilirlik borcu

**Konuşmacı notları:**

- Kurumsal site, içerik merkezi, katalog temeli veya kampanya yüzeyleri çoğu kurumda benzer ihtiyaçlar; fakat her seferinde farklı bir “mini ürün” üretiliyor.
- Sonuç: teslim süreleri uzar, kalite dalgalanır, ekipler dağıldığında bilgi kaybolur.
- Craftive bu tekrarı **paylaşılan bir temel + kontrollü özelleştirme** ile kısaltmayı hedefler (strategy: “sıfırdan inşa değil”).

---

## Slayt 3 — Craftive nedir?

**Ekranda:** Tek altyapı; ihtiyaca göre modüller ve kurulum. **Self-serve sabit paket değil** — proje bazlı çözüm teslimi.

**Konuşmacı notları:**

- Craftive, “herkes için tek SaaS paketi” değil; **platform + doğru modül seti + kurulum + gerektiğinde uyarlama ve işletme** satar (bkz. [strategy.md](./strategy.md) bölüm 3).
- Müşteri ihtiyacına göre şekillenir: kurumsal web, içerik operasyonu, headless katalog, e-posta operasyonu veya ajans modeli gibi senaryolar aynı çekirdek üzerinde anlatılabilir.
- Bu mesaj, beklenti yönetimi için kritik: fiyat ve kapsam **projeye göre** belirlenir.

---

## Slayt 4 — Üç katmanlı içerik mimarisi (iş dili)

**Ekranda:** Şablon (iskelet) → Bölge / slot (yerleşim) → Bileşen (içerik birimleri). Bir kez tanımla, sayfalarda tutarlı kullan.

**Konuşmacı notları:**

- Canlı pazarlama sitesinde de anlatıldığı gibi: **PageTemplate**, **PageSlot**, **CmsComponent** üçlüsü içerik disiplinini sağlar ([craftive.io/en/](https://craftive.io/en/) “Content Architecture”).
- İş faydası: editoryal tutarlılık, yeniden kullanım, “her sayfada farklı düzen” kaosunu azaltma.
- Teknik ekip için: headless API üzerinden aynı model mağaza veya kurumsal sitede tüketilir.

---

## Slayt 5 — Modüler yetenekler (özet harita)

**Ekranda:** Çekirdek içerik + sayfa oluşturma + medya + bileşen kütüphanesi | İsteğe bağlı: ürün kataloğu | İsteğe bağlı: e-posta pazarlama

**Konuşmacı notları:**

- Provizyon sırasında seçilebilen modül çerçevesi dokümantasyonda özetlenir: çekirdek genişletmesi (sayfa oluşturucu, medya, bileşen kütüphanesi) `core` ile birlikte çalışır; `product` ve `mail_marketing` ihtiyaca göre eklenir ([docs/README.md](../README.md), `ModuleCode`).
- “Hepsini açmak” zorunlu değil; doğru minimum set ile başlayıp büyümek mümkün.
- Headless vitrin için referans Next.js storefront repo içinde dokümante edilir; müşteri yüzeyi markaya göre temalanır.

---

## Slayt 6 — Kiracı güvenliği ve operasyonel ayrım

**Ekranda:** Veri düzeyinde ayrık veritabanı (kiracı başına). Kontrol düzeyi (platform) ile müşteri verisi ayrımı.

**Konuşmacı notları:**

- Mimari: **kiracı başına ayrı veritabanı** — klasik “tek tabloda tenant_id” modelinden farklı, sızıntı riskini iş dilinde “fiziksel ayrım” olarak konumlandırın ([README.md](../../README.md), [architecture.md](../global/architecture.md)).
- Platform tarafı: tenant oluşturma, modül işleri, demo talepleri gibi operasyonlar ayrı; müşteri içeriği kendi izolasyonunda.
- Uyumluluk ve yedekleme mesajlarını müşteri profiline göre ölçülü kullanın; iddiaları hukuk ile hizalayın.

---

## Slayt 7 — Kamuya açık teslimat (headless)

**Ekranda:** Yayınlanmış içerik, REST ile tüketilir; vitrin teknolojisi (ör. Next.js) bağımsız evrilebilir.

**Konuşmacı notları:**

- Storefront’lar kimlik doğrulaması olmadan, tenant bağlamında (host / header) içerik alır ([cms-delivery.md](../modules/cms-delivery.md)).
- Çok dil: site dili ve içerik dili modeliyle desteklenir; vitrin tarafında dil listesi tenant’tan gelir (dokümantasyon özeti).
- SEO sinyalleri: site haritası ve robots gibi uçlar dokümante edilmiştir — “arama görünürlüğü için teknik zemin var” mesajı.

---

## Slayt 8 — Editör deneyimi: SmartEdit (özet)

**Ekranda:** Canlı vitrin görünümü üzerinde güvenli önizleme; yayın akışı kontrollü kalır.

**Konuşmacı notları:**

- SmartEdit, admin tarafında vitrini çerçeve içinde açar; kısa ömürlü önizleme bileti ile **taslak ile canlıyı ayırır** ([smartedit.md](../modules/smartedit.md)).
- İş mesajı: “İçerik ekibi sayfayı gerçek bağlamda görür; yanlışlıkla canlıyı bozma riski azaltılır.”
- Kapsam fazları dokümante; tam slot yeniden düzenleme / onay iş akışı gibi konuları “yol haritası”nda konuşun, faz 1 dışında vaat etmeyin.

---

## Slayt 9 — Site yönetimi ve ölçümleme yüzeyi

**Ekranda:** Tek panelde site özeti; (yapılandırıldıysa) analitik ve arama görünürlüğü özetleri

**Konuşmacı notları:**

- Site Dashboard: genel bilgiler, teknik SEO ayarları, güvenlik politikası özetleri ve entegrasyonlu ölçüm kartları ([site-dashboard.md](../modules/site-dashboard.md)).
- GA4, Search Console, CrUX gibi entegrasyonlar **yapılandırma ve izin** gerektirir; canlıda “anında dolu” vaadi vermeyin.
- İletişim talepleri ve (modül varsa) abonelik / kampanya yüzeyleri tenant içinde yönetilir.

---

## Slayt 10 — Dış dünya: craftive.io ve talep toplama

**Ekranda:** craftive.io — “Fikrinizi paylaşın” akışı; dokümantasyon bağlantısı

**Konuşmacı notları:**

- Pazarlama sitesi statik Next.js, Cloudflare Pages; demo talebi ve bülten akışları platform API’leriyle bağlanır ([docs/README.md](../README.md) landing bölümü).
- Bu slayt “ürünün pazar yüzü ile operasyonel backend aynı ailedir” güvenini verir ([craftive.io/en/](https://craftive.io/en/)).
- Dokümantasyon adresi sunumda bağlantı olarak gösterilebilir (ör. docs.craftive.io).

---

## Slayt 11 — Teslimat modeli ve fiyatlandırma çerçevesi

**Ekranda:** Keşif → kapsam → önerilen modüller → demo / teklif → kurulum → işletme

**Konuşmacı notları:**

- Strategy’deki satış akışını görselleştirin: keşif görüşmesi, ihtiyaç netleştirme, modül önerisi, demo veya teklif ([strategy.md](./strategy.md) bölüm 6).
- Fiyat: modül karmaşıklığı, içerik yapısı, vitrin uyarlama, entegrasyon ve destek seviyesine bağlı — **paket fiyat listesi yok** mesajını net söyleyin.
- Başarı metrikleri (qualified call, proposal win, teslim süresi) iç içe konuşulabilir; müşteriye “birlikte ölçeriz” yaklaşımı.

---

## Slayt 12 — Özet ve sonraki adım

**Ekranda:** Özet 3 madde | Sonraki adım: pilot kapsam / teknik derin oturum / teklif

**Konuşmacı notları:**

- Özet: (1) Modüler, yeniden kullanılabilir temel. (2) Kiracı izolasyonu ve headless teslimat. (3) Proje bazlı teslim ve büyüme.
- Sonraki adım seçenekleri: sınırlı pilot tenant, mimari Q&A, güvenlik soruları listesi, referans demo ortamı.
- Açık soruları toplayın; teknik ekip için ayrı oturum planlayın.

---

## Ek — Kaçınılacak / tercih edilen dil (strategy ile hizalı)

**Tercih edilen:** modüler, özelleştirilebilir, kiracı izolasyonu, yeniden kullanılabilir platform, headless’a hazır.

**Kaçınılacak:** “herkes için self-serve SaaS”, “sabit abonelik paketi”, “tek ürün her şeye çözüm”.
