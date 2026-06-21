# Commerce Modülü Roadmap Taslağı

> Durum: Brainstorm sonucu netleşen kapsam taslağı. Bu doküman implementasyon planı değildir; MVP kararlarını ve sonraki fazları kısa şekilde özetler.

> Implementasyon durumu: Commerce module foundation, Product Catalog variant foundation, customer account/address book, anonymous/customer cart, checkout foundation, internal payment attempt, iyzico sandbox CheckoutForm init/callback, başarılı ödeme sonrası backend order finalization, customer order read API, müşteri iptal/iade talebi, admin iptal/iade karar akışı, iyzico tam refund, commerce admin operasyon görünürlüğü, admin order status transition + manual fulfillment slice, versioning'li legal template yönetimi, checkout legal readiness/onay akışı, order legal snapshot capture, customer transactional email v1, notification outbox admin görünürlüğü + manual/automatic retry ve `commerce-ui` Next.js storefront shell + minimal design + cart foundation + product listing/search + product detail add-to-cart + customer auth/account + address book + checkout + payment return + order history + iptal/iade talebi foundation hazırlandı. Transactional SMS, admin notification alerts ve notification template UI/API henüz yapılmadı.

## 1. Konumlandırma

Craftive commerce, self-serve mağaza kurucu değil; projeye özel kurulan, yönetilen headless D2C commerce temelidir.

Kararlar:

- Commerce core generic ve sektör bağımsız geliştirilecek.
- İlk MVP Türkiye odaklı olacak: TR-first, global-ready.
- Demo tenant minimalist ev & yaşam / dekorasyon / lifestyle concept store dikeyinde hazırlanacak.
- Core commerce delivery fixed-price proje olarak satılacak.
- AI ve ileri otomasyon özellikleri MVP sonrasında aylık premium add-on olarak değerlendirilecek.

## 2. Modül Sınırları

Product Catalog sahiplenir:

- Ürün, kategori, koleksiyon
- Generic varyant yönetimi
- SKU, price, firstPrice, vergi bilgisi, stok
- Ürün medya ve SEO alanları
- Ürün import, publish/archive akışları

Commerce sahiplenir:

- Customer account
- Sepet
- Checkout
- Ödeme
- Sipariş
- İptal/iade talepleri
- Fulfillment operasyonları
- Transactional bildirimler
- Operasyonel satış özeti

Commerce runtime ayarları ayrı dashboard UI içinde değil, mevcut key-value config panel yaklaşımıyla yönetilecek.

## 3. MVP Kapsamı

### 3.1 Product Catalog Genişletmeleri

Product Catalog, commerce-ready hale getirilecek.

Kapsam:

- Generic varyant option yönetimi
- Tenant admin panelinden option/value tanımlama
- Option görünüm tipi: `text`, `color`
- Ürün başına en fazla 2 varyant option
- Demo varyantları: renk ve size/ölçü
- Otomatik varyant kombinasyonu üretme
- Varyant bazlı SKU, price, firstPrice, vergi bilgisi, stok, aktif/pasif durum
- Sepet ve sipariş satırlarında product variant kullanımı
- KDV dahil fiyat girişi
- Default para birimi: TRY
- Varyant bazlı stok ve sipariş tamamlandığında stok düşümü
- Stokta olmayan varyantın sepete eklenmesini engelleme
- Kategori ve koleksiyon ayrımı
- Ürün durumları: Draft, Published, Archived
- Toplu ürün import/güncelleme, validation, preview, job geçmişi
- Import edilen ürünleri Draft olarak içeri alma
- Toplu publish/archive
- Ana ürün görseli, galeri, opsiyonel varyant görseli, alt text
- Responsive medya desteği
- SEO title, SEO description, slug

MVP dışında:

- Stok rezervasyonu
- Çoklu para birimi
- Varyant bazlı maksimum adet override
- AI görsel üretimi / arka plan kaldırma / görsel iyileştirme

### 3.2 Customer Account

Customer account, admin user modelinden ayrı tutulacak ve commerce içinde yaşayacak.

Kapsam:

