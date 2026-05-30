# Craftive Landing — İçerik Referans Dokümanı

> Son güncelleme: 2026-05-24
> Dil: Türkçe (sloganlar dışında)
> Kullanım: Landing page içerik girişi için başucu rehberi. Her sayfa için hazır TR copy + alt başlıklar + CTA metni içerir.

---

## 1. Yönetici Özeti

Craftive; bir fikri olan ama altyapıyı sıfırdan kurmak istemeyen büyüyen şirketler ve dijital ajanslar için **yönetilen (managed) modüler dijital ürün teslimat platformudur**. Müşteri ihtiyacını anlatır, Craftive ekibi izole bir kiracı ortamı (tenant) hazırlar, içerik mimarisini kurar ve canlıya alır. Sonuçta müşteri; headless REST API, sayfa oluşturucu (page builder) ve kendi içerik yönetimi olan, üretim-hazır bir dijital ürüne sahip olur.

Marka iki cümleyle özetlenir:
- **EN sloganı (kurumsal tag):** *"One platform, every configuration."*
- **TR ana vaat:** *"Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim."*

İkisi birlikte şunu söyler: **Tek bir paylaşılan altyapı, her projeye göre yeniden yapılandırılır; ve bu yapılandırmayı sizin yerinize biz kurarız.**

### Bu dokümanın özet kararları

| # | Karar | Sonuç |
|---|---|---|
| 1 | Dil | Tüm landing TR; slogan EN olarak korunur |
| 2 | Site haritası | 8 üst sayfa + 5 Çözümler alt sayfası = 13 sayfa |
| 3 | Karşılaştırma sayfaları | Yok (Contentful/Strapi farkı sayfa içine yedirilir) |
| 4 | Fiyatlandırma | Public fiyat yok — projeye özel teklif |
| 5 | Birincil CTA | "Demo Talep Et" / "Projenizi Anlatın" |
| 6 | İkincil CTA | Newsletter aboneliği |
| 7 | Üçüncül CTA | Docs (`docs.craftive.io`) — geliştirici güven sinyali |

---

## 2. Ürün Tanımı

### Tek satırlık tanım

Craftive; tek bir paylaşılan altyapı üzerinde, projeye özel yapılandırılmış dijital ürünler (kurumsal site, e-ticaret, İK portalı, blog, içerik platformu) teslim eden modüler bir platformdur.

### Üç katmanlı açıklama

**1. Paylaşılan altyapı:** Sıfırdan sunucu, veritabanı, deploy pipeline kurmaya gerek yok. Tüm projeler aynı sağlam temel üzerinde yaşar; her birinin kendi izole veritabanı vardır.

**2. Modüler yapı:** Sayfa oluşturucu, medya yönetimi, ürün katalogu, mail pazarlama gibi yetenekler projeye göre açılır veya kapatılır. İhtiyacınız kadar başlarsınız, sonradan büyütürsünüz.

**3. Yönetilen teslimat:** Kurulum, içerik mimarisi, frontend geliştirme, staging ve production deploy — tüm süreç Craftive ekibi tarafından yürütülür. Siz fikrinizi anlatın, biz inşa edelim.

### Craftive'in **olmadığı** şeyler

Bu liste hem iç ekibe hem dışarıya net bir çerçeve verir:
- ❌ Self-serve bir araç değil (sürükle-bırak ile siz tek başınıza site yapmazsınız)
- ❌ Tek tip hazır şablon ürünü değil (her teslimat müşteri ihtiyacına özel)
- ❌ Sadece bir tasarım aracı değil (arkada gerçek bir backend, API ve içerik mimarisi var)
- ❌ Sabit paketli abonelik değil (her proje, kendi kapsamına göre fiyatlanır)
- ❌ Salt CMS değil (sayfa, içerik, ürün katalogu, kullanıcı yönetimi tek çatı altında)

---

## 3. Hedef Kitle & İdeal Müşteri Profili (ICP)

### Birincil segment: Büyüme aşamasındaki KOBİ'ler ve dijital ajanslar (Türkiye)

| Kriter | Değer |
|---|---|
| Şirket boyutu | 10–200 çalışan |
| Bütçe aralığı | 5.000 USD – 50.000 USD (proje başına) |
| Coğrafya (yakın vade) | Türkiye |
| Coğrafya (orta vade) | İngilizce konuşan pazarlar |
| Karar verici rolleri | CTO, Ürün Müdürü, Operasyon Direktörü, Ajans Sahibi, Kurucu |

### Tipik tetikleyiciler

Bir şirketin Craftive'le konuşmaya başlamasına neden olan sinyaller:
- Yeni bir ürün ya da marka çıkarıyor, kurumsal site sıfırdan kurulacak
- Eski bir CMS'i (WordPress, eski-nesil tescilli yazılım) değiştirmek istiyor
- Tek siteden çok siteye / çok markaya / çok dile geçiyor
- Bir e-ticaret veya içerik projesi için "Webflow + Contentful + custom API" yığınını birleştirmekten yoruldu
- GDPR / KVKK uyumlu veri izolasyonu istiyor
- DevOps ekibi yok ama altyapısı dağılmadan bir ürün çıkarmak istiyor

### Persona Kartları

#### Persona 1 — Kurumsal Karar Verici (CTO / Operasyon Direktörü)

| Alan | Açıklama |
|---|---|
| Profil | 35–50 yaş, 50–200 kişilik bir şirkette teknik veya operasyonel liderlikte |
| Temel sorumluluk | Şirketin dijital varlığını yönetmek, riskleri kontrol altında tutmak |
| Problemler | Geliştirici ekibi küçük veya yok; mevcut sistem dağınık; veri uyumluluğundan kişisel olarak sorumlu |
| Tetikleyici | Yeni bir ürün çıkışı veya eski sistemin yetmemesi |
| Satın alma itirazları | "Verim güvende mi?", "Sonradan bağımlı kalır mıyım?", "Operasyon kim yürütecek?" |
| Hangi sayfayla buluşturulur | Anasayfa → Özellikler → Hakkımızda → İletişim |

#### Persona 2 — Ajans Sahibi / Operasyon Lideri

| Alan | Açıklama |
|---|---|
| Profil | 10–50 kişilik dijital ajansın sahibi veya delivery lideri |
| Temel sorumluluk | Müşterilere zamanında, kârlı projeler teslim etmek |
| Problemler | Her müşteri için sıfırdan kurmak operasyonu eritiyor; tekrar eden iş yükü; çok müşterili senaryolarda ölçeklenememe |
| Tetikleyici | Aynı anda 3+ proje sıkışması, bir delivery hattı kurma ihtiyacı |
| Satın alma itirazları | "Markamızın altına alabilir miyim?", "Özelleştirme sınırı ne?", "Müşteriye nasıl konumlandırırım?" |
| Hangi sayfayla buluşturulur | Çözümler → Ajanslar için → Özellikler → İletişim |

#### Persona 3 — Dijital Proje Sahibi (Kurucu / Ürün Müdürü)

| Alan | Açıklama |
|---|---|
| Profil | 25–40 yaş, küçük ekipli bir girişimde veya kurum içi yeni bir dijital projenin sahibi |
| Temel sorumluluk | Fikri hızlıca pazara çıkarmak ve doğrulamak |
| Problemler | Bütçe sınırlı; teknik birikim parça parça; ileride yeniden yazmamak istiyor |
| Tetikleyici | İlk MVP'yi çıkarma baskısı veya pilot bir doğrulama süreci |
| Satın alma itirazları | "Çok mu uzun sürer?", "İleride büyüyebilir miyim?", "Sahiplik kimde olur?" |
| Hangi sayfayla buluşturulur | Anasayfa → Kullanım Alanları → Fiyatlandırma → İletişim |

### Genel itiraz tablosu (TR)

İçerik yazarken bu itirazlara dolaylı veya doğrudan yanıt veren cümleler ekleyin.

| İtiraz | Yanıt cümlesi (kopyalanabilir) |
|---|---|
| "Zaten Webflow / WordPress kullanıyoruz" | "Webflow ve benzeri araçlar tasarımı çözer; Craftive arkadaki içerik mimarisini, API'yi ve büyüme yolunu da çözer." |
| "Geliştirici tutsam daha iyi olmaz mı?" | "Tek bir geliştirici aylar süren bir altyapıyı yeniden yazmak yerine, hazır temelin üzerine doğrudan ürününüze odaklanır." |
| "Verimiz güvende mi?" | "Her müşteri için ayrı, izole bir veritabanı kullanırız. Veriler birbirine sızmaz, KVKK / GDPR uyumlu çalışır." |
| "Sonradan bağımlı kalır mıyız (vendor lock-in)?" | "İçeriğiniz açık REST API üzerinden her zaman size aittir; veriler taşınabilir, frontend bağımsız geliştirilebilir." |
| "İleride büyütemez miyim?" | "Modüller proje canlıya çıktıktan sonra da açılabilir. Yeniden yazmadan, üzerine eklenerek büyür." |
| "Çok mu uzun sürer?" | "Tipik bir kurumsal site 4–8 hafta içinde canlıya alınır; kapsam küçükse daha kısa, e-ticaret veya çok-dilli projelerde daha uzun." |

---

## 4. Pozisyonlama & Mesaj Framework

### Pozisyonlama cümlesi (TR)

> Üretim-hazır bir dijital ürüne, sıfırdan altyapı kurmadan ihtiyacı olan büyüme aşamasındaki şirketler ve ajanslar için Craftive; izole, modüler ve yönetilen bir teslimat platformudur. Self-serve araçlardan farkı: süreci size bırakmaz, sizin için yürütür.

### Ana mesaj

> **Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim.**

Bu cümle, anasayfa hero'sunun H1'i olarak kullanılır. Diğer sayfalarda aynı vaadin farklı varyasyonları (örn. "Projenizi anlatın, kurulumu biz yapalım") destek mesaj olarak yer alır.

### Üç mesaj sütunu (messaging pillar)

#### Sütun 1 — Hızlı Teslimat, Üretim Hazır

**Başlık:** "Aylar sürmesin, haftalarla bitsin."

Sıfırdan sunucu kurulumu, deploy pipeline'ı ve içerik mimarisi yazmak yerine; Craftive'in hazır temelinin üzerine projenizin **kendine özgü kısmı** inşa edilir. Sonuçta haftalar içinde test ortamında, kısa süre sonra üretimde çalışan bir ürün elinizde olur.

**Kanıt noktası:** 4 adımlı teslimat — keşif görüşmesi → kapsam ve modül seçimi → kurulum ve içerik mimarisi → canlıya alım.

#### Sütun 2 — Kurumsal Altyapı, KOBİ Bütçesi

**Başlık:** "Banka seviyesinde izolasyon, küçük takımlar için."

Her müşterinin kendi izole veritabanı vardır; veriler birbirine sızmaz. KVKK / GDPR uyumlu mimari, çok kiracılı (multi-tenant) yapı ve süreklilik garantisi büyük şirketlere has kabul edilirdi — Craftive aynısını proje bazlı bir bütçeyle sunar.

**Kanıt noktası:** Müşteri başına ayrı veritabanı, sıfır veri sızıntısı riski, paylaşılan deploy hattı sayesinde sürekli güncel altyapı.

#### Sütun 3 — Modüler, Sonradan Büyür

**Başlık:** "Bugün ihtiyacın kadar başla, yarın olduğunda ekle."

Sayfa oluşturucu, medya yönetimi, ürün katalogu, mail pazarlama gibi modüller projeye göre açılır veya kapatılır. Canlıya çıktıktan sonra da yeni modüller eklenebilir. Yani küçük başlayan bir blog, ileride e-ticarete dönüşebilir — yeniden yazılmadan.

**Kanıt noktası:** Modüller açıp kapatılabilir; post-launch eklenebilir; mimari değişmeden büyür.

### "Self-serve değil, managed" anlatımı

Strapi ve Contentful gibi araçlar **size kontrol paneli verir, ondan sonrası size kalır.** Craftive'in farkı: kontrol paneli de var, ama altındaki tüm süreci (kurulum, içerik mimarisi, deploy, post-launch destek) sizin yerinize yürütüyoruz. Yani CMS değil, **CMS dahil bir teslimat ortağı**.

Bu paragrafın varyasyonları şu sayfalara serpiştirilir:
- **Özellikler** sayfasında "Sadece bir araç değil" kutusu
- **Hakkımızda** sayfasında "Neden farklıyız" bölümü
- **Kullanım Alanları**nda her hikayenin sonunda "süreci kim yürüttü" notu

---

## 5. Marka Dili & Slogan Kullanım Kuralları

### Slogan kullanım rehberi

| Sayfa | H1 (ana başlık) | Destek satırı |
|---|---|---|
| Anasayfa | "Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim." | *"One platform, every configuration."* |
| Çözümler hub | "Her ihtiyaca, aynı sağlam temel." | *"One platform, every configuration."* |
| Özellikler | "Modüler. Ölçeklenebilir. Sizin için yönetilen." | EN slogan footer'da |
| Kullanım Alanları | "Bir fikir, bir mimari, sayısız sonuç." | EN slogan altta |
| Fiyatlandırma | "Projenize göre fiyat, kapsamınıza göre paket." | EN slogan kullanılmaz |
| Blog | "Mimari, içerik, teslimat üzerine notlar." | EN slogan kullanılmaz |
| Hakkımızda | "Tek bir platform, sayısız konfigürasyon." (TR çevirisi) | EN slogan vurgulu, marka manifestosu olarak |
| İletişim | "Anlatın, eşlik edelim." | EN slogan kullanılmaz |

**Kural:** EN slogan her sayfada **en az bir yerde** (footer veya hero altı) görünür. TR ana cümle her sayfada bir varyasyonla geçer. İkisi aynı sayfada art arda tekrarlanmaz.

### Marka tonalitesi

| Şu olalım | Şu olmayalım |
|---|---|
| Modüler, esnek | Şişirilmiş, abartılı |
| Güvenilir, ortak yol yürüyen | Hızlı satıcı, push'lu |
| Teknik ama sade | Jargon yüklü |
| Net, vaad eden | Bulanık, "her şeyi yaparız" |
| Süreç odaklı, şeffaf | Sihirli kutu, "siz merak etmeyin" |

