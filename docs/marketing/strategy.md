# Craftive — Strateji Dokümanı

> Son güncelleme: 2026-02-11

---

## İçindekiler

1. [Vizyon ve İş Modeli](#1-vizyon-ve-iş-modeli)
2. [Services → Product Geçiş Stratejisi](#2-services--product-geçiş-stratejisi)
3. [Referans: SAP Commerce Cloud (Hybris)](#3-referans-sap-commerce-cloud-hybris)
4. [Pozisyonlama ve Mesajlaşma](#4-pozisyonlama-ve-mesajlaşma)
5. [Çözüm Paketleri ve Fiyatlandırma](#5-çözüm-paketleri-ve-fiyatlandırma)
6. [Modül Öncelik Roadmap'i](#6-modül-öncelik-roadmapi)
7. [Satış Stratejisi](#7-satış-stratejisi)
8. [Go-to-Market Planı](#8-go-to-market-planı)
9. [SEO ve İçerik Stratejisi](#9-seo-ve-içerik-stratejisi)
10. [Ücretli Reklam Stratejisi](#10-ücretli-reklam-stratejisi)
11. [Metrikler ve KPI'lar](#11-metrikler-ve-kpılar)
12. [Bütçe Önerisi](#12-bütçe-önerisi)

---

## 1. Vizyon ve İş Modeli

### Platform Vizyonu

Craftive, **müşteri ihtiyaçlarına göre modül bazlı özelleştirilebilir proje çözümleri** sunan bir platformdur.

Geleneksel ajans yaklaşımının aksine her proje sıfırdan yazılmaz. Her proje, aynı güçlü altyapının farklı bir konfigürasyonudur.

```
Müşteri A              Müşteri B              Müşteri C
Blog Sitesi            Ajans Yönetimi         HR Portali
───────────────        ───────────────        ───────────────
core                   core                   core
media                  project_mgmt           personnel
pagebuilder            client_portal          leave_mgmt
component_lib          media                  notifications
                                              forms
───────────────        ───────────────        ───────────────
1.500 TL/ay            2.000 TL/ay            2.500 TL/ay
```

Aynı platform, aynı altyapı — sadece farklı modüller aktif.

### İki Aşamalı İş Modeli

```
AŞAMA 1 (Şu An)                     AŞAMA 2 (İleride)
────────────────────────────        ────────────────────────────
Özelleştirilebilir Proje            SaaS Ürün Satışı
Çözümleri Sun                       (Hazır Paketler)

→ Müşteriye özel proje yap          → Aynı modüller self-serve
→ Proje başı ücret al               → Aylık abonelik
→ Her proje modül olarak inşa et    → Satış otomatik hale gelir
→ Gerçek ihtiyaçları öğren         → PMF zaten kanıtlanmış
```

### Temel Kural: Modül Disiplini

Her proje için yazılan her özellik, ileride başka müşterilere de sunulacak şekilde **standart modül** olarak inşa edilir.

```
❌ YANLIŞ: Müşteriye özel, tekrar kullanılamaz kod
✅ DOĞRU:  Standart modül → aktif et → yapılandır → teslim et

Ajans A için project_management: 3 hafta geliştirme
Ajans B için aynı modül        : 2 gün yapılandırma
Ajans C için aynı modül        : 2 gün yapılandırma  ← saf kâr
```

---

## 2. Services → Product Geçiş Stratejisi

### Strateji Özeti

> Müşteriye özel iş yaparken öğrendiklerini paketleyip herkese satmaya başlarsın.

Dünyaca tanınan ürünler bu yolla doğdu:

| Şirket | Başlangıç | Sonuç |
|--------|-----------|-------|
| **Basecamp** | 37signals ajans işi yapıyordu, iç proje aracı yaptı | Dünyanın en bilinen proje yönetim aracı |
| **Slack** | Tiny Speck oyun şirketinin iç iletişim aracıydı | $27 milyar değerlemeyle Salesforce'a satıldı |
| **GitHub** | Logical Awesome'ın iç geliştirme aracıydı | Microsoft tarafından $7.5 milyara satın alındı |

### Üç Faz

```
FAZ 1 — SERVİS MODU (0-18 Ay)
────────────────────────────────────────────────────────────
Her müşteri projesi = Craftive modülü olarak inşa et
Gelir   : Kurulum ücreti + aylık platform ücreti
Öğrenme : Müşteri gerçek ihtiyaçlarını söyler ve parasını öder
Risk    : Düşük — nakit akışı baştan başlar

FAZ 2 — GEÇİŞ (Sinyal geldiğinde)
────────────────────────────────────────────────────────────
Tetikleyici: Aynı modülü 3+ farklı müşteri istedi
Aksiyon   : O modül artık standart paket, custom kabul etme
Sonuç     : Yeni müşteriye "aktif ediyoruz" diyorsun

FAZ 3 — ÜRÜN MODU (18+ Ay)
────────────────────────────────────────────────────────────
Hazır paketler self-serve satılır
Custom geliştirme azalır, recurring gelir baskın hale gelir
Platform olgunlaşır, partner ekosistemi oluşur
```

### Geçiş Sinyalleri

Faz 2'ye geçmek için şu sinyallerden birini bekle:

- Aynı modülü 3 farklı müşteri istedi
- Yeni müşteri için kurulum eforu 2 güne indi
- Müşteri "bunu başkası da kullanır mı?" dedi

### Neden Önce Servis?

```
Hemen SaaS yap         vs.    Önce Servis
──────────────────────         ──────────────────────
6 ay kodla                     Müşteri parasını öder
Piyasaya çık                   Gerçek ihtiyacı öğren
Kimse almaz                    Modülü doğrula
Neden? Yanlış özellik          Sonra paketle ve sat

↑ Çoğu SaaS'ın başarısız        ↑ Craftive'ın yolu
  olma nedeni
```

---

## 3. Referans: SAP Commerce Cloud (Hybris)

### Neden Hybris Referans Alındı?

Craftive'ın mimarisi, Hybris'in temel felsefesini modern stack ile hayata geçiriyor.

```
Hybris (1997-bugün)              Craftive
──────────────────────           ──────────────────────
Extensions (modüller)        ←→  Modules
Accelerators (paketler)      ←→  Solution Templates
ImpEx (veri yönetimi)        ←→  Flyway migrations
Backoffice (admin UI)        ←→  Angular Admin Panel
OCC API (headless)           ←→  CMS Delivery API
Database-per-deployment      ←→  Database-per-tenant
```

### Hybris'in İş Stratejisi ve Craftive İçin Dersler

**Ders 1: Accelerator Modeli**
Hybris, sektöre özel hazır başlangıç paketleri satar:
- B2C Accelerator → Perakende sitesi
- B2B Accelerator → Kurumsal sipariş yönetimi
- Financial Accelerator → Bankacılık ürünleri

Craftive karşılığı: **Solution Templates** (Paket 1-5).
Her paket = pre-configured modül seti + demo data + yapılandırma.

**Ders 2: Her Şey Modül, Hiçbir Şey Core'a Dokunmaz**
Hybris'te özelleştirme her zaman extension ile yapılır, core değiştirilmez.
Craftive'ta: her özellik modül, core değişmez. Modül disiplini.

**Ders 3: Partner Ekosistemi = Ölçek Çarpanı**
Hybris'in gerçek büyümesi partner ağından geldi:
- SAP lisans satar
- Accenture, Deloitte gibi partnerlar implement eder
- Müşteri her ikisine de öder

İleride: "Certified Craftive Partner" programı.

### Piyasadaki Boşluk

```
SAP Commerce Cloud                Craftive Fırsatı
──────────────────────            ──────────────────────
500K$+/yıl lisans                 15-40K TL kurulum + 1.5-4K TL/ay
12-24 ay implementasyon           2-8 hafta kurulum
50 kişilik SAP ekibi              1-3 kişilik ekip
Sadece Fortune 500                KOBİ + Orta ölçek + Ajanslar
Türkiye'de 5-10 müşteri           Binlerce potansiyel müşteri
```

**Craftive, Hybris'in ulaşamadığı segmenti alıyor.**

---

## 4. Pozisyonlama ve Mesajlaşma

### Temel Mesaj (Türkiye)

**"Her işletme için özelleştirilebilir dijital çözümler. Tek platform, sınırsız yapılandırma."**

- Blog sitesinden HR portalına, e-ticaretten proje yönetimine kadar
- Her müşteri verisi tamamen izole, KVKK uyumlu
- Kurumsal altyapı, KOBİ fiyatı

### Temel Mesaj (Global)

**"Configurable business platform. Enterprise architecture, agency-friendly pricing."**

- One platform, unlimited configurations
- True database-per-tenant isolation
- From blog to ERP — activate only what you need

### Rakip Farklılaştırması

| Rakip | Onların Sınırı | Craftive Avantajı |
|-------|---------------|---------------------|
| Contentful / Strapi | Sadece CMS, iş süreçleri yok | CMS + proje + HR + e-ticaret tek platformda |
| WordPress | Güvenlik riskleri, teknik borç | Modern stack, database izolasyonu, API-first |
| SAP Hybris | 500K$+, 1-2 yıl, Fortune 500 için | Aynı felsefe, KOBİ bütçesinde, haftalar içinde |
| Monday / Jira | Proje var, içerik/site yok | Proje + içerik + site tek yerde |
| Özel Geliştirme | Her proje sıfırdan, bakımı zor | Hazır altyapı, hızlı teslim, sürdürülebilir |

### Değer Önerileri (Segmente Göre)

**Dijital Ajanslar:**
- Müşteri projelerini haftalar içinde teslim et
- Her müşterinin verisi tamamen izole
- Bir panel, tüm müşteriler

**KOBİ'ler:**
- Blog + HR + proje takip tek platformda
- SAP gibi kurumsal kalite, KOBİ bütçesiyle
- Türkçe destek, KVKK uyumu

**E-ticaret Firmaları:**
- Ürün kataloğu + içerik + sipariş yönetimi
- Headless API ile mevcut sitene entegre
- Responsive medya, ürün varyantları

---

## 5. Çözüm Paketleri ve Fiyatlandırma

### Fiyatlandırma Modeli

```
Kurulum Ücreti (one-time)    +    Aylık Platform Ücreti
        ↑                                  ↑
Tenant kurulum, içerik girişi,      Hosting, destek,
özelleştirme, eğitim                güncellemeler
```

Faz 1'den itibaren recurring gelir oluşur.
12. ayda 5 müşteri = **~120.000 TL/yıl otomatik gelir.**

### Paket 1 — Blog & Kurumsal Site ✅ BUGÜN SALILABİLİR

```
Modüller : core + media + pagebuilder + component_library
Hedef    : Hukuk bürosu, danışmanlık, kurumsal firma, kişisel marka
Kurulum  : 15.000 TL
Aylık    : 1.500 TL
```

### Paket 2 — E-ticaret Platformu

```
Modüller : core + media + product + order_management + forms
Hedef    : Online mağaza, ürün kataloğu, B2C/B2B satış
Kurulum  : 25.000 TL
Aylık    : 2.500 TL
Hazır    : Katman 3 modülleri tamamlanınca (Ay 7-9)
```

### Paket 3 — Ajans Yönetim Sistemi

```
Modüller : core + project_management + client_portal + media + forms
Hedef    : Dijital ajanslar, tasarım firmaları, danışmanlık şirketleri
Kurulum  : 20.000 TL
Aylık    : 2.000 TL
Hazır    : Katman 2 modülleri tamamlanınca (Ay 3-6)
```

### Paket 4 — Şirket İçi HR & Operasyon Portali

```
Modüller : core + personnel + leave_management + notifications + forms + announcements
Hedef    : 10-150 kişilik KOBİ'ler, şirket içi intranet ihtiyacı
Kurulum  : 20.000 TL
Aylık    : 2.500 TL
Hazır    : Katman 2 modülleri tamamlanınca (Ay 3-6)
```

### Paket 5 — Tam İşletme Platformu

```
Modüller : Tüm modüller, tam özelleştirme
Hedef    : Kurumsal, birden fazla departman ihtiyacı olan firmalar
Kurulum  : 40.000+ TL
Aylık    : 4.000+ TL
Hazır    : Katman 2-3 tamamlanınca (Ay 7-9)
```

### Yıllık Gelir Projeksiyonu (5 Müşteri)

| Müşteri | Kurulum | Aylık | Yıllık Platform |
|---------|---------|-------|-----------------|
| Blog/Kurumsal × 2 | 30.000 TL | 3.000 TL | 36.000 TL |
| Ajans OS | 20.000 TL | 2.000 TL | 24.000 TL |
| HR Portal | 20.000 TL | 2.500 TL | 30.000 TL |
| E-ticaret | 25.000 TL | 2.500 TL | 30.000 TL |
| **TOPLAM** | **95.000 TL** | **10.000 TL/ay** | **120.000 TL/yıl** |

---

## 6. Modül Öncelik Roadmap'i

### Mevcut Durum — Baseline

| Modül | Durum | Hangi Paket |
|-------|-------|-------------|
| `core` | ✅ Hazır | Tümü |
| `pagebuilder` | ✅ Hazır | Paket 1 |
| `media` | ✅ Hazır | Paket 1, 2, 3 |
| `component_library` | ✅ Hazır | Paket 1 |
| `product` | ✅ Hazır | Paket 2 |

**Paket 1 (Blog & Kurumsal) bugün satılabilir.**

### Katman 1 — Platform Temeli (Ay 1-2)

Her proje tipinde kullanılır. Diğer modüllerin altyapısı.

| Modül | Ne Yapar | Hangi Paketlerde |
|-------|----------|-----------------|
| `forms` | Dinamik form motoru — iletişim, brief, izin, iade | Tümü |
| `notifications` | In-app + email bildirimler, trigger sistemi | 3, 4, 5 |

### Katman 2 — Yeni Segmentler Aç (Ay 3-6)

Paket 3 ve Paket 4'ü mümkün kılan modüller.

| Modül | Ne Yapar | Hangi Paket |
|-------|----------|-------------|
| `project_management` | Proje → Milestone → Görev, atama, kanban, deadline | 3, 5 |
| `personnel` | Çalışan profili, departman, pozisyon, şirket rehberi | 4, 5 |
| `leave_management` | İzin talebi, onay akışı, bakiye, Türk iş hukuku uyumu | 4, 5 |

### Katman 3 — Değer Artır (Ay 7-9)

Mevcut müşterilere upsell + Paket 2 ve 5'i tamamlar.

| Modül | Ne Yapar | Hangi Paket |
|-------|----------|-------------|
| `order_management` | Sipariş takibi, durum yönetimi, fatura PDF | 2, 5 |
| `client_portal` | Ajansın son müşterisine deliverable + onay ekranı | 3, 5 |
| `announcements` | Şirket duyurusu, departman hedefleme, okundu onayı | 4, 5 |

### Katman 4 — Platform Olgunluğu (Ay 10-15)

SaaS moduna geçişi hızlandıran modüller.

| Modül | Ne Yapar |
|-------|----------|
| `analytics` | Sayfa görüntüleme, içerik performansı, kullanıcı davranışı |
| `reports` | Modül bazlı raporlar, CSV/PDF export |
| `survey` | Anket oluşturma, sonuç analizi |
| `integrations` | Webhook, Zapier/Make bağlantısı |
| `automations` | Olay tetikleyici → aksiyon kuralları |

### Genel Zaman Çizelgesi

```
Ay 1-2       Ay 3-6            Ay 7-9              Ay 10-15
────────────┬─────────────────┬───────────────────┬────────────────
forms       │ project_mgmt    │ order_management   │ analytics
notif.      │ personnel       │ client_portal      │ reports
            │ leave_mgmt      │ announcements      │ automations
────────────┴─────────────────┴───────────────────┴────────────────
Paket 1     │ Paket 3 & 4     │ Paket 2 & 5        │ SaaS modu
satılıyor   │ satılıyor       │ satılıyor          │ olgunlaşıyor
```

---

## 7. Satış Stratejisi

### Satış Modeli: Danışmanlık Tabanlı Satış

Faz 1'de self-serve değil, **danışmanlık bazlı satış** yapılır:

```
1. Potansiyel müşteri ile keşif görüşmesi (30-45 dk)
   "İhtiyacınız nedir? Hangi sorunları çözmek istiyorsunuz?"
      ↓
2. Paket önerisi + demo
   "Sizin için Paket 3 uygun, şunu şunu aktif edeceğiz"
      ↓
3. Teklif: Kurulum ücreti + aylık platform ücreti
      ↓
4. Kurulum ve teslim (2-8 hafta)
      ↓
5. Müşteri eğitimi + destek
      ↓
6. Upsell: "Şimdi client_portal modülü ekleyelim mi?"
```

### Satış Kanalları

**Birincil (Faz 1):**
- Kişisel ağ ve referanslar
- LinkedIn outreach (ajans sahipleri, KOBİ yöneticileri)
- Demo talebi formu (landing page)

**İkincil (Faz 2):**
- Ajans partner programı (reseller)
- Google Ads → landing page → demo talebi
- Vaka çalışmaları → inbound lead

### Ajans Partner Programı (Faz 2)

```
Tier      Proje Adedi   Komisyon   Destek
─────────────────────────────────────────
Silver    1-3 proje     %15        Community
Gold      4-10 proje    %20        Email
Platinum  10+ proje     %25        Dedicated
```

### Satış Araçları

- **CRM:** HubSpot veya Pipedrive
- **Demo:** Canlı demo ortamı + Loom kayıtları
- **Teklif:** Paket bazlı standart teklif şablonu
- **Onboarding:** Kurulum checklist + video eğitim serisi

---

## 8. Go-to-Market Planı

### Faz 1 — İlk Müşteriler (0-3 Ay)

**Hedef:** 3-5 ücretli proje, Paket 1 odaklı.

- [ ] Landing page yayına al (Türkçe)
- [ ] Demo talebi formu kur
- [ ] Kişisel ağa duyur, referans iste
- [ ] LinkedIn'de "ne yaptık, nasıl yaptık" içerikleri paylaş
- [ ] İlk 3 projeyi al, vaka çalışması hazırla

### Faz 2 — Büyüme (3-9 Ay)

**Hedef:** Paket 3 & 4 ile yeni segmentler, 10-20 aktif müşteri.

- [ ] Katman 2 modüllerini tamamla
- [ ] Ajans ve KOBİ segmentine outreach başlat
- [ ] İlk vaka çalışmalarını yayınla
- [ ] Google Ads kampanyası başlat (TR)
- [ ] İlk partner ajansı bul

### Faz 3 — Ölçeklenme (9-18 Ay)

**Hedef:** 50+ müşteri, partner ekosistemi, SaaS moduna hazırlık.

- [ ] Tüm paketler satışta
- [ ] Partner programı aktif
- [ ] Product Hunt lansmanı
- [ ] İngilizce landing page + global SEO
- [ ] SaaS self-serve pilot (1-2 paket için)

---

## 9. SEO ve İçerik Stratejisi

### Hedef Anahtar Kelimeler (Türkiye)

**Proje/Çözüm odaklı (yüksek öncelik):**
- "özelleştirilebilir yazılım çözümleri"
- "kurumsal web sitesi yaptırma"
- "ajans yönetim yazılımı"
- "şirket içi portal yazılımı"
- "izin yönetim sistemi türkiye"
- "proje takip yazılımı kobi"

**Platform/CMS odaklı:**
- "headless cms türkçe"
- "multi tenant cms"
- "kvkk uyumlu cms"
- "contentful alternatifi türkçe"

**Rakip alternatifi:**
- "sap hybris alternatifi türkiye"
- "wordpress yerine ne kullanmalı"
- "kolay ik alternatifi"

### Global SEO Anahtar Kelimeler

- "configurable business platform"
- "multi tenant cms platform"
- "headless cms for agencies"
- "sap commerce cloud alternative"
- "enterprise cms affordable"

### İçerik Planı

**Blog kategorileri:**
1. **Vaka Çalışmaları** — "X firması Craftive ile Y sorunu çözdü"
2. **Rehberler** — "Ajansınız için doğru CMS", "İzin yönetimi nasıl dijitalleştirilir"
3. **Teknik** — "Database-per-tenant nedir", "Multi-tenant mimari avantajları"
4. **Karşılaştırma** — "Craftive vs SAP Hybris", "Craftive vs WordPress"

**Takvim:**
- Haftalık: 2 blog yazısı (TR)
- Aylık: 1 vaka çalışması veya detaylı rehber
- Çeyreklik: 1 whitepaper / karşılaştırma içeriği

**Lead Magnet'ler:**
- "KOBİ'ler için Dijital Dönüşüm Rehberi" (PDF)
- "SAP Hybris vs Uygun Fiyatlı Alternatifler" (Karşılaştırma)
- "KVKK Uyumlu Yazılım Seçimi Kontrol Listesi" (Checklist)

### Teknik SEO

- Core Web Vitals optimize landing page
- ✅ Schema markup: SoftwareApplication, Organization, FAQPage — JSON-LD, locale layout'ta server-side
- ✅ Çok dilli sitemap (TR + EN) — `landing/app/sitemap.ts`
- ✅ Hreflang alternates + canonical — `generateMetadata` ile her locale için
- ✅ Open Graph + Twitter Card — OG image `next/og` ile dinamik üretiliyor
- ✅ robots.txt — `landing/app/robots.ts`
- Backlink: Türk tech blogları, partner ajanslar, sektör dizinleri

> Detaylı implementation: [`landing-seo.md`](landing-seo.md)

---

## 10. Ücretli Reklam Stratejisi

> Faz 1'de ücretli reklam **gerekmez** — kişisel ağ ve organik ile başla.
> Faz 2'de (3-6. ay) aşağıdaki kampanyaları devreye al.

### Google Ads (Türkiye)

**Kampanya 1 — Proje/Çözüm Arama:**
- Bütçe: 5.000-8.000 TL/ay
- Anahtar: "yazılım çözümü", "kurumsal portal", "ajans yönetim sistemi"
- Landing: Paket sayfası + demo talebi formu

**Kampanya 2 — Rakip Alternatifleri:**
- Bütçe: 2.000-3.000 TL/ay
- Anahtar: "wordpress alternatifi", "kolay ik alternatifi", "sap alternatifi"
- Landing: Karşılaştırma sayfası

**Kampanya 3 — Retargeting:**
- Landing page ve pricing sayfası ziyaretçileri
- "Demo talep edin" CTA

### LinkedIn Ads (Faz 2)

- Hedef: Ajans sahipleri, KOBİ yöneticileri, CTO/Operasyon Müdürleri (Türkiye)
- Format: Sponsored Content (vaka çalışmaları) + Lead Gen Forms
- Bütçe: $500-800/ay

---

## 11. Metrikler ve KPI'lar

### Faz 1 (0-6 Ay) — Servis Modu

| Metrik | Hedef |
|--------|-------|
| Ücretli proje | 3-5 |
| Aylık platform geliri (MRR) | 5.000-8.000 TL |
| Ortalama proje süresi | < 6 hafta |
| Müşteri memnuniyeti (NPS) | 40+ |

### Faz 2 (6-12 Ay) — Büyüme

| Metrik | Hedef |
|--------|-------|
| Aktif müşteri | 15-25 |
| MRR | 25.000-40.000 TL |
| Kurulum → aylık dönüşüm | %100 (her kurulumda platform ücreti) |
| Upsell oranı | %30 (ek modül alan müşteri) |

### Faz 3 (12-18 Ay) — Ölçeklenme

| Metrik | Hedef |
|--------|-------|
| Aktif müşteri | 50+ |
| MRR | 80.000-120.000 TL |
| Churn | < %5/ay |
| Partner sayısı | 3-5 |

---

## 12. Bütçe Önerisi

### Faz 1 (Ay 1-3) — Minimum Yatırım

```
Kanal                   Aylık          Açıklama
─────────────────────────────────────────────────
Landing Page (one-time) 2.000 TL       Tek seferlik
CRM Araçları            500 TL         HubSpot free / Pipedrive
LinkedIn (organik)      0 TL           Zaman yatırımı
─────────────────────────────────────────────────
TOPLAM                  ~500 TL/ay
```

### Faz 2 (Ay 3-9) — Büyüme Yatırımı

```
Kanal                   Aylık          Yıllık
──────────────────────────────────────────────
Google Ads (TR)         6.000 TL       72.000 TL
LinkedIn Ads            4.000 TL       48.000 TL
İçerik Üretimi          3.000 TL       36.000 TL
SEO Araçları            800 TL         9.600 TL
CRM + Araçlar           1.000 TL       12.000 TL
──────────────────────────────────────────────
TOPLAM                  14.800 TL/ay   177.600 TL/yıl
```

### Faz 3 (Ay 9-18) — Ölçek Yatırımı

```
Kanal                   Aylık
────────────────────────────────────────
Google Ads (TR + Global) 15.000 TL
LinkedIn Ads             8.000 TL
İçerik + SEO             5.000 TL
Etkinlik / PR            3.000 TL (ort.)
────────────────────────────────────────
TOPLAM                   ~31.000 TL/ay
```