- Müşteri hesabı zorunlu checkout
- E-posta/şifre ile giriş
- Google ile giriş
- Kayıt alanları: ad, soyad, e-posta, şifre, telefon
- Opsiyonel profil alanları: cinsiyet, doğum tarihi
- Non-blocking e-posta doğrulama
- Telefon zorunlu, ancak doğrulamasız
- Adres defteri
- Teslimat/fatura adresi ayrımı
- Bireysel/kurumsal fatura bilgileri
- Sipariş geçmişi
- Account sayfası: profil, adresler, siparişler, bildirim/izin tercihleri, şifre değiştir, çıkış yap
- Üyelik sözleşmesi ve KVKK aydınlatma onayları
- Opsiyonel pazarlama izinleri: e-posta, SMS, telefon araması

MVP dışında:

- Guest checkout
- Telefon OTP doğrulaması
- Wishlist
- Hesap silme/pasifleştirme talebi
- Apple/Facebook login

### 3.3 Sepet

Sepet customer account'a bağlı ve backend'de kalıcı olacak.

Kapsam:

- Product variant sepete ekleme
- Adet güncelleme ve ürün çıkarma
- Sepet fiyat hesaplama
- Fiyat/stok doğrulama
- Fiyat/stok değişikliği uyarıları
- Cart drawer ve sepet sayfası
- Minimum adet: 1
- Maksimum adet: tenant global config
- Ürün bazlı opsiyonel maksimum adet override
- Sepet görüntüleme ve checkout başlangıcında fiyat/stok yeniden doğrulama

MVP dışında:

- Guest/anonim sepet
- Login sonrası sepet merge
- Sepet expiration / eski sepet temizliği
- Kupon/promosyon
- Cross-sell / önerilen ürünler

### 3.4 Checkout

Checkout adım adım ilerleyecek:

1. Adres seçimi / adres ekleme
2. Teslimat yöntemi
3. Ödeme yöntemi
4. Sipariş özeti ve onaylar
5. Ödeme / sipariş tamamlama

Kapsam:

- Mevcut adres seçimi ve checkout sırasında yeni adres ekleme
- Fatura adresi teslimat adresiyle aynı seçeneği
- Farklı fatura adresi seçimi/ekleme
- Bireysel/kurumsal fatura seçimi
- Tek standart kargo yöntemi
- Sabit kargo ücreti ve ücretsiz kargo eşiği
- Kargo ayarlarının config panelden yönetilmesi
- Online kredi/banka kartı ödemesi
- Sipariş özeti
- Mesafeli satış sözleşmesi ve ön bilgilendirme formu onayları
- Checkout başlangıcında ve ödeme öncesinde fiyat/stok doğrulama
- Fiyat değişirse kullanıcıya uyarı ve yeniden onay
- Stok yetersizse checkout'u durdurma

MVP dışında:

- Kargo sağlayıcı entegrasyonu
- Pickup / mağazadan teslim
- Havale/EFT
- Kapıda ödeme

### 3.5 Ödeme

MVP ödeme sağlayıcısı iyzico olacak.

Kapsam:

- Payment provider adapter yapısı
- Tenant'ın kendi iyzico hesabını kullanması
- iyzico API key/secret değerlerinin tenant config ile yönetilmesi
- Secret değerlerin encrypted tutulması
- Online kart ödemesi
- Anında tahsilat
- 3D Secure zorunlu
- Internal checkout/payment attempt kaydı
- Ödeme başarılıysa customer-facing order oluşturma / Paid duruma geçirme
- Ödeme başarısızsa müşteri tarafında sipariş göstermeme
- Payment Failed bilgisini internal payment attempt history'de tutma
- Sepeti ödeme başarısızlığında koruma
- Kullanıcının yeniden ödeme deneyebilmesi
- Provider transaction id kaydı
- iyzico callback/webhook işleme
- Duplicate callback/webhook event'lerini güvenli işleme

MVP dışında:

- PayTR, Stripe ve diğer provider'lar
- Authorize/capture ödeme modeli

### 3.6 Siparişler

Customer-facing order status ile internal payment attempt status ayrı tutulacak.

Customer-facing order status:

- Paid
- Preparing
- Shipped
- Delivered
- Cancellation Requested
- Return Requested
- Cancelled
- Refunded

Internal payment attempt status:

- Payment Pending
- Payment Failed
- Payment Succeeded

Kapsam:

- Sipariş oluşturma
- Sipariş numarası formatı: `ORD-YYYYMMDD-000001`
- Default prefix: `ORD`
- Prefix'in tenant config ile değiştirilebilmesi
- Admin sipariş detay ekranı
- Status history satırına bağlı opsiyonel iç notlar
- Başarısız ödeme denemelerinin normal sipariş listesine karışmaması

MVP dışında:

- Admin manuel sipariş oluşturma

### 3.7 İptal ve İade Talepleri

Müşteri doğrudan iptal/iade yapmayacak; talep oluşturacak. Para ve sipariş aksiyonu admin onayıyla gerçekleşecek.

Kapsam:

- Müşteri taraflı iptal talebi
- Müşteri taraflı iade talebi
- Talep tipi: iptal / iade
- Talep nedeni ve açıklama
- Talep durumları: Pending, Approved, Rejected
- Admin talep inceleme ve onay/red akışı
- Admin onayıyla sipariş iptali
- Admin onayıyla tam ödeme iadesi
- Admin iade onayı verdiğinde iyzico üzerinden tam iade isteği başlatma
- Müşterinin account sayfasında talep durumunu görmesi

MVP dışında:

- Kısmi iade
- Ürün bazlı iade
- Değişim
- İade kargo yönetimi
- Talebe görsel ekleme

### 3.8 Kargo & Fulfillment

MVP fulfillment manuel operasyon olacak.

Kapsam:

- Admin siparişi Preparing durumuna alabilir
- Admin siparişi Shipped durumuna alabilir
- Admin siparişi Delivered durumuna alabilir
- Sipariş status transition akışı yalnızca tek adım ileri gider: `PAID -> PREPARING -> SHIPPED -> DELIVERED`
- Kargo firması manuel seçilir/girilir
- Kargo takip numarası manuel girilir
- Kargo takip URL'i opsiyonel tutulur
- Status history üzerinde aktör, fulfillment bilgisi ve opsiyonel iç not saklanır

Hazır:

- Sipariş Shipped olduğunda müşteriye transactional email gönderimi

MVP dışında:

- Kargo sağlayıcı entegrasyonu
- Otomatik kargo etiketi / label oluşturma
- Pickup / mağazadan teslim

### 3.9 Transactional Bildirimler

MVP'de transactional e-posta ve sınırlı transactional SMS olacak.

Provider yaklaşımı:

- E-posta provider: Postmark
- SMS provider: İleti Merkezi
- Provider'lar platform-managed olacak
- SMS provider adapter yapısı korunacak
- Tenant event bazlı bildirim açık/kapalı ayarlarını config panel üzerinden yönetebilir; şablon yönetim UI/API sonraki slice'tır
- Marketing e-posta/SMS transactional bildirimlerden ayrı ele alınır

Müşteri e-posta event'leri:

- Sipariş alındı / ödeme başarılı
- Sipariş kargoya verildi
- İptal/iade talebi alındı
- İptal/iade talebi onaylandı veya reddedildi

Hazır:

- Commerce-owned customer email v1: `ORDER_PAID`, `ORDER_SHIPPED`, `ORDER_REQUEST_CREATED`, `ORDER_REQUEST_APPROVED`, `ORDER_REQUEST_REJECTED`
- TR/EN seed template'ler ve `commerce_notification_outbox`
- Platform-managed email provider ile commit sonrası immediate send
- `commerce.notifications.email.enabled` global toggle ve `commerce.notifications.email.<event>.enabled` event override davranışı
- Tenant admin notification outbox liste/detay ekranı, tekil manual retry ve 15 dakikalık automatic retry job

Kalan:

- Transactional SMS
- Admin email alerts
- Tenant admin template management UI/API

Müşteri SMS event'leri:

- Sipariş alındı
- Sipariş kargoya verildi
- İptal/iade talebi sonucu

Admin e-posta event'leri:

- Yeni sipariş
- İptal/iade talebi
- Ödeme/iade işlem hatası

MVP dışında:

- Marketing e-posta/SMS
- Admin SMS bildirimi
- Ek SMS provider adapter'ları

### 3.10 Promosyonlar

MVP'de promosyon motoru olmayacak.

Kapsam:

- Ürün bazlı indirim gösterimi `firstPrice + price` ile yapılır
- Sale gibi kampanya grupları koleksiyon ile yönetilir

MVP dışında:

- Kupon kodları
- Sepet indirimi
- Yüzdelik/sabit tutar indirimi
- Ürün/kategori kısıtları
- Kullanım limiti

