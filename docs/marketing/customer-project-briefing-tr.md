# Craftive — Müşteri brifing raporu (proje özeti)

Bu belge, **sunum slaytı veya demo senaryosu** formatında değil; müşteri tarafında sıkça sorulan **mimari, kapsam, güvenlik ve operasyon** konularına odaklı, teknik doğruluk için repodaki dokümantasyonla hizalı bir özet sunar.

**Referans dokümanlar:** [docs/README.md](../README.md), [docs/marketing/strategy.md](./strategy.md), [docs/global/architecture.md](../global/architecture.md), [docs/global/security-multi-tenancy.md](../global/security-multi-tenancy.md), [docs/prelaunch.md](../prelaunch.md).

---

## 1. Belgenin amacı

- Craftive çözümünün **ne olduğunu** ve **hangi sınırlarla** satıldığını netleştirmek (bkz. [strategy.md](./strategy.md): sabit self-serve SaaS paketi değil, proje bazlı modüler teslim).
- Müşteri IT, bilgi güvenliği veya satın alma tarafının soracağı **veri ayrımı, kimlik doğrulama, genel yüzey alanı** sorularına cevap verebilecek düzeyde özet sağlamak.
- Ürünün **bileşenlerini** (backend, admin, vitrin, pazarlama sitesi) tek çerçevede göstermek.

---

## 2. Ürün tanımı (iş düzeyi)

Craftive; kurumsal web, içerik operasyonları, headless katalog / mağaza temeli, e-posta pazarlama veya ajans tipi çoklu müşteri teslimi gibi senaryolarda **ortak bir platform çekirdeği** üzerinden hızlanmayı hedefleyen bir yapıdır. Teslim modeli; platform lisansı + **ihtiyaca göre modül seti** + kurulum / yapılandırma + gerektiğinde uyarlama ve işletme desteğidir ([strategy.md](./strategy.md)).

---

## 3. Genel sistem yapısı (monorepo)

| Bileşen | Rol |
| -------- | ----- |
| `backend/` | Spring Boot API; tüm iş kuralları, güvenlik, çok kiracılı yönlendirme, CMS ve katalog API’leri. |
| `storefront/` | **Yönetici (admin) arayüzü** — Angular uygulaması; içerik, site, kullanıcılar, platform operasyonları burada. Kamu vitrin değildir ([README.md](../../README.md)). |
| `storefront-nextjs/` | **Demo / referans headless vitrin** — Next.js; CMS delivery API ile beslenir. Gerçek müşteri vitrinleri çoğunlukla bu tabanın fork’u ve ayrı yaşam döngüsü ile yönetilir ([prelaunch.md](../prelaunch.md)). |
| `landing/` | **Pazarlama sitesi** (`craftive.io`); statik export, Cloudflare Pages; demo talebi ve bülten gibi akışlar platform public API’lere bağlanır ([docs/README.md](../README.md)). |
| `docker/`, CI/CD | Dağıtım ve altyapı otomasyonu (ör. Traefik, ortam profilleri); ayrıntılar [docs/global/devops.md](../global/devops.md) ve [prelaunch.md](../prelaunch.md). |

**Kontrol düzlemi vs veri düzlemi:** Platform yönetimi (tenant kaydı, provizyon, demo talepleri gibi) ile her müşterinin kendi verisi **mantıksal ve fiziksel olarak ayrıştırılmıştır** (aşağıda §6).

---

## 4. Mimari özeti

- **Clean Architecture:** Sunum katmanı (REST), uygulama servisleri, domain ve altyapı (JPA, tenant filtreleri) ayrımı; iş kurallarının konumu dokümante edilmiştir ([architecture.md](../global/architecture.md)).
- **Çok kiracılılık — veritabanı başına kiracı:** Her aktif kiracı için ayrı MySQL veritabanı (`ac_subdomain_{id}`); platform veritabanı `platform_management`. Tenant tablolarında `tenant_id` kolonu **kullanılmaz**; izolasyon fizikseldir ([architecture.md](../global/architecture.md), [README.md](../../README.md)).
- **API kök yolu:** Backend `context-path: /api` altında yayınlanır; örnek olarak controller `/cms` ise dış dünyada `/api/cms` olur ([architecture.md](../global/architecture.md)).

---

## 5. Modül ve yetenek kapsamı