Hedef hissiyat: "Bu insanlar konuyu biliyor, vaadini tutar, sürecimi düzene sokar."

### Yasak ifadeler (TR ve EN)

Aşağıdaki kelimeleri **hiçbir copy'de** kullanmayın:

- "self-serve product" / "self-serve ürün"
- "fixed subscription package" / "sabit abonelik paketi"
- "tek tip ürün"
- "abonelik" (Craftive bir abonelik değil)
- "SaaS" (Craftive bir SaaS'ı değil, yönetilen bir teslimat platformudur)
- "ucuz" / "en uygun fiyatlı" — fiyat odaklı değil, değer odaklı konumlanıyoruz
- "her şeyi yaparız" / "all-in-one solution" — kapsam belirsizliği yaratır

### Tercih edilen kelimeler

- **modüler** — ürünün özünü tek kelimede anlatır
- **özelleştirilebilir** — esnekliği vurgular
- **tenant-izole** / **izole** — güvenliği teknik ifadeyle aktarır
- **paylaşılan altyapı** — operasyonel avantajı
- **headless-hazır** — geliştirici kitlesine sinyal
- **yönetilen teslimat** / **managed delivery** — temel farkı
- **projeye özel** — tek-tip değil mesajı
- **uçtan uca** — kurulumdan canlıya tüm süreç

---

## 6. Site Haritası

### 8 üst sayfa + 5 Çözümler alt sayfası

```text
Anasayfa
│
├── Çözümler (hub)
│   ├── Kurumsal Web Siteleri
│   ├── E-ticaret & Headless Storefront
│   ├── İK Portalları & Kurum İçi Siteler
│   ├── Blog & İçerik Platformları
│   └── Ajanslar için Delivery Platformu
│
├── Özellikler
├── Kullanım Alanları
├── Fiyatlandırma
├── Blog
├── Hakkımızda
└── İletişim
```

### Her sayfanın tek satırlık amacı

| Sayfa | Amaç | Birincil aksiyon |
|---|---|---|
| Anasayfa | Vaadi 5 saniyede iletmek, demo talebine yönlendirmek | Demo Talep Et |
| Çözümler (hub) | Hangi dikey için ne sunduğumuzu özetlemek | Alt sayfaya geçiş |
| Çözümler / alt sayfa | Belirli bir dikey için ürün hikayesi | Demo / İletişim |
| Özellikler | Modüler altyapının "ne içerdiğini" sade dille göstermek | Çözümler veya İletişim |
| Kullanım Alanları | Senaryo/vaka odaklı anlatım, hangi tip iş hangi modüllerle çözülür | Demo Talep Et |
| Fiyatlandırma | Projeye özel modeli açıklayıp şeffaflık vermek | Görüşme Talep Et |
| Blog | Otorite + SEO + lead nurture | Newsletter abonelik / İletişim |
| Hakkımızda | Marka hikayesi, ekip, güven sinyalleri | Demo Talep Et |
| İletişim | Demo / discovery call talebi formu | Form doldurma |

### Çözümler vs Kullanım Alanları ayrımı

Bu iki sayfa kolaylıkla karışır. Net ayrım:

- **Çözümler** = **ürün dikeyi**. "E-ticaret çözümümüz", "Kurumsal site çözümümüz". Müşteri ne **alıyor**? sorusuna cevap.
- **Kullanım Alanları** = **vaka/senaryo**. "Bir İK ekibi Craftive ile süreçlerini nasıl dönüştürdü". Müşteri ne **yapıyor**? sorusuna cevap.

İkisi de değer üretir; Çözümler aktif-arayan trafiğini, Kullanım Alanları araştırma-aşamasındaki trafiği yakalar. Navbar'da Çözümler ön planda, footer'da Kullanım Alanları öne çıkar.

---

## 7. Sayfa-Sayfa İçerik Blueprint'i

Bu bölüm 13 sayfa için içerik hazırlamak isteyen editör için tek tek şablon sunar. Her sayfanın altında **hazır TR copy** vardır; doğrudan kopyalanabilir.

### 7.1 Anasayfa

| Alan | Değer |
|---|---|
| URL kararı | `/` |
| Sayfa amacı | Vaadi 5 saniyede iletmek; ziyaretçinin "bu benim için mi?" sorusuna evet/hayır cevabı vermesini sağlamak; en güçlü adayı doğrudan İletişim formuna sürüklemek |
| Birincil hedef kitle | 3 personanın tümü — en geniş kapı |
| Birincil hedef anahtar kelime | "yönetilen dijital ürün teslimatı" |
| İkincil hedef anahtar kelimeler | "modüler dijital platform", "headless CMS Türkiye", "multi-tenant web platformu" |
| Search intent | Karışık — informational + commercial |

#### Hero (above-the-fold)

**H1 (TR ana cümle):**
> Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim.

**Alt başlık (1–2 cümle):**
> Craftive; kurumsal site, e-ticaret, İK portalı veya içerik platformu — fikrinizi söyleyin, izole altyapısı ve modüler mimarisiyle ürününüzü baştan sona biz inşa edelim.

**Brand tag (EN slogan, alt satır veya badge):**
> *one platform, every configuration.*

**Hero birincil CTA:**
> Demo Talep Et

**Hero ikincil CTA (text link):**
> Önce ürünü tanıyın → Özellikler

#### Bölüm 2 — "Craftive nedir?" (kısa tanıt)

**H2:** "Bir platform, sayısız konfigürasyon."

**Açıklama metni (2–3 cümle):**
> Craftive; tek bir paylaşılan altyapı üzerinde, projeye özel yapılandırılmış dijital ürünler teslim eder. Her müşterinin kendi izole veritabanı vardır. Sıfırdan başlamak yerine, hazır temelin üzerinde projenizin kendine özgü kısmına odaklanırız.

**3 sütunlu hızlı özellik tanıtımı:**

| Sütun başlığı | 1 cümle açıklama |
|---|---|
| 🧩 Modüler mimari | Page Builder, medya, ürün katalogu, mail — ihtiyaç kadar açın, sonra büyütün. |
| 🔒 İzole veritabanı | Her müşteri için ayrı veritabanı; KVKK/GDPR uyumlu, sıfır sızıntı riski. |
| ⚡ Yönetilen teslimat | Kurulum, içerik mimarisi, frontend, deploy — uçtan uca biz yürütürüz. |

#### Bölüm 3 — "Kimler için?" (segment-yönlendirme)

**H2:** "Kimler için?"

**Açıklama metni (1–2 cümle):**
> Sıfırdan altyapı kurmadan üretim-hazır bir dijital ürüne ihtiyacınız varsa, doğru yerdesiniz.

**4 sütunlu segment kartı (her biri Çözümler alt sayfasına link):**

| Kart başlığı | Mini-açıklama (1 cümle) | Link |
|---|---|---|
| Kurumsal Web Siteleri | Marka sitenizi, içerik mimarisiyle birlikte kuruyoruz. | → Çözümler / Kurumsal |
| E-ticaret & Storefront | Headless mimari ile esnek, ölçeklenebilir mağaza altyapısı. | → Çözümler / E-ticaret |
| İK Portalları | Çalışanlara özel, izole ve güvenli kurum içi siteler. | → Çözümler / İK |
| Blog & İçerik Platformları | Hızlı yayın, çok-dilli içerik, gerçek bir API arkada. | → Çözümler / Blog |

#### Bölüm 4 — "Nasıl Çalışıyoruz?" (4 adımlı süreç)

**H2:** "Fikirden canlıya, dört adımda."

| Adım | Başlık | 1–2 cümle |
|---|---|---|
| 01 | Keşif görüşmesi | Fikrinizi, hedef kitlenizi ve kapsamınızı dinleriz. |
| 02 | Kapsam & modül seçimi | İhtiyacınıza uygun modüller netleşir, teklif sunulur. |
| 03 | Kurulum & içerik mimarisi | İzole ortamınız hazırlanır, içerik yapısı kurulur, frontend geliştirilir. |
| 04 | Canlıya alım & devam | Üretim ortamına alır, sonrasında destek ve büyüme süreçlerinde eşlik ederiz. |

#### Bölüm 5 — Üç değer sütunu (messaging pillars)

**H2:** "Neden Craftive?"

3 sütun — Bölüm 4'teki messaging pillar'lardan kısa varyant:

| Başlık | Cümle |
|---|---|
| Hızlı teslimat, üretim hazır | Aylar yerine haftalar içinde test ortamında, kısa süre sonra üretimde çalışan bir ürün. |
| Kurumsal altyapı, KOBİ bütçesi | Banka seviyesinde izolasyon — proje bazlı bir bütçeyle. |
| Modüler, sonradan büyür | İhtiyaç kadar başlayın, canlıya çıktıktan sonra ekleyin. |

#### Bölüm 6 — "Self-serve değil, managed" (fark anlatımı)

**H2:** "Sadece bir araç değil, bir teslimat ortağı."

**Açıklama (3–4 cümle):**
> Headless CMS pazarındaki birçok araç size sadece bir kontrol paneli verir; ondan sonrası size kalır. Craftive farklıdır: kontrol paneli var, ama altındaki tüm süreci — kurulum, içerik mimarisi, frontend geliştirme, deploy, canlı sonrası destek — sizin yerinize biz yürütüyoruz. Sonuç: bir araç değil, bir teslimat ortağı.

#### Bölüm 7 — Logos / güven satırı

**H2 yok** (sade bir bant).

**Üst yazı:**
> Aynı altyapı, çeşitli sektörler.

**Logos:** Eğer hazır müşteri logosu yoksa "framework partners" stratejisi — Spring Boot, Next.js, Cloudflare, DigitalOcean, Angular gibi teknoloji logoları.

**Alternatif:** "Bağımsız mimari kararlarımız" başlığı altında kullandığımız altyapı bileşenleri (güven sinyali olarak).

#### Bölüm 8 — Footer öncesi büyük CTA bandı

**H2:** "Fikriniz var mı? Konuşalım."

**Açıklama (1 cümle):**
> 30 dakikalık keşif görüşmesi; size en uygun çözümü, hangi modüllerle ve nasıl bir takvimle ilerleyebileceğimizi konuşuruz.

**Birincil CTA:** Demo Talep Et
**İkincil CTA:** Önce dokümantasyonu inceleyin

#### Sosyal kanıt / güven sinyali fikirleri (Anasayfa)

- "X. yıl, Y. proje" gibi sayı vurgusu (rakam netleşince)
- Müşteri testimonial alıntısı (1–2 cümle, isim + rol)
- Sektör bazlı "şu tip projelerde çalıştık" listesi
- Teknoloji partner logo bandı
- GitHub stars / open-source bileşen referansı (varsa)

---

### 7.2 Çözümler (hub sayfası)

| Alan | Değer |
|---|---|
| URL kararı | `/cozumler` |
| Sayfa amacı | Ziyaretçiyi kendi sektörüne/projesine uygun alt sayfaya yönlendirmek |
| Birincil hedef kitle | Sektörü/projesi netleşmiş ziyaretçi (commercial intent) |
| Birincil hedef anahtar kelime | "dijital çözümler" |
| İkincil hedef anahtar kelimeler | "kurumsal web site çözümü", "headless e-ticaret platformu", "İK portalı kurulum" |
| Search intent | Commercial |

#### Hero

**H1:**
> Her ihtiyaca, aynı sağlam temel.

**Alt başlık:**
> Kurumsal siteden e-ticarete, İK portalından içerik platformuna — Craftive tek bir altyapı üzerinde her projeyi farklı yapılandırır.

**Brand tag:** *one platform, every configuration.*

**CTA:** Hangi alandasınız? Aşağıdan seçin.

#### Bölüm 2 — 5 dikey kartı

5 büyük kart, her biri ilgili alt sayfaya link verir:

| Kart başlığı | 2 cümle özet | "Detayına git" link |
|---|---|---|
| 🏢 Kurumsal Web Siteleri | Markanızın yüzü; sayfa oluşturucu, çok-dilli içerik ve sağlam SEO altyapısıyla. Marka rehberinize uygun, içerik ekibinize bağımsız. | Kurumsal çözümünü incele |
| 🛒 E-ticaret & Headless Storefront | Headless mimari, esnek ürün katalogu, modern Next.js storefront. Mağazanız bağımsız, API'niz açık. | E-ticaret çözümünü incele |
| 👥 İK Portalları | Çalışanlara özel, izole ve güvenli kurum içi siteler. Duyurular, belgeler, formlar — tek çatı altında. | İK çözümünü incele |
| ✍️ Blog & İçerik Platformları | Hızlı yayın akışı, çok-dilli içerik, gerçek API. Editörünüze hızlı, geliştiricinize esnek. | İçerik çözümünü incele |
| 🏗️ Ajanslar için Delivery Platformu | Müşterilerinizi aynı altyapıya alın; her projeyi izole tutarak operasyonu ölçekleyin. | Ajans çözümünü incele |

#### Bölüm 3 — "Hangi modül hangi çözümde?" (özet matris)

**H2:** "Hangi modüller, hangi çözümde?"

| Modül | Kurumsal | E-ticaret | İK | İçerik | Ajans |
|---|---|---|---|---|---|
| Sayfa Oluşturucu | ✓ | ✓ | ✓ | ✓ | ✓ |
| Medya Kütüphanesi | ✓ | ✓ | ✓ | ✓ | ✓ |
| Ürün Katalogu | – | ✓ | – | – | opsiyonel |
| Mail Pazarlama | opsiyonel | ✓ | opsiyonel | ✓ | opsiyonel |
| Çok-tenant Yönetimi | – | – | – | – | ✓ |

#### Bölüm 4 — CTA

**H2:** "Çözümünüze emin değil misiniz?"

**Açıklama:**
> 30 dakikalık keşif görüşmesinde, sizin için en uygun yapılandırmayı birlikte netleştirelim.

**CTA:** Görüşme Talep Et

---

### 7.3 Çözümler / Kurumsal Web Siteleri

| Alan | Değer |
|---|---|
| URL kararı | `/cozumler/kurumsal-web-siteleri` |
| Sayfa amacı | Kurumsal karar verici personasını ikna etmek; "biz bu işi yapanız" demek |
| Birincil hedef kitle | CTO / Pazarlama Direktörü / Operasyon Direktörü |
| Birincil hedef anahtar kelime | "kurumsal web sitesi çözümü" |
| İkincil hedef anahtar kelimeler | "kurumsal site kurulumu", "marka web sitesi yönetimi", "çok dilli kurumsal site" |
| Search intent | Commercial |

#### Hero

**H1:**
> Markanızın yüzü, içerik ekibinizin elinde.

**Alt başlık:**
> Kurumsal sitenizi, sayfa oluşturucu, medya yönetimi ve çok dilli içerik altyapısıyla baştan kuruyoruz. Geliştirici beklemeden içeriğinizi güncelleyin.

**Birincil CTA:** Demo Talep Et
**İkincil CTA:** Diğer çözümlere bak

#### Bölüm 2 — Tipik müşteri

**H2:** "Kim için?"

**Açıklama (3 cümle):**
> 50–500 kişilik şirketler. Marka itibarı önemli, yeni bir kurumsal site veya eski sistemden geçiş yapacak. Kurum içi geliştirici ekibi küçük veya yok; pazarlama / kurumsal iletişim ekibi içeriği sahiplenecek.

#### Bölüm 3 — Karşılaştığınız problem

**H2:** "Bu tanıdık geliyor mu?"

Bullet listesi:
- Eski siteniz yıllar içinde dağıldı; her küçük güncelleme bir geliştirici gerektiriyor.
- Çok dilli içerik kontrolden çıktı; sayfalar tutarsız.
- Pazarlama ekibi içerik güncellemek için IT'yi bekliyor.
- Yeni bir alt marka çıkacak; yeni bir site daha açmak istemiyorsunuz.

#### Bölüm 4 — Çözümümüz

**H2:** "Kurumsal site, baştan sona."

**Açıklama (3–4 cümle):**
> Sizin için izole bir Craftive ortamı kurarız. İçerik mimarisi, sayfa şablonları ve marka rehberinize uygun bileşenler hazırlanır. Çok dilli içerik desteği baştan açıktır. Site canlıya alındıktan sonra içerik ekibiniz tek başına yönetir — her küçük değişiklik için geliştiriciye ihtiyaç kalmaz.

#### Bölüm 5 — Neler dahil?

**H2:** "Bu çözümde neler var?"

Bullet listesi (modül isimleri yerine yetenek dili):
- ✓ Sayfa oluşturucu (drag-drop ile içerik düzenleme)
- ✓ Medya kütüphanesi (görsel/video/dosya yönetimi, otomatik boyutlandırma)
- ✓ Çok dilli içerik altyapısı
- ✓ Marka rehberinize özel bileşen kütüphanesi
- ✓ SEO temel altyapısı (meta yönetimi, sitemap, robots)
- ✓ Bağımsız frontend (Next.js veya kendi tercih ettiğiniz framework)
- ✓ Headless REST API — gelecekteki entegrasyonlar için hazır

#### Bölüm 6 — Tipik teslimat süresi

**H2:** "Ne kadar sürer?"

**Açıklama:**
> Tipik bir kurumsal site, kapsama göre **4–8 hafta** içinde canlıya alınır. Tek dilli ve sade yapılı projeler daha kısa; çok markalı, çok dilli veya yoğun özelleştirme isteyen projeler bunun üstüne çıkabilir.

#### Bölüm 7 — Sıkça sorulan sorular (3–5 soru)

**H2:** "Sıkça sorulan sorular"

| Soru | Cevap |
|---|---|
| İçeriği biz yönetebilecek miyiz? | Evet. Site canlıya alındıktan sonra tüm içerik yönetimi sizdedir. Geliştiriciye ihtiyaç duymadan sayfa ekleyip güncelleyebilirsiniz. |
| Verilerimiz kimde duruyor? | Her müşterinin kendi izole veritabanı vardır. Veriler size aittir; istediğiniz zaman taşınabilir veya yedeklenebilir. |
| Yeni bir alt marka çıkarsak ne olur? | Aynı altyapıda yeni bir tenant açılır. Sıfırdan kurulum gerekmez; günler içinde yeni site canlıya alınabilir. |
| Mevcut sitemden veri taşıyabilir misiniz? | Evet. Mevcut içeriği yapılandırıp Craftive içerik mimarisine aktarırız. Kapsam görüşmesinde detaylandırılır. |

#### Bölüm 8 — CTA

**H2:** "Kurumsal sitenizi konuşalım."

**Açıklama:** Demo talebinizi alalım, 30 dakikalık keşif görüşmesinde kapsamınızı netleştirelim.

**Birincil CTA:** Demo Talep Et

---

### 7.4 Çözümler / E-ticaret & Headless Storefront

| Alan | Değer |
|---|---|
| URL kararı | `/cozumler/e-ticaret-storefront` |
| Sayfa amacı | E-ticaret yatırımı yapacak teknik karar vericiyi headless yaklaşımına ve managed delivery'ye ikna etmek |
| Birincil hedef kitle | E-ticaret kurucusu, CTO, Operasyon Müdürü |
| Birincil hedef anahtar kelime | "headless e-ticaret platformu" |
| İkincil hedef anahtar kelimeler | "headless commerce Türkiye", "next.js mağaza altyapısı", "API tabanlı e-ticaret çözümü" |
| Search intent | Commercial / transactional |

#### Hero

**H1:**
> Esnek bir mağaza, sağlam bir altyapı.

**Alt başlık:**
> Headless mimariyle ürünleriniz arkada, mağaza önyüzü tamamen sizin tarafınızda. Marka deneyiminizi hiç ödün vermeden büyütün.

**Birincil CTA:** Demo Talep Et
**İkincil CTA:** Storefront önyüz örneğini gör

#### Bölüm 2 — Tipik müşteri

**H2:** "Kim için?"

**Açıklama:**
> Mağaza deneyimini tek tip Shopify temasına sıkıştırmak istemeyen markalar. Ürün sayısı 100'den 100.000'e ölçeklenecek operasyonlar. Çoklu pazaryeri, çoklu para birimi veya çok dilli operasyon planlayan markalar.

#### Bölüm 3 — Karşılaştığınız problem

**H2:** "Bu tanıdık geliyor mu?"

- Mevcut e-ticaret platformunuz tema yapısının dışına çıkmıyor; her özelleştirme yan-iş gibi geliyor.
- Mobil uygulamanız, web siteniz ve pazaryeri farklı sistemlerden besleniyor; veri tutarsız.
- Hızlı kampanya değişiklikleri (landing page'ler, sezon görselleri) IT'yi yoruyor.
- Veri ve müşteri tabanı sizin değil, platformun.

#### Bölüm 4 — Çözümümüz

**H2:** "Headless commerce + yönetilen teslimat."

**Açıklama (3–4 cümle):**
> Ürün katalogu, içerik ve müşteri verisi Craftive arkayüzünde tutulur — hepsi headless REST API üzerinden açılır. Önyüzünüz Next.js storefront ile teslim edilir; istediğinizde React Native ile mobil uygulamayı, istediğinizde başka bir kanalı bağlarsınız. Tüm sistem sizin için kurulur, deploy edilir ve sonrasında desteklenir.

#### Bölüm 5 — Neler dahil?

**H2:** "Bu çözümde neler var?"

- ✓ Ürün katalogu (varyant, kategori, stok)
- ✓ Çoklu para birimi & çoklu dil
- ✓ Headless REST API — mobil veya başka kanal için hazır
- ✓ Next.js tabanlı storefront (mevcut tema veya özel)
- ✓ Sayfa oluşturucu (kampanya & landing page üretimi)
- ✓ Medya kütüphanesi (ürün görselleri, otomatik boyutlandırma)
- ✓ Mail pazarlama entegrasyonu (opsiyonel)
- ✓ İzole veritabanı — müşteri verisi tamamen size ait

#### Bölüm 6 — Tipik teslimat süresi

**H2:** "Ne kadar sürer?"

**Açıklama:**
> E-ticaret projeleri kapsama göre **6–12 hafta** arasında canlıya alınır. Tek pazarlı, tek dilli ve sade tema seçimi daha kısa; çoklu pazar, çoklu dil, yoğun entegrasyon (ERP, ödeme, lojistik) daha uzun sürer.

#### Bölüm 7 — Sıkça sorulan sorular

| Soru | Cevap |
|---|---|
| Mevcut Shopify / WooCommerce verimi taşıyabilir misiniz? | Evet. Ürün, kategori ve müşteri verisi yapılandırılarak Craftive katalog yapısına aktarılır. |
| Ödeme entegrasyonu nasıl çalışır? | Önyüz tarafında size en uygun ödeme sağlayıcısı entegre edilir. Önerilen sağlayıcılar görüşmede netleşir. |
| Aynı katalogdan hem web hem mobil uygulama beslenebilir mi? | Evet. Tüm veri headless API üzerinden sunulduğu için web, mobil ve diğer kanallar aynı katalogu kullanır. |
| ERP / lojistik entegrasyonu mümkün mü? | Evet. Headless API üzerinden mevcut ERP, depo ve kargo sistemlerinizle çift yönlü entegrasyon kurulabilir. |

#### Bölüm 8 — CTA

**H2:** "Mağazanızı baştan kuralım."

**CTA:** Demo Talep Et

---

### 7.5 Çözümler / İK Portalları & Kurum İçi Siteler

| Alan | Değer |
|---|---|
| URL kararı | `/cozumler/ik-portallari` |
| Sayfa amacı | İK / kurumsal iletişim direktörlerini güvenli kurum içi site fikrine ikna etmek |
| Birincil hedef kitle | İK Direktörü, Operasyon Direktörü, İç İletişim Yöneticisi |
| Birincil hedef anahtar kelime | "İK portalı çözümü" |
| İkincil hedef anahtar kelimeler | "kurum içi site kurulumu", "çalışan portalı yazılımı", "izole intranet platformu" |
| Search intent | Commercial |

#### Hero

**H1:**
> Çalışanlarınıza özel, güvenli ve sade.

**Alt başlık:**
> Duyurular, belgeler, formlar, organizasyon haritası — hepsi tek bir kurum içi portalda. İzole altyapı sayesinde verileriniz dışarı çıkmaz.

**Birincil CTA:** Demo Talep Et
**İkincil CTA:** Diğer çözümlere bak

#### Bölüm 2 — Tipik müşteri

**H2:** "Kim için?"

**Açıklama:**
> 100–2.000 çalışanlı kurumlar. İç iletişim dağınık; duyurular e-posta, belgeler ortak sürücü, formlar farklı sistemlerden geçiyor. Tek bir noktada toplamak isteyen ve veri güvenliği yüksek bir İK ekibi.

#### Bölüm 3 — Karşılaştığınız problem

**H2:** "Bu tanıdık geliyor mu?"

- Şirket içi duyurular SharePoint, e-posta, WhatsApp arasında kayboluyor.
- Çalışan belgeleri farklı klasörlerde; arama imkânsız.
- İK formları (izin, talep, devam çizelgesi) hâlâ kâğıt ya da kişisel bilgisayarda Excel.
- Veri uyumluluğu açısından bulut depolama riskleri sizi rahatsız ediyor.

#### Bölüm 4 — Çözümümüz

**H2:** "Kurum içi portal, kurumunuza özel."

**Açıklama (3–4 cümle):**
> Sizin için izole bir Craftive ortamı kurarız. Çalışanlara özel giriş, departman bazlı içerik, duyuru akışı ve belge yönetimi tek panelde toplanır. İçerik mimarisi İK ekibinizin gözüyle tasarlanır. Veri sizin altyapınızda, izole bir veritabanında durur.

#### Bölüm 5 — Neler dahil?

**H2:** "Bu çözümde neler var?"

- ✓ Çalışan girişi (yetki bazlı erişim)
- ✓ Duyuru / haber akışı
- ✓ Belge ve form merkezi
- ✓ Departman bazlı içerik düzenleme
- ✓ Çok dilli içerik (uluslararası ekipler için)
- ✓ Sayfa oluşturucu (İK içerik ekibi tarafından kullanılabilir)
- ✓ İzole veritabanı — KVKK / GDPR uyumlu
- ✓ Mobil-uyumlu önyüz

#### Bölüm 6 — Tipik teslimat süresi

**H2:** "Ne kadar sürer?"

**Açıklama:**
> İK portalı projeleri tipik olarak **6–10 hafta** içinde canlıya alınır. Mevcut sistemlerden veri taşınacaksa veya özel rol/yetki yapıları varsa süre uzayabilir.

#### Bölüm 7 — Sıkça sorulan sorular

| Soru | Cevap |
|---|---|
| Çalışan verilerimiz nerede tutulur? | Sizin için ayrılmış, izole bir veritabanında. Veriler başka müşterilerle paylaşılmaz. |
| Mevcut SSO (single sign-on) sistemimize bağlanabilir mi? | Evet. Şirketinizin kullandığı kimlik sağlayıcısı (Azure AD, Google Workspace vb.) ile entegrasyon kurulabilir. |
| Sadece belirli kişiler belirli içerikleri görsün istiyoruz, mümkün mü? | Evet. Yetki ve rol bazlı erişim kontrolü baştan tanımlanır. |
| Mobil cihazlardan çalışacak mı? | Evet. Önyüz mobil-uyumlu teslim edilir; isterseniz native mobil uygulama da headless API üzerinden eklenebilir. |

#### Bölüm 8 — CTA

**H2:** "Çalışanlarınıza özel portal kuralım."

**CTA:** Demo Talep Et

---

### 7.6 Çözümler / Blog & İçerik Platformları

| Alan | Değer |
|---|---|
| URL kararı | `/cozumler/blog-icerik-platformu` |
| Sayfa amacı | İçerik odaklı projelerin (medya, yayıncı, çok yazarlı blog) Craftive'a ikna edilmesi |
| Birincil hedef kitle | Medya kurucusu, içerik yayın yöneticisi, kurumsal pazarlama lideri |
| Birincil hedef anahtar kelime | "headless CMS Türkiye" |
| İkincil hedef anahtar kelimeler | "çok dilli blog altyapısı", "yayıncı içerik platformu", "API tabanlı blog" |
| Search intent | Commercial |

#### Hero

**H1:**
> İçeriğiniz hızlı çıksın, esnek dağıtılsın.

**Alt başlık:**
> Headless API arkayüz, hızlı yayın akışı, çok dilli içerik. Editörünüze hızlı bir yönetim paneli, geliştiricinize açık bir API.

**Birincil CTA:** Demo Talep Et
**İkincil CTA:** Diğer çözümlere bak

#### Bölüm 2 — Tipik müşteri

**H2:** "Kim için?"

**Açıklama:**
> Düzenli içerik yayınlayan kurumsal pazarlama ekipleri, online medya kuruluşları, özel ilgi alanlı yayıncılar. Web sitesinin ötesine geçip aynı içeriği e-posta bültenine, mobile uygulamaya veya partnerlere de dağıtmak isteyen ekipler.

#### Bölüm 3 — Karşılaştığınız problem

**H2:** "Bu tanıdık geliyor mu?"

- WordPress yavaşladı; eklenti karmaşası içeriği yönetmeyi zorlaştırıyor.
- Aynı içeriği farklı kanallara (web, e-posta, mobil) elle kopyalıyorsunuz.
- Çok dilli içerik karmaşıklaştı; çeviri ve yayın akışı düzensiz.
- Geliştirici ekibiniz olmadan tema ve özellik değişikliği yapamıyorsunuz.

#### Bölüm 4 — Çözümümüz

**H2:** "Headless içerik altyapısı + yönetilen kurulum."

**Açıklama (3–4 cümle):**
> İçerik mimarisi (yazı, yazar, kategori, etiket, çoklu dil) baştan sizin yayın akışınıza göre kurulur. Headless API üzerinden hangi kanala isterseniz oradan içerik akar. Önyüzünüz hızlı bir Next.js sitesi olarak teslim edilir; editör paneli içerik ekibinizin günlük kullanımına uygun şekilde tasarlanır.

#### Bölüm 5 — Neler dahil?

**H2:** "Bu çözümde neler var?"

- ✓ Yazı, yazar, kategori, etiket içerik mimarisi
- ✓ Çok dilli içerik altyapısı
- ✓ Editör paneli (sayfa oluşturucu + yazı editörü)
- ✓ Medya kütüphanesi (görsel optimizasyonu otomatik)
- ✓ Hızlı Next.js önyüz (SEO ve Core Web Vitals odaklı)
- ✓ Headless REST API — bültene, mobile, partnere bağlamak için hazır
- ✓ Mail pazarlama entegrasyonu (opsiyonel)

#### Bölüm 6 — Tipik teslimat süresi

**H2:** "Ne kadar sürer?"

**Açıklama:**
> İçerik platformu projeleri kapsama göre **5–9 hafta** içinde canlıya alınır. Mevcut WordPress veya başka bir CMS'den içerik göçü varsa süre kapsamla artar.

#### Bölüm 7 — Sıkça sorulan sorular

| Soru | Cevap |
|---|---|
| WordPress içeriğimi taşıyabilir misiniz? | Evet. Yazı, yazar, kategori, etiket ve medya verisi yapılandırılarak Craftive içerik yapısına aktarılır. |
| SEO performansım korunur mu? | Evet. URL yapısı, başlık ve meta yönetimi, sitemap ve hızlı önyüz baştan SEO odaklı kurulur. Mevcut URL'leriniz yönlendirilebilir. |
| Yazarlar kendi yazılarını yönetebilir mi? | Evet. Yazar bazlı yetkilendirme ve düzenleme baştan tanımlanır. |
| Bültene veya mobile aynı içeriği nasıl akıtırım? | Tüm içerik headless API üzerinden sunulduğu için bülten, mobil uygulama veya başka bir kanal aynı kaynağı kullanır. |

#### Bölüm 8 — CTA

**H2:** "İçerik altyapınızı yenileyelim."

**CTA:** Demo Talep Et

---

### 7.7 Çözümler / Ajanslar için Delivery Platformu

| Alan | Değer |
|---|---|
| URL kararı | `/cozumler/ajanslar-icin` |
| Sayfa amacı | Dijital ajansları "kendi delivery hattını Craftive üzerine kurma" fikrine ikna etmek |
| Birincil hedef kitle | Ajans Sahibi / Delivery Lideri |
| Birincil hedef anahtar kelime | "ajans için dijital teslimat platformu" |
| İkincil hedef anahtar kelimeler | "multi-tenant ajans çözümü", "ajans white-label CMS", "müşteri projeleri için ortak altyapı" |
| Search intent | Commercial |

#### Hero

**H1:**
> Her müşteri için sıfırdan kurmaya elveda.

**Alt başlık:**
> Müşterilerinizi Craftive'ın aynı sağlam altyapısına alın. Her projeyi izole tutun; operasyonunuzu ölçekleyin, kârınızı koruyun.

**Birincil CTA:** Ortaklık Görüşmesi Talep Et
**İkincil CTA:** Diğer çözümlere bak

#### Bölüm 2 — Tipik ajans

**H2:** "Kim için?"

**Açıklama:**
> Aynı anda 3'ten fazla müşteri projesi yürüten, her birine sıfırdan altyapı kurmaktan yorulan dijital ajanslar. Geliştirici ekibi 5–30 kişi arasında; tekrar eden iş yükünü azaltıp marjını artırmak isteyen ekipler.

#### Bölüm 3 — Karşılaştığınız problem

**H2:** "Bu tanıdık geliyor mu?"

- Her müşteri projesinde aynı altyapıyı tekrar tekrar kuruyorsunuz.
- Geliştirici saatlerinin büyük kısmı "yeniden yazma" ile geçiyor; gerçek değer üretme zamanı az.
- Müşteri başka bir geliştiriciye geçtiğinde teslim etmek zor; her şey size özel.
- Müşteri sayısı arttıkça operasyon dağılıyor; tek bir sunucuda birden çok proje sürdürülmesi risk.

#### Bölüm 4 — Çözümümüz

**H2:** "Ortak temel, ayrı projeler."

**Açıklama (3–4 cümle):**
> Ajansınızı Craftive partneri olarak kabul ederiz. Her müşteriniz için izole bir tenant açılır; aynı altyapı, ayrı veritabanı. Tekrar eden işler bizde kalır, sizin ekibiniz markaya ve özelleştirmeye odaklanır. İsterseniz beyaz-etiket (white-label) modeliyle Craftive'ı arka planda tutabilirsiniz.

#### Bölüm 5 — Ajans için ne sağlıyoruz?

**H2:** "Ajansınız için neler?"

- ✓ Çok müşterili (multi-tenant) yönetim arayüzü
- ✓ Her müşteri için izole veritabanı — sıfır sızıntı riski
- ✓ Modüllerin müşteriye göre açılıp kapatılabilmesi
- ✓ Beyaz-etiket görünüm (opsiyonel)
- ✓ Tekrar eden işlerin (kurulum, deploy, güncelleme) ajansın üzerinden alınması
- ✓ Ortak destek hattı

#### Bölüm 6 — Ticari model

**H2:** "Birlikte nasıl çalışırız?"

**Açıklama (2–3 cümle):**
> Ticari yapı projeye veya müşteri portföyüne göre özel olarak konuşulur. Aylık taban + müşteri başına model, proje bazlı model veya partnerlik gelir paylaşımı seçenekleri masada. Detaylar keşif görüşmesinde netleşir.

#### Bölüm 7 — Sıkça sorulan sorular

| Soru | Cevap |
|---|---|
| Beyaz-etiket olarak sunabilir miyim? | Evet. Müşteri arayüzünde Craftive markası görünmez; sizin markanız altında teslim edersiniz. |
| Her müşteriye farklı modül açabilir miyim? | Evet. Modüller müşteri başına yapılandırılır. Bir müşteride sadece blog, diğerinde tüm e-ticaret olabilir. |
| Mevcut müşterilerimi taşıyabilir miyim? | Evet. Taşıma kapsamı keşif görüşmesinde netleştirilir; veri yapısı ve önyüz uyarlama sürelerine göre planlanır. |
| Geliştirici ekibim Craftive üzerinde geliştirme yapabilir mi? | Evet. Headless API ve frontend katmanı geliştirici ekibinizin geliştirmesine açıktır; biz altyapıyı, siz markaya özel kısmı yürütürsünüz. |

#### Bölüm 8 — CTA

**H2:** "Delivery hattınızı birlikte kuralım."

**CTA:** Ortaklık Görüşmesi Talep Et

---

### 7.8 Özellikler

| Alan | Değer |
|---|---|
| URL kararı | `/ozellikler` |
| Sayfa amacı | Craftive'ın "ne içerdiğini" tek sayfada görme ihtiyacı; teknik karar verici için derin sayfa |
| Birincil hedef kitle | CTO, Teknik Karar Verici, Geliştirici |
| Birincil hedef anahtar kelime | "modüler CMS özellikleri" |
| İkincil hedef anahtar kelimeler | "multi-tenant platform özellikleri", "headless platform yetenekleri", "page builder Türkiye" |
| Search intent | Informational / commercial |

#### Hero

**H1:**
> Modüler. Ölçeklenebilir. Sizin için yönetilen.

**Alt başlık:**
> Craftive'ı oluşturan yedi temel yetenek. İhtiyacınıza göre açılır, projeniz büyüdükçe ekleyebilirsiniz.

**Birincil CTA:** Demo Talep Et
**İkincil CTA:** Geliştirici dokümantasyonuna gör (docs.craftive.io)

#### Bölüm 2 — 7 yetenek kartı

**H2:** "Yedi temel yetenek."

Her yetenek için: başlık + 2–3 cümle açıklama + opsiyonel "öne çıkan detay" bullet.

##### 1. Modüler mimari

> Sayfa oluşturucu, medya yönetimi, ürün katalogu, mail pazarlama — projenizin ihtiyaç duyduğu yetenekler açık, gerekmeyenler kapalı. Canlıya çıktıktan sonra da yeni modüller eklenebilir; mimari değişmez, sadece üzerine eklenir.

- Açıp kapatılabilir modül yapısı
- Post-launch genişletme
- Tek altyapı, sayısız konfigürasyon

##### 2. Üç katmanlı içerik motoru

> İçerik mimarisi üç katmana ayrılır: Sayfa Şablonu (PageTemplate), Sayfa Slotları (PageSlot) ve İçerik Bileşenleri (CmsComponent). Bu yapı sayesinde aynı bileşen birden çok sayfada yeniden kullanılır; sayfa düzeni içerikten bağımsız değiştirilebilir.

- Yeniden kullanılabilir bileşenler
- Sayfa düzeni ile içerik ayrımı
- Tekrara düşmeden büyüme

##### 3. İzole veritabanı (database-per-tenant)

> Her müşteri için ayrı bir veritabanı kullanılır. Bir müşterinin verisi başka bir müşteriye sızamaz. KVKK / GDPR uyumlu çalışır; yedekleme, geri yükleme ve veri taşıma müşteri bazında yapılır.

- Sıfır veri sızıntısı riski
- KVKK / GDPR uyumu
- Bağımsız yedekleme

##### 4. Multi-tenant altyapı

> Tek bir Craftive kurulumu yüzlerce müşteriyi destekler. Her müşterinin kendi subdomain'i, kendi ayarları, kendi veritabanı vardır. Operasyon ölçeklenir, maliyet düşer, güncellemeler aynı anda herkese ulaşır.

- Yüzlerce kiracı, tek altyapı
- Müşteri başına bağımsız ayar
- Eşzamanlı güvenlik güncellemeleri

##### 5. Headless REST API

> Tüm içerik, ürün, kullanıcı verisi açık REST API üzerinden sunulur. JSON tabanlı, Next.js / Angular / React Native ile uyumlu. Önyüzünüzü istediğiniz teknolojiyle yazabilir, kanal sayısını sınırsızca artırabilirsiniz.

- JSON tabanlı, standart REST
- Çoklu önyüz desteği
- Vendor lock-in yok

##### 6. Uçtan uca yönetilen teslimat

> Kurulum, içerik mimarisi tasarımı, frontend geliştirme, staging, production deploy — Craftive ekibi yürütür. Canlıya alındıktan sonra destek ve büyüme süreçlerinde eşlik eder.

- Tek nokta sorumluluk
- DevOps yükü sizde değil
- Post-launch destek hattı

##### 7. Canlı sonrası büyüme

> Site yayına girdikten sonra durmaz. Yeni modüller, yeni kanallar, yeni ürün dikeyleri sonradan eklenebilir. Yeniden yazmak gerekmez; mevcut altyapının üzerine eklenir.

- Modül sonradan açılır
- Yeni kanallar bağlanabilir
- Sıfırdan başlamaya gerek yok

#### Bölüm 3 — "Sadece bir araç değil" kutusu

**H2:** "Bu sadece bir CMS değil."

**Açıklama (3–4 cümle):**
> Çoğu headless CMS size bir kontrol paneli verir; kurulum, deploy ve bakım sizdedir. Craftive farklıdır: kontrol paneli de var, ama altındaki tüm süreç — kurulum, içerik mimarisi, frontend, deploy, destek — bizim sorumluluğumuzda. Yani araç değil, teslimat ortağı.

#### Bölüm 4 — Teknoloji şeffaflığı

**H2:** "Altyapımız nelerden oluşur?"

Geliştirici güveni için açıkça paylaşılır:
- **Backend:** Spring Boot (Java)
- **Önyüz:** Next.js + React (alternatif: Angular, React Native)
- **Veritabanı:** MySQL — müşteri başına izole
- **Altyapı:** Docker, Cloudflare, DigitalOcean
- **Dokümantasyon:** docs.craftive.io (API referansı dahil)

#### Bölüm 5 — CTA

**H2:** "Hangi modüller sizin için?"

**Açıklama:** 30 dakikalık keşif görüşmesi, sizin için en uygun yapılandırmayı netleştirir.

**CTA:** Demo Talep Et

---

### 7.9 Kullanım Alanları

| Alan | Değer |
|---|---|
| URL kararı | `/kullanim-alanlari` |
| Sayfa amacı | Senaryo/vaka odaklı anlatımla "biz bu işi yapanız" demek; üst funnel ziyaretçiyi yakalamak |
| Birincil hedef kitle | Araştırma aşamasındaki tüm personalar |
| Birincil hedef anahtar kelime | "dijital ürün geliştirme örnekleri" |
| İkincil hedef anahtar kelimeler | "kurumsal site case study", "e-ticaret platform vaka analizi", "İK portalı örnekleri" |
| Search intent | Informational |

#### Hero

**H1:**
> Bir fikir, bir mimari, sayısız sonuç.

**Alt başlık:**
> Craftive'ın aynı modüler altyapısı; bir markanın kurumsal sitesi, bir İK ekibinin portalı, bir yayıncının içerik platformu olabilir. Vakalardan örneklerle.

**Brand tag:** *one platform, every configuration.*

#### Bölüm 2 — Senaryo seçici (3–5 senaryo)

**H2:** "Hangi senaryo size benziyor?"

Her senaryo kartı: senaryo başlığı + 2 cümle özet + "Çözümünü gör" link.

| Senaryo başlığı | Özet |
|---|---|
| "Yeni bir alt marka çıkarıyoruz" | Şirket içinde yeni bir alt marka veya pazar açılışı; yeni site, yeni içerik, yeni dil. Aynı altyapı, yeni bir tenant. |
| "Eski WordPress'ten geçiyoruz" | Yıllar içinde dağılmış bir kurumsal site veya yayıncı. İçerik göçü + yeni altyapı + SEO korumalı geçiş. |
| "E-ticaretimizi headless'a taşıyoruz" | Tema sınırından sıkılmış bir e-ticaret operasyonu. Aynı katalog web, mobil ve pazaryeri için. |
| "Çalışanlarımız için kurum içi portal" | İç iletişim dağınık. Duyuru, belge, form tek panelde; izole, KVKK uyumlu. |
| "Ajans olarak müşterilerimizi tek altyapıya alıyoruz" | Her projeyi sıfırdan kurmaktan yorulmuş ajans. Multi-tenant + beyaz-etiket. |

#### Bölüm 3 — Vaka anlatım şablonu

Her vaka için sayfa içinde mini-bölüm. Yapı:

1. **Müşteri tipi & sektör** (anonimleştirilebilir)
2. **Başlangıç durumu** — proje öncesi neye benziyordu?
3. **Süreç** — Craftive ile birlikte hangi adımlar atıldı?
4. **Sonuç** — ölçülebilir veya niteliksel kazanım
5. **Devamı** — canlıya çıktıktan sonra nasıl büyüdü?

> İlk lansman için en az 2 vaka anonimleştirilmiş şekilde hazırlanmalı. Yoksa "tipik bir [senaryo] nasıl olur" başlığıyla kurgusal-ama-gerçekçi senaryolar yazılır.

#### Bölüm 4 — CTA

**H2:** "Sizin senaryonuz ne?"

**Açıklama:** Vaka tablosunda kendinizi tanıdıysanız, 30 dakikalık görüşmede konuyu derinleştirelim.

**CTA:** Projenizi Anlatın

---

### 7.10 Fiyatlandırma

| Alan | Değer |
|---|---|
| URL kararı | `/fiyatlandirma` |
| Sayfa amacı | "Fiyat ne kadar?" sorusuna güven veren bir cevap; ziyaretçiyi rahatsız etmeden teklif sürecine taşımak |
| Birincil hedef kitle | Tüm personalar — özellikle bütçe sahibi |
| Birincil hedef anahtar kelime | "Craftive fiyatlandırma" |
| İkincil hedef anahtar kelimeler | "dijital ürün teslimat fiyatı", "headless CMS proje maliyeti", "kurumsal site geliştirme bütçesi" |
| Search intent | Transactional |

#### Hero

**H1:**
> Projenize göre fiyat, kapsamınıza göre paket.

**Alt başlık:**
> Craftive sabit paketli bir abonelik değildir. Her teslimat; ihtiyaç duyduğunuz modüllere, içerik karmaşıklığınıza ve özelleştirme kapsamınıza göre fiyatlanır.

**Birincil CTA:** Görüşme Talep Et
**İkincil CTA:** Önce çözümleri inceleyin

#### Bölüm 2 — Fiyatın bileşenleri

**H2:** "Fiyat neye göre belirlenir?"

5 bileşenli açıklayıcı blok:

| Bileşen | Açıklama |
|---|---|
| 🧩 Modüller | Hangi yetenekleri kullanacaksınız? Sayfa oluşturucu mu, ürün katalogu mu, mail pazarlama mı? |
| 📐 İçerik & tenant karmaşıklığı | Kaç dil, kaç sayfa türü, kaç bileşen? Tek tenant mı, çok tenant mı? |
| 🎨 Önyüz / tema uyarlaması | Hazır referans tema mı, marka rehberinize özel tasarım mı? |
| 🔌 Entegrasyon | Mevcut sistemlere (ERP, ödeme, CRM, SSO) bağlantı var mı? |
| 🛠️ Destek seviyesi | Canlı sonrası ne kadar destek istiyorsunuz? |

#### Bölüm 3 — Tipik aralıklar (opsiyonel — kullanıcı kararına göre dahil edilir)

> Not: Bu blok "Açık sorular" bölümünde tartışılmaktadır. Eğer yayınlanırsa şu yapıda olur:

**H2:** "Tipik bütçe aralıkları"

| Proje tipi | Tipik başlangıç bütçesi |
|---|---|
| Kurumsal web sitesi (tek dilli, sade) | 5.000 – 12.000 USD |
| Kurumsal site (çok dilli, geniş kapsam) | 12.000 – 25.000 USD |
| İK portalı | 10.000 – 20.000 USD |
| İçerik platformu / yayıncı | 8.000 – 20.000 USD |
| E-ticaret / headless storefront | 15.000 – 50.000 USD |
| Ajans partnerliği | Aylık taban + müşteri başına |

> ⚠️ Bu sayılar bağlayıcı değildir, görüşmeye gelmeden önce büyüklük fikri vermek içindir.

#### Bölüm 4 — Süreç şeffaflığı

**H2:** "Görüşmeden teslime, fiyat süreci."

| Adım | Ne olur? |
|---|---|
| 1. Keşif görüşmesi | Fikrinizi ve kapsamınızı dinleriz. (Ücretsiz, 30 dakika) |
| 2. Önerilen yapılandırma | Hangi modüller, hangi kapsam, hangi takvim — yazılı sunum. |
| 3. Teklif | Sabit ücret + opsiyonel destek modeli. |
| 4. Sözleşme & başlangıç | Onay sonrası ilk hafta kurulum başlar. |
| 5. Teslimat & devamı | Canlıya alım + isteğe bağlı destek anlaşması. |

#### Bölüm 5 — SSS

**H2:** "Sıkça sorulan sorular"

| Soru | Cevap |
|---|---|
| Neden sabit paket fiyatı yok? | Çünkü Craftive sabit bir ürün değil, projeye özel bir teslimattır. Her müşterinin ihtiyacı farklı; tek bir fiyat herkese adil olmaz. |
| Aylık ücret var mı? | İsteğe bağlı destek/operasyon anlaşması olabilir. Zorunlu bir aylık abonelik yoktur. |
| Sonradan ek ücret çıkar mı? | Kapsam dışı yeni istekler için ek teklif sunulur. Mevcut kapsam içinde sürpriz fatura olmaz. |
| Ödemeyi nasıl yapıyoruz? | Tipik olarak başlangıç ödemesi + ara taksitler + canlıya alım ödemesi. Ayrıntı sözleşmede netleşir. |

#### Bölüm 6 — CTA

**H2:** "Projenize özel teklif alın."

**Açıklama:** 30 dakikalık keşif görüşmesi, yazılı bir teklif ile sonuçlanır.

**CTA:** Görüşme Talep Et

---

### 7.11 Blog

| Alan | Değer |
|---|---|
| URL kararı | `/blog` |
| Sayfa amacı | Otorite, SEO, lead nurture; ziyaretçiye değer veren içeriklerle markanın güvenilirliğini kanıtlamak |
| Birincil hedef kitle | Araştırma aşamasındaki tüm personalar + organik arama trafiği |
| Birincil hedef anahtar kelime | "Craftive blog" |
| İkincil hedef anahtar kelimeler | "headless CMS rehber", "multi-tenant mimari", "dijital ürün teslimatı" |
| Search intent | Informational |

#### Hero (blog listeleme sayfası üstü)

**H1:**
> Mimari, içerik, teslimat üzerine notlar.

**Alt başlık:**
> Dijital ürün geliştirme, headless mimari, multi-tenant operasyon ve içerik yönetimi üzerine birinci elden deneyimler ve teknik analizler.

**Birincil CTA (sağ üst veya hero altı):** Yeni yazıları kaçırma — Aboneliğe katıl

#### Bölüm 2 — Konu kategorileri

**H2:** "Konular"

3 ana kategori (filtre etiketi olarak da kullanılır):
- **Mimari & Altyapı** — headless CMS, multi-tenant, izole veritabanı, deploy
- **İçerik & Operasyon** — içerik mimarisi, çoklu dil, editör akışı, SEO
- **Teslimat & Süreç** — proje yönetimi, ajans operasyonu, müşteri ile çalışma

#### Bölüm 3 — Yazı listesi

Her yazı kartı için minimum içerik:
- Kapak görseli (opsiyonel)
- Yazı başlığı (H3)
- 1–2 cümle özet
- Yazar adı + yayın tarihi
- Kategori etiketi
- Okuma süresi (örn: 6 dakika)

#### Bölüm 4 — Newsletter aboneliği

**H2:** "Yeni yazıları kaçırma."

**Açıklama (1 cümle):**
> Ayda 1–2 e-posta. Spam yok, sadece yeni yazı bildirimleri ve seçilmiş Craftive notları.

**Form alanı etiketi:** E-posta adresiniz
**Buton:** Aboneliğe katıl

#### Bölüm 5 — Footer öncesi CTA

**H2:** "Konuşmak ister misiniz?"

**Açıklama:** Yazılarda gördüğünüz mimari veya süreç sizin probleminize denk düşüyorsa, 30 dakikalık keşif görüşmesinde derinleşelim.

**CTA:** Demo Talep Et

#### Tek yazı (post) sayfası şablonu

Her blog yazısı için sabit yapı:
- Yazı başlığı (H1)
- Yazar bilgisi + yayın tarihi + okuma süresi
- Özet (lead paragraph, 2–3 cümle)
- İçerik
- Sayfa altında "İlgili yazılar" (3 başlık)
- Sayfa altında newsletter abonelik kutusu
- Sayfa altında "Bu konuyu konuşalım" CTA (Demo Talep Et linki)

---

### 7.12 Hakkımızda

| Alan | Değer |
|---|---|
| URL kararı | `/hakkimizda` |
| Sayfa amacı | Marka hikayesi + güven sinyalleri; "kim bu insanlar?" sorusuna güçlü bir cevap |
| Birincil hedef kitle | Karar verici (Kurumsal CTO, Ajans Sahibi) — kontrol sayfası olarak ziyaret eder |
| Birincil hedef anahtar kelime | "Craftive hakkında" |
| İkincil hedef anahtar kelimeler | "Craftive ekibi", "Craftive nedir", "modüler platform geliştirici" |
| Search intent | Informational / branded |

#### Hero

**H1:**
> Tek bir platform, sayısız konfigürasyon.

**Brand tag (alt başlık):**
> *one platform, every configuration.*

**Alt başlık (TR):**
> Craftive; her dijital projeye sıfırdan başlamayı reddeden bir fikirden doğdu. Aynı sağlam altyapıyı, her ihtiyaca göre yeniden yapılandırmak için kurduk.

#### Bölüm 2 — Manifesto / inanışımız

**H2:** "Neye inanıyoruz?"

3–4 paragraflık manifesto:

> Bir dijital ürünü baştan başa kurmak; sunucu, veritabanı, deploy hattı, içerik mimarisi, frontend... bunların hepsi her projede yeniden çözülmesi gereken sorunlar olarak görüldüğünde, ekipler asıl üretmeleri gereken şeye — projenin **kendine özgü** kısmına — zaman ayıramıyor.

> Bizim cevabımız: paylaşılan, modüler bir temel. Her projeye sıfırdan başlamak yerine, sağlam ve test edilmiş bir altyapının üzerine her müşterinin kendi yapılandırmasını kurarız.

> Bu yaklaşım, ortada bırakan bir aracın değil; uçtan uca eşlik eden bir teslimat ortağının güvencesini sunar. Çünkü deneyimle biliyoruz: çoğu işletmenin ihtiyacı bir kontrol paneli değil, doğru kurulmuş bir dijital ürün.

> Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim. Bu cümle bizim için sadece bir slogan değil; çalışma biçimimiz.

#### Bölüm 3 — Nasıl çalışırız (süreç şeffaflığı)

**H2:** "Bir proje bizimle nasıl yürür?"

4 adım kart yapısı:

| Adım | Başlık | Açıklama |
|---|---|---|
| 01 | Dinleriz | Fikrinizi, sektörünüzü, hedef kitlenizi ve kısıtlarınızı anlamak için keşif görüşmesi yaparız. |
| 02 | Önerir | Hangi modüller, hangi kapsam ve hangi takvim — yazılı bir sunumla netleştiririz. |
| 03 | İnşa ederiz | İzole ortamınız hazırlanır, içerik mimarisi kurulur, önyüz geliştirilir, staging'e alınır. |
| 04 | Eşlik ederiz | Canlıya çıktıktan sonra destek, büyüme ve yeni modül ihtiyaçlarında yanınızda kalırız. |

#### Bölüm 4 — Neden farklıyız (managed delivery)

**H2:** "Bir araç değil, bir ortak."

**Açıklama (3–4 cümle):**
> Headless CMS pazarındaki birçok araç size kontrol paneli verir; ondan sonrası size kalır. Craftive farklıdır: kontrol paneli de var, ama altındaki süreci — kurulum, içerik mimarisi, frontend, deploy, post-launch destek — sizin yerinize biz yürütürüz. Yani CMS değil, CMS dahil bir teslimat ortağı.

#### Bölüm 5 — Ekip (opsiyonel — kullanıcı kararına göre)

**H2:** "Ekip"

> İçeriği netleştirmek için "Açık sorular" bölümüne bakın. Yayınlanırsa yapı:

- Kurucu / ekip üyeleri için: fotoğraf, isim, rol, 1 cümlelik bio
- LinkedIn link
- "Bizimle çalışmak ister misiniz?" mini-link (kariyer sayfası varsa)

#### Bölüm 6 — Teknolojiler & ortaklar

**H2:** "Üzerinde yükseldiğimiz teknolojiler"

Logo bandı + kısa açıklama:
- Spring Boot, Next.js, MySQL, Docker, Cloudflare, DigitalOcean
- "Açık standartlara dayalı, vendor lock-in'siz mimari."

#### Bölüm 7 — Footer öncesi CTA

**H2:** "Bizi tanıyın, projenizi anlatın."

**CTA:** Demo Talep Et

---

### 7.13 İletişim

| Alan | Değer |
|---|---|
| URL kararı | `/iletisim` |
| Sayfa amacı | Demo / discovery call talebini almak; ziyaretçinin formu doldurma engelini en aza indirmek |
| Birincil hedef kitle | Karar vermiş veya görüşmeye hazır tüm personalar |
| Birincil hedef anahtar kelime | "Craftive iletişim" |
| İkincil hedef anahtar kelimeler | "Craftive demo talep", "dijital proje görüşme" |
| Search intent | Transactional |

#### Hero

**H1:**
> Anlatın, eşlik edelim.

**Alt başlık:**
> 30 dakikalık keşif görüşmesi. Fikrinizi dinler, size uygun çözümü ve takvimi netleştiririz. Görüşme ücretsizdir, taahhüt gerektirmez.

#### Bölüm 2 — Form

**H2 (form üstü):** "Birkaç soruya cevaplayın, size dönelim."

**Form alanları (etiket + yardımcı metin):**

| Alan | Etiket | Yardımcı metin / placeholder |
|---|---|---|
| İsim | Adınız Soyadınız | – |
| E-posta | E-posta adresiniz | İş e-postanız idealdir |
| Telefon (opsiyonel) | Telefon (isteğe bağlı) | Daha hızlı dönüş için |
| Şirket / proje | Şirketiniz veya projeniz | "Yeni bir kurumsal site", "ABC Mağaza" gibi |
| Sektör (opsiyonel, dropdown) | Sektörünüz | Kurumsal / E-ticaret / İK / Yayıncı / Ajans / Diğer |
| Bütçe aralığı (opsiyonel, dropdown) | Yaklaşık bütçe aralığı | Henüz belirsiz / 5–15K USD / 15–30K USD / 30K+ |
| Mesaj | Fikrinizi birkaç cümleyle anlatın | "Yeni bir kurumsal site planlıyoruz, 5 dilli olacak..." |
| Bizi nereden duydunuz? (opsiyonel) | Bizi nasıl buldunuz? | LinkedIn / Tavsiye / Google / Diğer |

**Buton metni:** Görüşme Talebi Gönder

**KVKK / aydınlatma metni (form altı, küçük):**
> Verileriniz sadece görüşme talebiniz için kullanılır; KVKK kapsamında işlenir, üçüncü taraflarla paylaşılmaz.

#### Bölüm 3 — Form gönderimi sonrası mesaj

**Başarı durumu mesajı:**
> Teşekkürler! Talebinizi aldık. En geç 1 iş günü içinde size dönüş yapacağız.

**Alt aksiyon (opsiyonel):**
> Bu sırada Craftive'ı daha yakından tanımak isterseniz: → Özellikler

#### Bölüm 4 — Alternatif iletişim kanalları

**H2:** "Form yerine doğrudan ulaşmak isterseniz"

- E-posta: hello@craftive.io (örnek — gerçek adres)
- LinkedIn: linkedin.com/company/craftive (örnek)
- Dokümantasyon: docs.craftive.io

#### Bölüm 5 — Güven satırı

**H2:** "Görüşme nasıl geçer?"

Mini liste:
- Yaklaşık 30 dakika, online (Google Meet / Zoom)
- Satış sunumu değil; ihtiyacınızı anlama görüşmesi
- Sonunda yazılı bir özet + sonraki adım önerisi alırsınız
- Taahhüt gerektirmez; uygun değilse "şu an değil" demek serbest

---

## 8. Use Case Narrative Blueprint'leri

> **Bu bölümün amacı:** Bölüm 7.3–7.7'deki Çözümler alt sayfaları "ürün dikeyi" anlatımı yapar (biz **ne sunuyoruz**). Bu bölüm ise her dikey için **hikâye/senaryo** anlatımı sunar (müşteri **ne yaşıyor**). Buradaki blueprint'ler hem Kullanım Alanları (7.9) sayfasında, hem blog yazılarında, hem LinkedIn / sales deck / case study üretiminde tekrar tekrar kullanılır.

### 8.1 Kurumsal Web Siteleri

| Bileşen | İçerik |
|---|---|
| Tipik müşteri profili | 50–500 kişilik şirket, yeni site kuracak veya eski WordPress'i değiştirecek. Marka itibarı yüksek öncelikte. |
| Tipik tetikleyici | Marka yenileme, yeni alt marka açılışı, eski sistemde IT bağımlılığının dayanılmaz olması |
| Karşılaştığı problem (hikâye) | Pazarlama ekibi her küçük güncelleme için IT'yi bekliyor. Çok dilli içerik kontrolden çıktı. Yeni alt marka çıkıyor ama bir site daha kurmak istemiyorlar. |
| Craftive'ın çözüm anlatımı | İzole bir ortam kurulur. Sayfa şablonları ve marka rehberinize özel bileşenler hazırlanır. İçerik ekibi tek başına yönetir; IT kuyruğa girmeyi unutur. Yeni alt marka için aynı altyapıda yeni tenant açılır — günler içinde canlıda. |
| Hangi yetenekler kullanılır (kullanıcı diliyle) | Sayfa oluşturucu • Medya kütüphanesi • Çok dilli içerik • Marka bileşen kütüphanesi |
| Tipik teslimat süresi | 4–8 hafta |
| Sonraki adım cümlesi (CTA bağlama) | "Markanızın sitesi de aynı kapsamda mı? Keşif görüşmesinde detaylandıralım." |
| Yan-kullanım | Blog yazısı: "Pazarlama ekibinin sitesi neden hâlâ IT'ye bağımlı?" |

### 8.2 Headless E-ticaret / Storefront

| Bileşen | İçerik |
|---|---|
| Tipik müşteri profili | 100–100.000 ürünlü marka, Shopify temasının dışına çıkmak isteyen veya çok kanallı (web + mobil + pazaryeri) satışa geçen markalar. |
| Tipik tetikleyici | Yeni pazara açılma, mobil uygulama planı, marka deneyiminin tema sınırını aşma ihtiyacı |
| Karşılaştığı problem (hikâye) | Tema her özelleştirme için yan iş gibi. Web, mobil ve pazaryeri ayrı sistemlerden besleniyor; envanter ve müşteri verisi tutarsız. Marka kontrolünü tamamen kazanmak istiyorlar. |
| Craftive'ın çözüm anlatımı | Ürün katalogu, içerik ve müşteri verisi Craftive arkayüzünde tutulur, headless REST API üzerinden açılır. Next.js storefront teslim edilir. İsterse mobile uygulamayı aynı API'ye bağlar. |
| Hangi yetenekler kullanılır | Ürün katalogu • Çoklu para birimi & dil • Sayfa oluşturucu (kampanya/landing) • Medya kütüphanesi • Headless REST API |
| Tipik teslimat süresi | 6–12 hafta |
| Sonraki adım cümlesi | "Mağazanızı baştan baştan kuralım — kapsamı 30 dakikalık görüşmede netleştirelim." |
| Yan-kullanım | Sales deck: "Tema'dan headless'a geçişin gerçek maliyeti" / LinkedIn: "Aynı katalog, üç kanal" |

### 8.3 İK Portalları & Kurum İçi Siteler

| Bileşen | İçerik |
|---|---|
| Tipik müşteri profili | 100–2.000 çalışanlı kurum. Veri güvenliği ve uyumluluk öncelikli; İK ekibi iç iletişimi tek noktaya toplamak istiyor. |
| Tipik tetikleyici | İç iletişim aracı arayışı, kurum büyürken iletişimin dağılması, KVKK uyumlu bulut depolama ihtiyacı |
| Karşılaştığı problem (hikâye) | Duyurular SharePoint, e-posta, WhatsApp arasında kayboluyor. Belgeler ortak sürücüde — arama imkânsız. Formlar hâlâ Excel. İK ekibi tek bir noktada toplamak istiyor; ama bulut depolama veri açısından rahatsız. |
| Craftive'ın çözüm anlatımı | İzole bir ortam kurulur. Yetki bazlı erişim, duyuru akışı, departman bazlı içerik düzenleme, belge & form merkezi. Veriler izole bir veritabanında, müşteriye ait. SSO ile şirket kimlik sisteminize bağlanır. |
| Hangi yetenekler kullanılır | Sayfa oluşturucu • Medya kütüphanesi • Çok dilli içerik • Rol/yetki yönetimi • İzole veritabanı |
| Tipik teslimat süresi | 6–10 hafta |
| Sonraki adım cümlesi | "Çalışan portalınızı nasıl kuracağımızı keşif görüşmesinde konuşalım." |
| Yan-kullanım | Blog: "KVKK uyumlu iç iletişim platformu nasıl kurulur?" |

### 8.4 Blog & İçerik Platformları

| Bileşen | İçerik |
|---|---|
| Tipik müşteri profili | Düzenli içerik yayınlayan kurumsal pazarlama ekibi, online medya kuruluşu veya niş yayıncı. Birden çok kanala içerik dağıtmak isteyen ekipler. |
| Tipik tetikleyici | WordPress'in performans/yönetim sınırı, çok dilli içerik karmaşası, bültene-mobile-partnere içerik dağıtma ihtiyacı |
| Karşılaştığı problem (hikâye) | WordPress yavaşladı, eklenti karmaşası içerik yönetimini zorlaştırdı. Aynı içeriği web'e, bültene ve mobile elle kopyalıyorlar. Çok dilli içerik düzensiz. Geliştirici olmadan tema değiştiremiyorlar. |
| Craftive'ın çözüm anlatımı | İçerik mimarisi (yazı, yazar, kategori, etiket, çoklu dil) yayın akışınıza göre kurulur. Headless API ile hangi kanal isterse oradan akar. Hızlı Next.js önyüz teslim edilir — SEO ve Core Web Vitals odaklı. |
| Hangi yetenekler kullanılır | Sayfa oluşturucu • Yazı editörü • Çok dilli içerik • Medya kütüphanesi (otomatik boyutlandırma) • Headless REST API |
| Tipik teslimat süresi | 5–9 hafta |
| Sonraki adım cümlesi | "İçerik altyapınızı yenileyelim — 30 dakikada büyük resmi paylaşalım." |
| Yan-kullanım | Blog yazısı: "WordPress'ten headless'a geçişin teknik checklisti" |

### 8.5 Ajanslar için Reusable Delivery Foundation

| Bileşen | İçerik |
|---|---|
| Tipik müşteri profili | 10–50 kişilik dijital ajans. Aynı anda 3+ proje yürüten, operasyon dağılan ekipler. |
| Tipik tetikleyici | Tekrar eden iş yükünün maliyeti, müşteri sayısı arttıkça operasyonun dağılması, beyaz-etiket satış fikri |
| Karşılaştığı problem (hikâye) | Her müşteri için sıfırdan altyapı kurmak ekibi yoruyor. Geliştirici saatlerinin büyük kısmı "yeniden yazma" ile geçiyor. Müşteri başka geliştiriciye geçtiğinde teslim etmek zor. |
| Craftive'ın çözüm anlatımı | Ajans, Craftive partneri olur. Her müşteri için izole tenant açılır. Tekrar eden işler Craftive tarafında kalır; ajansın ekibi markaya ve özelleştirmeye odaklanır. İsterse beyaz-etiket olarak sunar. |
| Hangi yetenekler kullanılır | Multi-tenant yönetimi • İzole veritabanı • Modül başına yapılandırma • Beyaz-etiket (opsiyonel) |
| Tipik teslimat süresi | Yapılandırma 2–4 hafta (sonrası müşteri projeleri normal sürede) |
| Sonraki adım cümlesi | "Delivery hattınızı birlikte kuralım — partnerlik görüşmesi planlayalım." |
| Yan-kullanım | LinkedIn carousel: "Ajansta her projeye sıfırdan başlamak neden marjı eritiyor?" |

---

## 9. Blog İçerik Stratejisi

### Topic cluster yaklaşımı

3 ana pillar (büyük rehber yazı) + her birinin altında 6–7 destekleyici yazı. Toplam ~21 başlık.

#### Pillar 1 — Headless CMS & Modüler Mimariler (commercial intent)

> **Pillar yazı:** "Türkiye'de Headless CMS Rehberi: Ne Zaman Gerekli, Ne Zaman Aşırı?"

Destekleyici yazılar:

| # | Başlık | 1 cümle açıklama | Hedef anahtar kelime |
|---|---|---|---|
| 1 | Headless CMS Nedir, Klasik CMS'den Farkı Ne? | Tanım, mimari, ne zaman tercih edilir | "headless CMS nedir" |
| 2 | Headless CMS Karşılaştırması: Hangi Aracın Hangi Probleme Çözüm? | Strapi, Contentful, Sanity, Craftive farkı | "headless CMS karşılaştırma" |
| 3 | Self-Serve CMS vs Managed Delivery: Hangi Modeli Seçmeli? | Sahip olduğunuz ekibe göre seçim rehberi | "managed CMS Türkiye" |
| 4 | WordPress'ten Headless'a Geçiş Checklisti | Adım adım göç planı, SEO koruma | "WordPress'ten headless'a geçiş" |
| 5 | Modüler Mimari: Bir Platformu Esnek Tutmanın Bedeli ve Kazanımı | Mimari tartışma yazısı | "modüler yazılım mimarisi" |
| 6 | Headless E-ticaret Nedir, Ne Zaman Anlamlı? | Headless commerce için karar rehberi | "headless e-ticaret" |

#### Pillar 2 — Multi-Tenant & GDPR / Güvenlik (trust / authority)

> **Pillar yazı:** "Multi-Tenant Mimari: Birden Çok Müşteriyi Aynı Sistemde Güvenle Yürütmek"

Destekleyici yazılar:

| # | Başlık | 1 cümle açıklama | Hedef anahtar kelime |
|---|---|---|---|
| 7 | Database-Per-Tenant Modeli: Avantajları ve Maliyetleri | Mimari karar yazısı | "database per tenant" |
| 8 | KVKK ile Uyumlu Veri İzolasyonu Nasıl Sağlanır? | Yasal + teknik kombinasyonu | "KVKK uyumlu yazılım" |
| 9 | GDPR ile Çalışan Çok Kiracılı Sistemlerin 7 İlkesi | Pratik checklist | "GDPR multi-tenant" |
| 10 | Tenant İzolasyonu Bozulduğunda Ne Olur? | İncelenmiş gerçek senaryolar | "tenant izolasyonu" |
| 11 | İç İletişim Portalı İçin Veri Güvenliği Önlemleri | İK ekibine yönelik rehber | "kurum içi portal güvenliği" |
| 12 | Vendor Lock-In'den Kaçınmanın 5 Yolu | Geliştirici güveni odaklı | "vendor lock-in nasıl önlenir" |

#### Pillar 3 — Dijital Ürün Teslimatı & Süreç (top-funnel)

> **Pillar yazı:** "Bir Dijital Ürünün İlk Lansmanına Kadar 4 Aşama"

Destekleyici yazılar:

| # | Başlık | 1 cümle açıklama | Hedef anahtar kelime |
|---|---|---|---|
| 13 | Kurumsal Web Sitesi Projeleri Neden Bütçe Aşar? | Süreç hataları + çözümler | "kurumsal site maliyeti" |
| 14 | Geliştirici Tutmak vs Hazır Platform: Gerçek Karşılaştırma | İçe yakın bir mali değerlendirme | "geliştirici tutmak mı platform kullanmak mı" |
| 15 | İçerik Mimarisi Tasarlamak: Sayfa Şablonu, Slot, Bileşen | Editör/geliştirici ortak rehberi | "içerik mimarisi nasıl tasarlanır" |
| 16 | Ajansta Tekrar Eden İşi Tanımak ve Otomatikleştirmek | Ajans operasyonu için | "ajans operasyon verimliliği" |
| 17 | E-ticaret Lansmanı: 10 Hafta İçin Gerçekçi Bir Yol Haritası | Adım adım çalışma planı | "e-ticaret lansman süreci" |
| 18 | "Hızlı Geliştirme" ile "Doğru Geliştirme"yi Aynı Anda Yapmak | Süreç felsefesi yazısı | "MVP geliştirme süreci" |
| 19 | Beyaz-Etiket Modelde Müşteri Memnuniyeti Nasıl Korunur? | Ajans-müşteri dinamiği | "beyaz etiket dijital hizmet" |

### İlk 90 günlük yayın takvimi

Haftalık 1 yazı temposu. Pillar'lar dönüşümlü, destekleyici yazılar arada.

| Hafta | Yazı | Tip |
|---|---|---|
| 1 | Pillar 1: Türkiye'de Headless CMS Rehberi | Pillar |
| 2 | #1 Headless CMS Nedir | Destek |
| 3 | #14 Geliştirici Tutmak vs Hazır Platform | Destek |
| 4 | Pillar 3: 4 Aşama | Pillar |
| 5 | #13 Kurumsal Web Sitesi Projeleri Neden Bütçe Aşar | Destek |
| 6 | #2 Headless CMS Karşılaştırması | Destek |
| 7 | #4 WordPress'ten Headless'a Geçiş Checklisti | Destek |
| 8 | Pillar 2: Multi-Tenant Mimari | Pillar |
| 9 | #8 KVKK ile Uyumlu Veri İzolasyonu | Destek |
| 10 | #3 Self-Serve vs Managed Delivery | Destek |
| 11 | #15 İçerik Mimarisi Tasarlamak | Destek |
| 12 | #11 İç İletişim Portalı İçin Veri Güvenliği | Destek |
| 13 | İlk vaka analizi (anonim case study) | Vaka |

### Yazı uzunluk ve format önerileri

| Yazı tipi | Hedef uzunluk | Yapı |
|---|---|---|
| Pillar yazı | 2.500–4.000 kelime | Kapak görseli + içindekiler + 5–8 ana bölüm + sonuç + CTA |
| Destekleyici yazı | 1.200–2.000 kelime | Giriş + 3–5 ana bölüm + checklist veya tablo + sonuç + CTA |
| Vaka analizi (case study) | 800–1.500 kelime | Müşteri profili + problem + süreç + sonuç + sonraki adımlar |
| Mimari analiz (deep dive) | 2.000–3.500 kelime | Diagram + kod örneği + tartışma + alternatifler + karar matrisi |

### Yazı sonrası eylem zinciri

Her blog yazısının altında üç şey olmalı:
1. **İlgili 3 yazı** (cluster içinden) — internal link
2. **Newsletter abonelik kutusu** — "yeni yazıları kaçırma"
3. **CTA bloğu** — "Bu konuyu konuşalım: Demo Talep Et"

---

## 10. SEO İçerik Yaklaşımı

> **Kapsam notu:** Bu bölüm landing içeriği yazılırken SEO açısından nelere dikkat edileceğini anlatır. Teknik SEO altyapısı (sitemap, schema markup, hreflang, redirect yönetimi vb.) kapsam dışıdır — landing projesi zaten kuruludur ve teknik SEO ayrı bir iş paketinde ele alınır.

### TR birincil hedef anahtar kelimeler (10 adet)

İçerik yazarken bu anahtar kelimeler doğal biçimde sayfaya yedirilmelidir.

| # | Anahtar kelime | Niyet | Birincil sayfa |
|---|---|---|---|
| 1 | yönetilen dijital ürün teslimatı | Commercial | Anasayfa |
| 2 | modüler dijital platform | Commercial | Anasayfa |
| 3 | headless CMS Türkiye | Commercial | Çözümler / Blog & İçerik |
| 4 | multi-tenant web platformu | Commercial | Özellikler |
| 5 | kurumsal web sitesi çözümü | Commercial | Çözümler / Kurumsal |
| 6 | headless e-ticaret platformu | Commercial | Çözümler / E-ticaret |
| 7 | İK portalı çözümü | Commercial | Çözümler / İK |
| 8 | ajans için dijital teslimat platformu | Commercial | Çözümler / Ajanslar için |
| 9 | dijital ürün geliştirme örnekleri | Informational | Kullanım Alanları |
| 10 | dijital dönüşüm danışmanlığı | Informational / Commercial | Hakkımızda |

### Sayfa başına hedef keyword dağılımı

Bölüm 7'deki her sayfa briefinde birincil + ikincil anahtar kelimeler verildi. Genel ilke:
- Her sayfanın **tek** bir birincil hedef anahtar kelimesi olmalı
- İkincil 3–5 anahtar kelime sayfa içinde doğal akışta geçmeli
- Aynı anahtar kelime iki sayfada birden birincil olmamalı (cannibalization)

### Title ve meta description rehberi

Her sayfa için:

**Title kuralları:**
- Maksimum 60 karakter (SERP'te kesilmeden görünür)
- Birincil anahtar kelime başa yakın
- Şirket adı ("| Craftive") sonda

**Meta description kuralları:**
- 140–160 karakter
- Birincil anahtar kelime + somut değer + CTA çağrısı
- Soru sormakla başlayabilir, sıkı bir cümleyle bitirir

#### Örnek title + meta her sayfa için

| Sayfa | Title (≤60 char) | Meta description (≤160 char) |
|---|---|---|
| Anasayfa | Yönetilen Dijital Ürün Teslimatı \| Craftive | Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim. Modüler ve izole altyapı ile kurumsal site, e-ticaret, İK portalı teslimatı. |
| Çözümler hub | Çözümler — Modüler Dijital Platform \| Craftive | Kurumsal site, e-ticaret, İK portalı, içerik platformu — aynı sağlam altyapı, projenize özel yapılandırma. Hemen inceleyin. |
| Çözümler / Kurumsal | Kurumsal Web Sitesi Çözümü \| Craftive | Marka rehberinize özel, çok dilli, içerik ekibi tarafından yönetilebilen kurumsal site altyapısı. Demo talep edin. |
| Çözümler / E-ticaret | Headless E-ticaret & Storefront \| Craftive | Headless mimari, esnek ürün katalogu, modern Next.js storefront. Markanızın deneyimini tam kontrol edin. |
| Çözümler / İK | İK Portalı & Kurum İçi Site Çözümü \| Craftive | Duyuru, belge, form merkezi — izole ve KVKK uyumlu kurum içi portal. Çalışanlarınıza özel teslim ediyoruz. |
| Çözümler / Blog & İçerik | Headless İçerik Platformu \| Craftive | Hızlı yayın akışı, çok dilli içerik, headless API. WordPress'i bırakıp gerçek bir içerik altyapısına geçin. |
| Çözümler / Ajanslar | Ajanslar İçin Delivery Platformu \| Craftive | Her müşteri için sıfırdan kurmaya elveda. Multi-tenant altyapıyla ajansınızı ölçeklendirin. |
| Özellikler | Craftive Özellikleri — Modüler & Multi-Tenant | Modüler mimari, multi-tenant altyapı, izole veritabanı, headless API ve yönetilen teslimat. Craftive'ın yedi temel yeteneği. |
| Kullanım Alanları | Dijital Ürün Geliştirme Örnekleri \| Craftive | Kurumsal site, e-ticaret, İK portalı, içerik platformu — gerçek senaryolarla Craftive'ın nasıl çalıştığını görün. |
| Fiyatlandırma | Fiyatlandırma — Projeye Özel Teklif \| Craftive | Sabit paket yok. Modüllerinize, kapsamınıza ve destek seviyenize göre özel teklif. 30 dakikada netleştirelim. |
| Blog | Blog — Mimari, İçerik, Teslimat \| Craftive | Headless mimari, multi-tenant, dijital ürün teslimatı üzerine birinci elden yazılar. Yeni yazıları kaçırmamak için abone olun. |
| Hakkımızda | Hakkımızda — One Platform, Every Configuration | Craftive bir araç değil, bir teslimat ortağıdır. Fikrinizi anlatın, dijital dönüşümünüze eşlik edelim. |
| İletişim | İletişim — Demo Talep Et \| Craftive | 30 dakikalık ücretsiz keşif görüşmesi. Fikrinizi dinliyor, size özel çözümü ve takvimi netleştiriyoruz. |

### İçerik yazarken kaçınılacak SEO hataları (pratik liste)

- ❌ Aynı anahtar kelimeyi sayfada **5'ten fazla** tekrarlama (keyword stuffing)
- ❌ Birden fazla `H1` kullanma — her sayfada sadece **bir** ana başlık olmalı
- ❌ İki sayfada birden aynı title veya meta description (duplicate)
- ❌ "Modüler dijital ürün teslimat platformu çözümü Türkiye" gibi 5+ kelime yığma başlıklar
- ❌ Hero altında **300 kelimeden az** içerik (thin content)
- ❌ Görsel için alt text yazmama (her görselin alt'i olmalı)
- ❌ "Buraya tıkla" gibi anchor metni — bunun yerine link metni içeriği anlatmalı
- ❌ Yazıların sonunda "umarım faydalı olmuştur" tarzı boş kapanış — onun yerine net bir CTA

### İçerik yazarken yapılması gerekenler

- ✓ Birincil anahtar kelime ilk 100 kelime içinde geçsin
- ✓ H2/H3 başlıkları soru formatında olabilir (snippet'e yardımcı)
- ✓ Listeler ve tablolar — okunabilirlik + featured snippet için
- ✓ İç linkler — her yazı en az **3 başka sayfaya** link versin
- ✓ Görseller — açıklayıcı dosya adı (`kurumsal-site-cms-craftive.jpg` gibi) + alt text
- ✓ Yazı uzunluğu konuya yetsin — yapay şişirmeden, eksik bırakmadan

---

## 11. CTA & Lead Yakalama (içerik düzeyinde)

### Birincil CTA — "Demo Talep Et" varyasyonları

Aynı eylem, farklı bağlama göre farklı copy. Tek bir kelime sıkışıklığı yaratmayın:

| Bağlam | Önerilen CTA metni |
|---|---|
| Anasayfa hero | Demo Talep Et |
| Çözümler hub | Görüşme Talep Et |
| Çözümler / Kurumsal | Demo Talep Et |
| Çözümler / E-ticaret | Demo Talep Et |
| Çözümler / İK | Demo Talep Et |
| Çözümler / Blog & İçerik | Demo Talep Et |
| Çözümler / Ajanslar | Ortaklık Görüşmesi Talep Et |
| Özellikler | Demo Talep Et |
| Kullanım Alanları | Projenizi Anlatın |
| Fiyatlandırma | Görüşme Talep Et |
| Blog (yazı altı) | Bu Konuyu Konuşalım |
| Hakkımızda | Demo Talep Et |
| İletişim | Görüşme Talebi Gönder (buton) |

**Yedek varyasyonlar** (A/B test için):
- Projenizi Anlatın
- Fikrinizi Paylaşın
- Discovery Call'a Başla
- Ücretsiz Keşif Görüşmesi

### İkincil CTA — Newsletter

| Bağlam | Önerilen CTA metni | Destek metni |
|---|---|---|
| Blog listeleme | Aboneliğe Katıl | Yeni yazıları kaçırma — ayda 1–2 e-posta. |
| Blog yazı altı | E-postama Ekle | Mimari ve teslimat üzerine birinci elden notlar. |
| Footer (tüm sayfalar) | Bültene Abone Ol | Spam yok, sadece değerli içerik. |

### Üçüncül CTA — Docs (geliştirici güven sinyali)

| Bağlam | Önerilen CTA metni | Hedef |
|---|---|---|
| Özellikler sayfası | Geliştirici Dokümantasyonu | docs.craftive.io |
| Hakkımızda | Teknik Mimari Detayı | docs.craftive.io |
| Hero ikincil link | API Referansını İnceleyin | docs.craftive.io |

### Form alanı etiket önerileri (TR)

Bölüm 7.13'teki tablodan kısa hatırlatma:
- **İsim:** "Adınız Soyadınız"
- **E-posta:** "E-posta adresiniz" — yardımcı: "İş e-postanız idealdir"
- **Telefon (opsiyonel):** "Telefon (isteğe bağlı)" — yardımcı: "Daha hızlı dönüş için"
- **Şirket / proje:** "Şirketiniz veya projeniz"
- **Sektör (opsiyonel):** Kurumsal / E-ticaret / İK / Yayıncı / Ajans / Diğer
- **Bütçe aralığı (opsiyonel):** Henüz belirsiz / 5–15K USD / 15–30K USD / 30K+
- **Mesaj:** "Fikrinizi birkaç cümleyle anlatın"
- **Kanal (opsiyonel):** LinkedIn / Tavsiye / Google / Diğer

### Form gönderim sonrası mesajlar

**Başarı (success state):**
> Teşekkürler! Talebinizi aldık. En geç 1 iş günü içinde size dönüş yapacağız.

**Hata (error state — bağlantı problemi):**
> Talebiniz şu anda gönderilemedi. Lütfen birkaç dakika sonra tekrar deneyin veya doğrudan hello@craftive.io adresine yazın.

**Form doğrulama (validation):**
- E-posta formatı: "Geçerli bir e-posta adresi giriniz."
- Zorunlu alan boş: "Bu alanı doldurmanız gerekiyor."

### Cross-page CTA yerleşim ilkeleri (içerik düzeyinde)

> Bu yerleşim önerileri içerik blok seviyesindedir; teknik komponent yapısı landing projesinde zaten kuruludur.

- **Her sayfada en az 2 CTA bloğu** olmalı: biri hero'da, biri sayfa altında
- **Hero CTA'sı** sayfa amacının doğrudan eylemi olmalı (Anasayfa = Demo Talep Et)
- **Sayfa altı CTA bloğu** "büyük bant" formatında — başlık + 1 cümle + buton
- **Blog yazılarında** CTA yazının ilgili konusuna bağlı olabilir (örn. e-ticaret yazısı sonunda "Mağazanızı Konuşalım")
- **Footer**ta her zaman "Bültene abone ol" + "Demo Talep Et" linki olmalı

---

## 12. Pazarlama & Dağıtım Yönetimi

### Kanal önceliklendirme (TR pazara uyarlanmış)

| Sıra | Kanal | Neden bu sırada |
|---|---|---|
| 1 | LinkedIn Outbound | CTO, Operasyon Direktörü ve ajans sahipleri doğrudan erişilebilir. Düşük maliyet, yüksek niyet. |
| 2 | SEO / Blog | Uzun vadeli kümülatif değer. "Headless CMS", "multi-tenant", "managed delivery" anahtar kelimelerinde Türkiye'de boşluk var. |
| 3 | Referral / Ajans Partnerliği | Dijital ajanslar hem müşteri hem yönlendirme kaynağı. Tek ilişkiden birden çok proje çıkar. |
| 4 | Cold Email | Belirli dikeylere (e-ticaret markaları, İK yazılım şirketleri) kişiselleştirilmiş yaklaşımla ölçeklenebilir. |
| 5 | Twitter/X & Geliştirici Toplulukları | Teknik kredibilite inşa eder. Satın alma kararını etkileyen geliştiricilere ulaşır. |
| 6 | Paid Search (Google Ads) | Mesajlar doğrulandıktan sonra. Şimdilik test etmek için maliyetli. |

**Şimdilik kaçınılacaklar:** Geniş sosyal reklam (broad social ads), influencer marketing. Teknik B2B ürün için bu aşamada düşük ROI.

### 30 günlük operasyonel takvim (TR uyarlaması)

#### Hafta 1 — Temel

- [ ] 3 hedef dikey netleştirilir (örn: e-ticaret markaları, İK tech şirketleri, dijital ajanslar)
- [ ] 3 messaging pillar kullanılarak 5 LinkedIn yazısı taslağı hazırlanır
- [ ] Basit CRM kurulur (Notion veya HubSpot Free) — outreach takibi için
- [ ] LinkedIn'de 50 ICP prospekt listesi çıkarılır

#### Hafta 2 — Outbound Lansman

- [ ] Yumuşak (pitch'siz) açılış mesajıyla 20 LinkedIn bağlantı talebi gönderilir
- [ ] 2 LinkedIn yazısı yayınlanır; etkileşim izlenir
- [ ] 3 e-postalık cold email serisi taslağı yazılır (problem → çözüm → kanıt)
- [ ] "KOBİ'ler için yönetilen headless CMS" temalı 1 blog yazısı yayınlanır

#### Hafta 3 — Konuşmalar & İçerik

- [ ] LinkedIn bağlantılarıyla takip; 5+ kişi discovery call'a çekilir
- [ ] Cold email serisi 30 kişiye gönderilir
- [ ] Blog yazısı 2 ilgili topluluğa gönderilir (Dev.to, Indie Hackers veya TR muadili)
- [ ] 1 müşteri vakası kısa case study olarak yazılır (anonim olabilir)

#### Hafta 4 — Değerlendirme & İkilik Yatırım

- [ ] Hangi kanal en çok yanıt ve görüşme getirdi — analiz
- [ ] Görüşmelerde duyulan itirazlara göre mesajlar revize edilir
- [ ] Case study LinkedIn yazısına ve e-posta nurture asset'ine dönüştürülür
- [ ] İşleyen kanallara göre 2. ay hedefleri belirlenir

**Tahmini zaman yatırımı:** Tek kişi için haftada 8–10 saat. İlk 3 müşterinin ICP'yi doğrulamasına kadar LinkedIn ve haftada 1 içerik yatırımı önceliklidir.

### Sosyal kanıt toplama stratejisi

Lansman sonrası 90 gün içinde toplanacak materyaller:

| Tip | Hedef | Kullanım yeri |
|---|---|---|
| Müşteri logosu | 5–8 logo (gerçek müşteri veya partner) | Anasayfa + Çözümler hub |
| Testimonial (yazılı) | 3 testimonial (isim + rol + 1–2 cümle alıntı) | Anasayfa + Çözümler alt sayfaları |
| Case study (yazılı) | 2 vaka (1 sayfa, anonim olabilir) | Kullanım Alanları + Blog |
| Video referans | 1 müşteri görüşmesi (1–3 dakika) | Anasayfa hero veya altı |
| Sayı vurgusu | "X tenant", "Y modül", "Z teslimat" | Anasayfa + Hakkımızda |

### Ölçüm önerileri

Landing page lansmanı sonrası ölçülmesi gereken metrikler:

| Metrik | Hedef davranış | Birincil sayfa |
|---|---|---|
| Demo request sayısı | Form gönderimi | İletişim |
| Demo → qualified call dönüşümü | Görüşmeye katılma | – |
| Qualified call → teklif dönüşümü | Yazılı teklif aşaması | – |
| Sayfa başına ortalama scroll derinliği | %75'i geçmek | Anasayfa, Çözümler |
| Blog organic trafik (oturum) | Aylık büyüme | Blog |
| Newsletter abone sayısı | Haftalık büyüme | Blog + Footer |
| Kanal-call attribution | Hangi kanaldan kaç görüşme | – |
| Çıkış oranı (bounce) | %60 altı (sektör ortalaması) | Anasayfa |

---

## 13. Açık Sorular / Bir Sonraki Adımlar

İçerik girişine başlamadan önce netleşmesi gereken konular. Bu sorulara cevap verildiğinde rapor "tam yayına hazır" duruma gelir.

### 13.1 Sosyal kanıt & referans

| # | Soru | Neden önemli? |
|---|---|---|
| 1 | Case study yazılabilecek hazır tenant müşterisi var mı? | Anasayfa + Kullanım Alanları + Blog için temel materyal. Yoksa "tipik senaryo" formatında kurgu yazılır. |
| 2 | Müşteri logosu paylaşma izni alınmış 5+ müşteri var mı? | Anasayfa "logos" bandı için. Yoksa "framework partners" stratejisine (Spring, Next.js, Cloudflare) geçilir. |
| 3 | Testimonial verecek (isim + rol açıklamayı kabul eden) 3 müşteri var mı? | Çözümler alt sayfalarında güven sinyali için. |

### 13.2 Fiyatlandırma sayfası

| # | Soru | Etkilediği bölüm |
|---|---|---|
| 4 | Fiyatlandırma sayfasında tipik bütçe aralıkları gösterilecek mi? | 7.10 Bölüm 3'te tablo opsiyonel olarak duruyor. |
| 5 | "Starter / Standard / Enterprise" gibi paket çerçevesi gösterilecek mi yoksa saf "quote-only" mı? | Eğer paket gösterilecekse fiyatlandırma sayfası baştan revize edilir. |
| 6 | Aylık destek/operasyon anlaşması zorunlu mu, opsiyonel mi? | Fiyat SSS'sini etkiler. |

### 13.3 Marka ve ekip

| # | Soru | Etkilediği bölüm |
|---|---|---|
| 7 | Hakkımızda sayfasında "Ekip" bölümü olacak mı? Kurucu fotoğraf + bio paylaşılacak mı? | 7.12 Bölüm 5 opsiyonel olarak duruyor. |
| 8 | Blog yazarları kim olacak? (Yazar bio için isim + rol + fotoğraf gerekiyor) | 7.11 + her yazının author bilgisi. |
| 9 | E-E-A-T (Experience, Expertise, Authoritativeness, Trustworthiness) için göstereceğimiz sertifika, ödül veya partner statüsü var mı? | Hakkımızda + Footer. |

### 13.4 İletişim ve form

| # | Soru | Etkilediği bölüm |
|---|---|---|
| 10 | Demo request formundaki opsiyonel alanlar (telefon, bütçe, sektör) sahaya dahil mi? | 7.13 form yapısı. |
| 11 | Hızlı dönüş için gerçek bir SLA (örn. "1 iş günü") taahhüt edilebilir mi? | Form sonrası başarı mesajı. |
| 12 | Public e-posta adresi nedir? (hello@craftive.io örnek kullanıldı) | 7.13 Bölüm 4. |
| 13 | LinkedIn şirket sayfası URL'i nedir? | Footer + İletişim alternatif kanallar. |

### 13.5 İçerik üretim kapasitesi

| # | Soru | Etkilediği bölüm |
|---|---|---|
| 14 | Blog için haftada 1 yazı tempo'su sürdürülebilir mi? Yoksa 2 haftada 1 mi planlansın? | Bölüm 9 yayın takvimi. |
| 15 | İlk pillar yazısı (Türkiye'de Headless CMS Rehberi) için ne zaman yazılabilir? | Lansman takvimi. |
| 16 | İçerik üretiminde dış kaynak (freelance yazar, ajans) kullanılacak mı? | Süreç & kalite kontrolü. |

### 13.6 Strateji ve roadmap

| # | Soru | Etkilediği bölüm |
|---|---|---|
| 17 | İngilizce versiyon ne zaman gündeme gelir? (Şu an TR-only) | Gelecekteki hreflang ve içerik genişleme planı. |
| 18 | Karşılaştırma sayfaları (Craftive vs Contentful, vs Strapi) ileride yazılacak mı? | Eğer evet, SEO için yüksek-niyetli trafiği yakalama fırsatı. |
| 19 | Programmatic SEO (her dikey + sektör için otomatik üretilmiş sayfalar) kapsamda mı? | Şu an değil, ama lansman sonrası 6. ay için değerlendirme. |

### Karar matrisinde öneriler

> **Hızlı yayın için minimum gerekenler:**
> - 5 müşteri logosu (veya framework partner logo stratejisi)
> - 1 anonim case study (Kullanım Alanları için temel materyal)
> - Form için gerçek e-posta adresi + LinkedIn sayfası
> - İlk pillar yazısı taslağı (Blog'un boş açılmaması için)
>
> **Lansman sonrası 30 gün içinde:**
> - 2 testimonial alıntısı + 1 ilk gerçek case study
> - Demo→teklif dönüşümünün ilk ölçümü
> - İlk 3 destekleyici blog yazısı
>
> **3 ay sonra değerlendirme:**
> - İngilizce versiyon kararı
> - Karşılaştırma sayfası kararı
> - Programmatic SEO değerlendirmesi

---

## Ek: Doküman Kullanım Notları

### İçerik editörü için pratik kullanım

1. Sayfa içeriği girerken **bu dokümandaki bölümü açın**, doğrudan kopyalayıp paste edin.
2. Hero başlıkları doküman içinde **blockquote** (`>`) ile işaretlenmiştir — bunlar yapışacak hâzır metinlerdir.
3. Tabloların içeriği bullet listesi olarak veya kart bileşeni olarak görselleştirilebilir.
4. Tabloların hücrelerinde "—" veya boş işaret, içeriğin **henüz netleşmediğini** gösterir; Bölüm 13'teki açık sorulara cevap alındıkça doldurulmalı.

### Doküman güncellemeleri

Bu doküman canlı bir referanstır. Yeni vakalar, yeni mesaj testleri, yeni müşteri itirazları ortaya çıktıkça **versiyon notuyla** güncellenir. Doküman başındaki "Son güncelleme" tarihi her değişiklikte yenilenir.

### İlgili dokümanlar

- `docs/marketing/strategy.md` — üst seviye iş ve sales stratejisi
- `docs/marketing/customer-platform-capabilities-report-en.md` — yetenek raporu (EN)
- `docs/marketing/customer-project-briefing-tr.md` — müşteriye giden brief şablonu
- `docs/marketing/customer-presentation-deck-outline-tr.md` — sunum yapısı
- `docs/marketing/landing-seo.md` — landing SEO (varsa daha eski not)