### 3.11 Admin Operasyonları

MVP'de hafif operasyonel satış özeti bulunacak. Gelişmiş analytics Faz 2'ye kalacak.

Kapsam:

- Commerce dashboard
- Bugünkü sipariş sayısı ve ciro
- Son 7 gün sipariş sayısı ve ciro
- Bekleyen iptal/iade talepleri
- Kargoya verilmesi gereken siparişler
- Başarısız ödeme denemeleri
- Düşük stok uyarıları
- Sipariş listesi ve detay
- İptal/iade talepleri listesi
- Payment attempts / başarısız ödeme denemeleri

Hazır:

- Read-only dashboard: bugünkü sipariş/ciro, son 7 gün sipariş/ciro, attention order count, başarısız ödeme denemesi count
- Sipariş listesi ve detay görünümü
- Sipariş status transition aksiyonları: Preparing, Shipped, Delivered
- Manuel fulfillment bilgisi: kargo firması, takip numarası, opsiyonel takip URL'i
- Status history timeline ve history satırına bağlı opsiyonel iç notlar
- Read-only payment attempt history
- İptal/iade talepleri listesi, detay ekranı ve approve/reject karar akışı
- Admin onayında iyzico üzerinden tam refund denemesi
- Commerce sidebar navigation

Kalan:

- Düşük stok uyarıları

MVP dışında:

- Gelişmiş satış analytics
- Ürün/kategori/koleksiyon performansı
- Conversion funnel
- Abandoned cart analytics

### 3.12 Config ve Legal Yönetimi

Config panel kısa runtime property'ler için kullanılır: boolean flag, number, kısa string ve seyrek değişen ayarlar.

Örnek config key'leri:

- `commerce.shipping.enabled`
- `commerce.shipping.standard_fee`
- `commerce.shipping.free_shipping_threshold`
- `commerce.order.number_prefix`
- `commerce.cart.max_quantity_per_product`
- `commerce.payment.provider`
- `commerce.payment.enabled`
- `commerce.payment.iyzico.api_key`
- `commerce.payment.iyzico.secret_key`
- `commerce.payment.iyzico.base_url`
- `commerce.notifications.email.enabled`
- `commerce.notifications.email.from_name`
- `commerce.notifications.email.reply_to`
- `commerce.notifications.sms.enabled`
- `commerce.notifications.sms.sender_name`
- `commerce.notifications.*.enabled`
- `commerce.legal.seller_*`

Legal yönetimi:

- Statik policy sayfaları CMS ile yönetilir.
- Checkout legal dokümanları Commerce admin tarafında versioning'li template olarak yönetilir. Hazır: list/detail/create/update/publish/archive/preview.
- Mesafeli satış sözleşmesi ve ön bilgilendirme formu template olarak tutulur.
- Template status: Draft, Published, Archived
- Published template immutable kabul edilir; yeni içerik için yeni draft/version publish edilir.
- Checkout exact language davranır; checkout dilinde published template veya zorunlu seller config yoksa ödeme bloklanır.
- Placeholder desteği `TemplateVariableRenderer` ile customer, adres snapshot, checkout items/totals, shipping ve seller config kaynaklarından beslenir.
- Checkout'ta sipariş verisiyle render edilmiş plain text metin gösterilir ve her doküman için müşteri onayı alınır.
- Payment attempt create isteği template UID/version/accepted payload'ını doğrular; eksik, stale veya false acceptance ödeme denemesi oluşturmaz.
- Sipariş finalization anında rendered legal snapshot, content hash ve acceptance metadata order üzerinde saklanır.
- Hukuki metin içeriği Craftive tarafından otomatik üretilmez; tenant/proje bazlı onaylı şablon girilir.

### 3.13 Storefront

Commerce storefront, `storefront-nextjs` kopyasından arındırılan bağımsız `commerce-ui` Next.js app olarak ilerliyor. Mevcut CMS renderer/theme bağı söküldü; tasarım ileride tenant/theme ihtiyacına göre yeniden yapılabilecek şekilde minimal tutuluyor.

Kararlar:

- Core CMS/runtime contract korunacak.
- Commerce akışları `commerce-ui` içinde ince API/state katmanlarıyla geliştirilecek.
- Tenant/theme katmanı marka deneyimi, görsel tasarım ve sayfa kompozisyonunu özelleştirecek.
- Cart, checkout, payment callback, account/order history gibi kritik davranışlar ortak commerce contract'a bağlı kalacak.

Hazır:

- `/[lang]` locale routing, tenant header foundation, minimal app shell ve skeleton route'lar
- Quiet Retail minimal design skeleton
- Typed cart API client, `localStorage` cart token handling, cart provider, header item count badge ve `/[lang]/cart` read/mutation wiring
- Product listing/search route, Product detail delivery client, `/[lang]/products/[productUid]` real product render, variant/quantity selection ve real variant add-to-cart
- Customer auth/account foundation: login/register/logout, refresh-cookie restore, memory-only access token state, read-only profile summary ve cart merge state update
- Address book, checkout, payment return ve order history UI foundation
- Checkout legal document görüntüleme/onay akışı ve order detail legal snapshot görüntüleme

Kalan:

- Tenant/theme final redesign
- Production hardening ve uçtan uca UX polish

MVP storefront kapsamı:

- Ürün listeleme
- Ürün detay
- Cart drawer
- Sepet sayfası
- Checkout
- Sipariş onay sayfası
- Account / sipariş geçmişi
- Koleksiyon/kampanya sayfaları

## 4. MVP Dışında Kalan Büyük Başlıklar

- Guest checkout
- Telefon OTP doğrulaması
- Wishlist
- Stok rezervasyonu
- Sepet expiration
- Cross-sell / önerilen ürünler
- Kupon/promosyon motoru
- Kargo sağlayıcı entegrasyonları
- Pickup / mağazadan teslim
- Havale/EFT
- Kapıda ödeme
- Gelişmiş return yönetimi
- Admin manuel sipariş oluşturma
- Marketing e-posta/SMS
- Admin SMS bildirimi
- Gelişmiş analytics
- Ek ödeme sağlayıcıları
- Authorize/capture
- Fatura / e-arşiv entegrasyonu
- ERP / stok senkronizasyonu
- Pazaryeri connector'ları
- Çoklu para birimi
- Global commerce desteği

## 5. Premium / AI Add-on Fikirleri

AI ve ileri otomasyon özellikleri MVP dışında kalır. Bu başlık MVP'yi kilitlemez.

Olası premium özellikler:

- AI Catalog Studio
- Ürün açıklaması ve SEO önerileri
- Import Copilot
- Catalog Quality Score
- Campaign Assistant
- Visual Studio
- Commerce automation rules
- Abandoned cart recovery
- Smart recommendations / cross-sell
- Advanced analytics
- Global commerce pack

Ticari ayrım:

- Core commerce delivery: fixed-price proje
- AI/automation layer: aylık premium add-on

## 6. Roadmap

### Faz 0: Hazırlık

- Commerce module boundary kararlarını dokümante et
- Product Catalog genişletme planını hazırla
- Config key listesini uygulama öncesi gözden geçir
- iyzico, Postmark ve İleti Merkezi operasyon modelini hazırla
- Demo tenant içerik yönünü ve storefront fork stratejisini hazırla

### Faz 1: Commerce MVP

- Product Catalog commerce hazırlığı
- Customer account
- Cart
- Checkout
- iyzico ödeme
- Siparişler
- İptal/iade talebi
- Manual fulfillment
- Transactional bildirimler
- Admin operasyon özeti
- Referans storefront commerce akışları

### Faz 2: Operasyon ve Growth

- Guest checkout
- OTP
- Wishlist
- Promosyon motoru
- Kargo sağlayıcı entegrasyonu
- Pickup
- Gelişmiş iade/return
- Marketing bildirimler
- Abandoned cart
- Cross-sell
- Gelişmiş analytics

### Faz 3: AI & Otomasyon

- AI Catalog Studio
- Import Copilot
- Catalog Quality Score
- Campaign Assistant
- Visual Studio
- Operation Assistant

### Faz 4: Entegrasyonlar & Ölçek

- Ek ödeme sağlayıcıları
- Authorize/capture
- Fatura/e-arşiv
- ERP/stok senkronizasyonu
- Pazaryeri connector'ları
- Çoklu para birimi
- Global commerce desteği

## 7. Açık Konular

MVP'yi kilitleyen açık konu kalmadı.