**Kullanıcıya “seçilebilir modül” olarak sunulan provizyon kataloğu** (özet): `core`, `product`, `mail_marketing`. `core` seçildiğinde çalışma zamanında **medya kütüphanesi, bileşen kütüphanesi, sayfa oluşturucu** gibi çekirdeğe bağlı yetenekler de devreye alınır ([ModuleCode.java](../../backend/src/main/java/com/backend/domain/enums/ModuleCode.java), [docs/README.md](../README.md)).

**İçerik modeli (özet):** Sayfa şablonu (`PageTemplate`) → içerik bölgeleri (`PageSlot`) → yerleştirilebilir içerik birimleri (`CmsComponent`). Bu model hem admin tarafında yönetilir hem de headless JSON çıktısında tüketilir; canlı pazarlama anlatımıyla uyumludur ([craftive.io](https://craftive.io/en/)).

**Öne çıkan ürün işleri (müşteri açısından anlamlı):**

| Konu | Müşteri faydası | Teknik / doküman işareti |
| ------ | ----------------- | ------------------------- |
| Site Dashboard | Tek yerden site kimliği, teknik SEO ayarları, güvenlik politikası özeti; yapılandırıldıysa analitik ve arama konsolu özetleri | [site-dashboard.md](../modules/site-dashboard.md) |
| SmartEdit | Yayınlanmış sayfayı vitrin görünümü üzerinden düzenleme; taslak / önizleme ile canlıyı ayırma | [smartedit.md](../modules/smartedit.md) |
| Headless CMS teslimat | Kimlik doğrulaması olmadan, tenant bağlamında sayfa, bileşen, navigasyon, site meta, robots/sitemap | [cms-delivery.md](../modules/cms-delivery.md) |
| Ürün kataloğu | Kategori ve ürün yönetimi; CMS ile birleşik headless ürün sayfaları | `product` modülü, ilgili controller’lar |
| E-posta pazarlama | Şablon ve abone yönetimi (tenant ve platform kapsamları dokümante) | [mail-marketing.md](../modules/mail-marketing.md) |
| ImpEx | Onaylı roller için SQL tabanlı toplu veri içe aktarma (tenant DB ile sınırlı) | [impex.md](../modules/impex.md), [security-multi-tenancy.md](../global/security-multi-tenancy.md) |
| Provizyon | Tenant oluşturma, modül migrasyonları, iş durumu takibi | [platform-provisioning.md](../modules/platform-provisioning.md) |

**SmartEdit kapsam notu (beklenti yönetimi):** Dokümantasyonda faz bazlı kapsam açıkça listelenir; örneğin slot içinde bileşen ekleme/sıralama veya zamanlanmış yayın gibi özellikler **şu anki fazın dışında** bırakılmış olabilir. Müşteriye “yol haritası / ayrı teklif” konusu olarak değerlendirilmelidir ([smartedit.md](../modules/smartedit.md)).

---

## 6. Güvenlik ve çok kiracılık (can alıcı konular)

### 6.1 Kiracı çözümlemesi

- Varsayılan olarak kiracı; `X-Tenant-ID`, `X-Tenant-Subdomain` veya güvenilir hostname zinciri ile çözülür.
- `Origin` / `Referer` kiracı çözümlemesinde **kullanılmaz** (istemci manipüle edilebilir).
- `X-Forwarded-Host` varsayılan olarak **güvenilmez** kabul edilir; yalnızca ters vekil güvenilir şekilde yapılandırıldığında açılabilir ([security-multi-tenancy.md](../global/security-multi-tenancy.md)).

### 6.2 Uç nokta sınıfları

- **Platform uçları** (`/api/platform/**`, `/api/provisioning/**`, çoğu `/api/tenants/**`): `SUPER_ADMIN` rolü.
- **Kiracı uçları:** Çözülmüş kiracı + JWT ve rol / modül kuralları.
- **Halka açık CMS teslimatı** (`/api/cms/**` vb.): Kimlik doğrulaması yok; fakat **kiracı bağlamı zorunludur** (başka kiracının verisine erişim yok).
- **Halka açık formlar:** Örn. `POST /api/public/contact-requests` kiracı kapsamlıdır; reCAPTCHA ve hız sınırları yapılandırmaya bağlıdır ([docs/README.md](../README.md)).
- **ImpEx:** Yalnızca **aktif kiracı veritabanı** üzerinde; platform DB’ye çalışma zamanı ImpEx yolu yoktur ([security-multi-tenancy.md](../global/security-multi-tenancy.md)).

### 6.3 Oran sınırlama (abus önleme)

Resilience4j ile örneğin ImpEx yürütme, demo talebi gönderimi, bazı admin işlemleri için dakika başına istek sınırları tanımlıdır; aşımda HTTP 429. Demo taleplerinde ek olarak aynı e-posta + IP için kısa pencerede mükerrer bastırma ve sabit süreli yanıt davranışı (enumeration riskini azaltma) anlatılır ([security-multi-tenancy.md](../global/security-multi-tenancy.md)).

### 6.4 HTTP güvenlik başlıkları (API)

API yanıtlarına `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `Referrer-Policy`, kısıtlı `Content-Security-Policy`, prod benzeri profillerde `Strict-Transport-Security` uygulanır. Admin ve vitrin HTML’i için CSP’nin Traefik / CDN katmanında da ele alındığı belirtilir ([security-multi-tenancy.md](../global/security-multi-tenancy.md)).

### 6.5 Kimlik doğrulama ve oturum

Ayrıntılar [authentication.md](../global/authentication.md) içindedir; müşteri brifinginde tipik olarak **JWT tabanlı oturum**, rol ayrımı (`SUPER_ADMIN`, kiracı admin / kullanıcı), şifre sıfırlama ve mümkünse **2FA politikası** (Site Dashboard üzerinden yapılandırılabilir politika) özetlenir.

### 6.6 Girdi doğrulama ve içerik güvenliği

Root [README.md](../../README.md) özeti: DTO doğrulama, XSS için sanitizasyon / encoder kullanımı, rate limiting. Müşteri güvenlik anketlerinde bu maddeler “mevcut savunma katmanları” olarak işaretlenebilir.

---

## 7. Gizlilik ve uyumluluk iletişimi

- **Veri ayrımı:** Kiracı başına ayrı veritabanı; “komşu kiracı” verisine yanlışlıkla erişim riskini mimari olarak düşürür. GDPR veya benzeri çerçevelerde anlatım **müşteri hukuk danışmanı** ile doğrulanmalıdır; bu belge hukuki tavsiye değildir.
- **Yedekleme ve felaket kurtarma:** Operasyonel taahhüt sözleşme ve SLA ile tanımlanır; teknik olarak bağımsız veritabanları yedeklemeyi **kiracı bazında** planlamayı kolaylaştırır.

---

## 8. Operasyon ve gözlemlenebilirlik

[prelaunch.md](../prelaunch.md) özetinden: üretim öncesi gizli anahtarlar (JWT, DB, SMTP, Cloudflare DNS, Loki vb.) ayrı ortamlarda yönetilir; `/config` paneli **bazı** runtime override’ları kapsar, deploy-time sırları kapsamaz. Loglarda **korelasyon kimliği** kullanımı sorun gidermeyi destekler ([security-multi-tenancy.md](../global/security-multi-tenancy.md)).

---

## 9. Pazarlama yüzeyi ve dokümantasyon

- Canlı pazarlama sitesi [craftive.io/en/](https://craftive.io/en/) ürün mesajı ve kullanım örnekleri için referans; repodaki `landing/` ile uyumludur.
- Teknik dokümantasyon ayrı barındırılabilir (ör. `docs.craftive.io`); API ve modül davranışı için **repo içi `docs/`** kaynak doğruluk kabul edilir.

---

## 10. Bilinen sınırlar ve dürüst mesajlar

- **Self-serve “tek fiyatlı SaaS”** beklentisi strategy ile uyumlu değildir ([strategy.md](./strategy.md)).
- **SmartEdit ve içerik iş akışı:** Faz 1 kapsamı dokümante sınırlara sahiptir; tam WYSIWYG ürün yönetimi vaadi verilmeden önce [smartedit.md](../modules/smartedit.md) gözden geçirilmelidir.
- **Storefront:** Repo içi Next.js vitrin **referans / demo**; müşteri markası genelde ayrı repo ve deploy ile yönetilir ([prelaunch.md](../prelaunch.md)).

---

## 11. Sonuç

Craftive; **modüler, kiracı-izole, headless’a hazır** bir içerik ve (isteğe bağlı) katalog platformudur. Müşteri kararı için kritik farklılaştırıcılar: **veritabanı başına kiracı**, **katmanlı endpoint güvenliği ve hız sınırları**, **yönetilen provizyon ve modül modeli**, **kamuya açık CMS API ile vitrin bağımsızlığı**. Bu belge, derin teknik inceleme yerine **doğru soruların sorulması** ve **dokümantasyona yönlendirme** amacıyla hazırlanmıştır.
