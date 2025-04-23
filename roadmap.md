# CMS Projesi Roadmap

## Proje Özeti
Modern, sürdürülebilir ve genisletilebilir bir CMS sistemi gelistirme projesi. Spring Boot ve Angular 17 teknolojileri kullanilarak kurumsal sirketlerin tanitim sitelerini kolayca yönetebilecekleri, dinamik sayfa yapilari ve komponentlerle özellestirilebilir bir platform olusturulacaktir.

## Teknoloji Yigini

### Backend
- **Java 21**: Modern Java özelliklerinden faydalanma (Records, Pattern Matching, Text Blocks)
- **Spring Boot 3.2+**: Modern Spring uygulamasi
- **Spring Security**: JWT tabanli kimlik dogrulama
- **Spring Data JPA**: Veritabani islemleri
- **Hibernate ORM**: Entity-iliskisel esleme ve otomatik sema yönetimi (ddl-auto=update)
- **MySQL**: Ana veritabani
- **Spring Cache**: Yerlesik önbellekleme çözümü

### Frontend
- **Angular 19**: Admin panel
- **TypeScript 5.x**: Tip güvenligi
- **Angular Services + RxJS**: State yönetimi (BehaviorSubject pattern)
- **Angular Material veya PrimeNG**: UI komponentleri
- **TailwindCSS**: Utility-first styling
- **Angular CDK Drag/Drop**: Sürükle-birak komponent düzenleme
- **RxJS 7+**: Reaktif programlama

### Template Engine (Önyüz Için)
- **Thymeleaf**: Server-side rendering
- **Custom Tag Libraries**: Dinamik içerik ve komponentler için

## Dinamik Sayfa ve Komponent Yapisi

CMS sistemimizde, asagidaki ekstra özelliklerle daha esnek bir sayfa yönetimi saglanacaktir:

1. **Sayfa Kategorileri**:
    - Statik sayfalar (Hakkimizda, Vizyon-Misyon, Degerlerimiz)
    - Anasayfa
    - Ürün sayfalari
    - Ürün detay sayfalari
    - Ürün kategori sayfalari
    - Iletisim sayfalari
    - Blog sayfalari
    - vb.

2. **Template-Kategori Iliskisi**:
    - Her sayfa kategorisi için özel sablonlar
    - Kategori bazli varsayilan layout yapisi

3. **Dinamik Komponentler**:
    - Sayfaya eklenip çikarilabilen bilesenler
    - Sürükle-birak ile yeniden siralama
    - Her komponentin kendi içerik yönetimi
    - Responsive görünüm yönetimi

### 2. Temel Altyapi Gelistirme (3 Hafta)

#### Hafta 3-4: Backend Altyapisi
- [ ] MySQL veritabani semasi ve Hibernate migration'lari
- [ ] Generic base siniflar (BaseEntity, BaseService, BaseController)
- [ ] CRUD islemleri için ortak altyapi
- [ ] Exception handling mekanizmasi
- [ ] API response standardizasyonu
- [ ] Temel güvenlik yapilandirmasi (Spring Security + JWT)
- [ ] Temel logging yapilandirmasi

#### Hafta 5: Frontend Altyapisi
- [ ] Angular modül yapisi ve routing konfigürasyonu
- [ ] Angular Services + RxJS ile basit state yönetimi
- [ ] HTTP interceptors ve auth guards
- [ ] UI komponent kütüphanesi entegrasyonu
- [ ] Shared komponent ve direktifler
- [ ] Responsive design altyapisi

### 3. Core Modüllerin Gelistirilmesi (5 Hafta)

#### Hafta 6-7: Kullanici Yönetimi
- [ ] Kullanici entity ve DTO'lari
- [ ] Spring Security ve JWT entegrasyonu
- [ ] Rol tabanli yetkilendirme
- [ ] Kullanici yönetimi servisleri ve API'leri
- [ ] Kullanici yönetimi frontend komponentleri
- [ ] Kullanici profil yönetimi

#### Hafta 8-9: Sayfa Kategorileri ve Sablonlar
- [ ] Sayfa kategorileri modeli ve iliskileri
- [ ] Kategori-template iliski yönetimi
- [ ] Sayfa kategori yönetimi API'leri
- [ ] Template yönetim sistemi
- [ ] Sayfa kategorisi UI modülü
- [ ] Template CRUD islemleri

#### Hafta 10: Dinamik Komponent Altyapisi
- [ ] Komponent modeli ve entity yapisi
- [ ] Sayfa-komponent iliski yapisi
- [ ] Komponentlerin dinamik yüklenmesi
- [ ] Komponent meta-data yönetimi
- [ ] Komponent library olusturma
- [ ] Komponent pozisyon yönetimi

### 4. Sayfa ve Içerik Yönetimi (3 Hafta)

#### Hafta 11: Sayfa Içerik Yönetimi
- [ ] Sayfa entity ve DTO'lari (kategori iliskisi ile)
- [ ] Sayfa içerigi ve komponent yerlesimi
- [ ] Rich text editor entegrasyonu
- [ ] Içerik durumlari (draft, published)
- [ ] Sayfa meta bilgileri (title, description)
- [ ] Sayfa CRUD islemleri

#### Hafta 12: Komponent Içerik Editörü
- [ ] Komponent içerik editörü UI
- [ ] Farkli komponent tipleri için içerik formlari
- [ ] Komponent özelliklerine göre ayarlar
- [ ] Komponent önizleme
- [ ] Komponent validasyonu
- [ ] Responsive görünüm ayarlari

#### Hafta 13: Drag-Drop Sayfa Editörü
- [ ] Sürükle-birak sayfa komponent dizayni
- [ ] Komponent ekleme/çikarma islemleri
- [ ] Komponent siralama
- [ ] Komponent içerigi düzenleme
- [ ] Sayfa önizleme
- [ ] Layout yönetimi

### 5. Medya ve Entegrasyon (3 Hafta)

#### Hafta 14: Medya Yönetimi
- [ ] Dosya upload altyapisi
- [ ] Temel resim isleme
- [ ] Medya kitapligi UI
- [ ] Medya kategorilendirme
- [ ] Dosya browser ve picker
- [ ] Komponentlere medya entegrasyonu

#### Hafta 15: Template-Komponent Entegrasyonu
- [ ] Thymeleaf konfigürasyonu
- [ ] Komponent renderlari için custom tag'ler
- [ ] Sayfa sablonlari ve layout'lar
- [ ] Template önizleme
- [ ] Dinamik komponent renderlamasi
- [ ] Komponent parametrelerinin template'e aktarimi

#### Hafta 16: Içerik Önizleme ve Yayinlama
- [ ] Draft/Published versiyonlama
- [ ] Sayfa önizleme
- [ ] Zamanlanmis yayinlama
- [ ] Yayin is akisi
- [ ] Versiyon karsilastirma
- [ ] Degisiklikleri geri alma

### 6. Optimizasyon, Test ve Deployment (2 Hafta)

#### Hafta 17: Optimizasyon ve Test
- [ ] Spring Cache kullanimi
- [ ] Database indexleme ve query optimizasyonu
- [ ] Frontend bundle optimizasyonu
- [ ] Temel statik dosya caching
- [ ] URL yapilandirma (SEO-friendly URL'ler)
- [ ] Unit ve integration testler
- [ ] API dokümantasyonu (OpenAPI/Swagger)

#### Hafta 18: Dokümantasyon ve Deployment
- [ ] Kullanici kilavuzu ve dokümantasyon
- [ ] Deployment dokümanlari
- [ ] Production ortam konfigürasyonu
- [ ] MySQL veritabani kurulumu
- [ ] SSL sertifikasi (Let's Encrypt)
- [ ] Son güvenlik kontrolleri
- [ ] Launch ve test

## Java 21 Özellikleri Kullanimi

### Record Pattern Kullanimi (DTO'lar Için)
```java
// Komponent DTO tanimlari için
public record ComponentDTO(Long id, String title, JsonNode content, JsonNode settings) {}

// Sayfa DTO'su
public record PageDTO(
    Long id, 
    String title, 
    String slug, 
    PageCategoryDTO category,
    TemplateDTO template,
    List<PageComponentDTO> components
) {}

// Pattern matching
if (response instanceof ApiResponse<PageDTO>(PageDTO pageData, String message)) {
    // Sayfa ve komponentlerini isleme...
    for (var component : pageData.components()) {
        System.out.println("Component: " + component.title());
    }
}
```

### Text Blocks ile Daha Okunakli SQL
```java
@Query("""
    SELECT p FROM Page p
    JOIN FETCH p.category c
    LEFT JOIN FETCH p.template t
    WHERE p.status = :status
    AND c.slug = :categorySlug
    ORDER BY p.publishedAt DESC
    """)
List<Page> findByCategoryAndStatus(
    @Param("categorySlug") String categorySlug, 
    @Param("status") String status
);
```

### Switch Expressions ile Komponent Rendering
```java
String renderComponent(Component component) {
    return switch (component.getType().getCode()) {
        case "text_block" -> renderTextComponent(component);
        case "image" -> renderImageComponent(component);
        case "gallery" -> renderGalleryComponent(component);
        case "video" -> renderVideoComponent(component);
        case "product_list" -> renderProductListComponent(component);
        case "contact_form" -> renderContactFormComponent(component);
        default -> renderDefaultComponent(component);
    };
}
```

### Pattern Matching for Switch
```java
Object parseComponentContent(Component component) {
    return switch(component.getContent()) {
        case JsonNode json when json.has("text") -> 
            new TextContent(json.get("text").asText());
            
        case JsonNode json when json.has("images") ->
            parseImageGallery(json);
            
        case JsonNode json when json.has("videoUrl") ->
            new VideoContent(json.get("videoUrl").asText(), 
                            json.get("autoplay").asBoolean());
                            
        case JsonNode json when json.has("productIds") ->
            parseProductList(json);
            
        case null -> 
            new EmptyContent();
            
        default -> 
            new UnknownContent();
    };
}
```

## AI Özellikleri Kullanici Arayüzü Örnekleri

### Zengin Metin Editöründe AI Asistani
```
+------------------------------------------------------------------+
| Editör                                         | AI Asistani [?] |
+------------------------------------------------------------------+
| # Ana Baslik                                   |  Içerik Islemleri |
|                                               | +-----------------+
| Lorem ipsum dolor sit amet, consectetur       | | ? Iyilestir     |
| adipiscing elit. Nullam ac metus at risus     | | ? Uzat          |
| efficitur aliquam vel vel lectus. Donec       | | ? Özet çikar    |
| dapibus risus velit, nec euismod leo sodales  | | ? Yazim denetle |
| vel. Praesent eget suscipit lorem.            | +-----------------+
|                                               |  Ton Degistir      |
| * Liste ögesi 1                               | +-----------------+
| * Liste ögesi 2                               | | ? Profesyonel   |
| * Liste ögesi 3                               | | ? Resmi         |
|                                               | | ? Samimi        |
|                                               | | ? Teknik        |
|                                               | +-----------------+
|                                               |                    |
|                                               | [AI Ile Iyilestir] |
+------------------------------------------------------------------+
```

### SEO Önerileri Paneli
```
+------------------------------------------------------------------+
| SEO Analizi                                          Puan: 82/100 |
+------------------------------------------------------------------+
| ? Baslik uzunlugu: 58 karakter (Ideal)                           |
| ? Ana anahtar kelime baslikta geç kullanilmis                    |
| ? Meta açiklama: 145 karakter (Ideal)                            |
| ? Baslik hiyerarsisi dogru kullanilmis                           |
| ? Içerik 300 kelimeden kisa (Önerilen: min. 500 kelime)          |
| ? Iç baglantilar mevcut (3 adet)                                 |
| ? Alt metinler tüm görseller için mevcut                         |
| ? "kurumsal web sitesi" kelimesi yeterince kullanilmamis         |
|                                                                  |
| Önerilen Eylemler:                                               |
| 1. Içerigi uzatin (En az 200 kelime daha)                        |
| 2. Ana anahtar kelimeyi basligin basina tasiyin                  |
| 3. "kurumsal web sitesi" ifadesini 2-3 kez daha kullanin         |
|                                                                  |
| [AI ile Otomatik Düzelt]             [Önerileri Uygula]          |
+------------------------------------------------------------------+
```

### Otomatik Meta Bilgi Üretimi
```
+------------------------------------------------------------------+
| Sayfa Meta Bilgileri                                             |
+------------------------------------------------------------------+
| Meta Baslik:                                                     |
| [Kurumsal Web Sitesi Çözümleri - XYZ Sirketi]     [AI Öner ?]   |
|                                                                  |
| Meta Açiklama:                                                   |
| [Modern kurumsal web sitesi çözümleri ile is...]   [AI Öner ?]   |
|                                                                  |
| Anahtar Kelimeler:                                               |
| [kurumsal web sitesi, web çözümleri, dijital...]   [AI Öner ?]   |
|                                                                  |
| [Tüm Meta Bilgileri AI ile Olustur]   [Kaydet]                   |
+------------------------------------------------------------------+
```

### Görsel Analizi ve Alt Metin Önerileri
```
+------------------------------------------------------------------+
| Görsel Yönetimi                                                  |
+------------------------------------------------------------------+
| [Ofis Toplantisi.jpg]                                           |
|                                                                  |
| Alt Metin:                                                       |
| [Is profesyonelleri modern ofiste toplanti...]    [AI Üret ?]    |
|                                                                  |
| Otomatik Etiketler:                                              |
| [?] Is toplantisi  [?] Ofis  [?] Ekip çalismasi  [?] Kurumsal   |
|                                                                  |
| Önerilen Kullanim Yerleri:                                       |
| • Hakkimizda sayfasi                                             |
| • Kurumsal kültür bölümü                                         |
| • Takim tanitimi                                                 |
|                                                                  |
| [Düzenle] [Optimize Et] [Galeriye Ekle]                          |
+------------------------------------------------------------------+
```

## Gelecek Asamalar (Bu Roadmap'e Dahil Degil)

### SEO Optimizasyonlari
- URL yapisi iyilestirme
- Sitemap olusturma
- Structured data markup
- Canonical URL'ler

### Ileri Performans Optimizasyonlari
- Advanced caching stratejileri
- CDN entegrasyonu
- Ileri resim optimizasyonu
- Sayfa yüklenme hizi iyilestirmeleri

### Analytics ve Raporlama
- Google Analytics entegrasyonu
- Dashboard gelistirme
- Içerik performans raporlari

## Veritabani Semasi (Güncellenmis)

### Users ve Roles
```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE user_roles (
  user_id BIGINT,
  role_id BIGINT,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### Sayfa Kategorileri ve Sablonlar
```sql
CREATE TABLE page_categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE templates (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  folder_path VARCHAR(255) NOT NULL,
  description TEXT,
  active BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE category_templates (
  category_id BIGINT,
  template_id BIGINT,
  is_default BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (category_id, template_id),
  FOREIGN KEY (category_id) REFERENCES page_categories(id),
  FOREIGN KEY (template_id) REFERENCES templates(id)
);
```

### Dinamik Komponentler
```sql
CREATE TABLE component_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) NOT NULL UNIQUE,
  description TEXT,
  icon VARCHAR(100),
  editor_component VARCHAR(100) NOT NULL,
  renderer_component VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE components (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  type_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  content JSON,
  settings JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (type_id) REFERENCES component_types(id)
);
```

### Sayfalar ve Sayfa-Komponent Iliskisi
```sql
CREATE TABLE pages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  category_id BIGINT NOT NULL,
  template_id BIGINT,
  content TEXT,
  meta_title VARCHAR(255),
  meta_description VARCHAR(500),
  status VARCHAR(20) DEFAULT 'DRAFT',
  published_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT,
  FOREIGN KEY (category_id) REFERENCES page_categories(id),
  FOREIGN KEY (template_id) REFERENCES templates(id),
  FOREIGN KEY (created_by) REFERENCES users(id),
  FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE TABLE page_components (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  page_id BIGINT NOT NULL,
  component_id BIGINT NOT NULL,
  zone VARCHAR(100) NOT NULL,
  position INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (page_id) REFERENCES pages(id),
  FOREIGN KEY (component_id) REFERENCES components(id)
);
```

### Medya
```sql
CREATE TABLE media (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  file_name VARCHAR(255) NOT NULL,
  original_file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(500) NOT NULL,
  file_size BIGINT NOT NULL,
  file_type VARCHAR(100) NOT NULL,
  alt_text VARCHAR(255),
  title VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by BIGINT,
  FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE media_categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE media_category_items (
  media_id BIGINT,
  category_id BIGINT,
  PRIMARY KEY (media_id, category_id),
  FOREIGN KEY (media_id) REFERENCES media(id),
  FOREIGN KEY (category_id) REFERENCES media_categories(id)
);
```

## Kod Standartlari ve Best Practices

### Java/Spring Kod Standartlari
- **Java Code Conventions**: Oracle standartlari
- **Null-safety**: Optional kullanimi veya @Nullable/@NotNull annotations
- **Clean Code prensipler**: SOLID, DRY, KISS
- **Exception handling**: Standardize edilmis exception yapisi
- **Package structure**: Feature-based organization
- **Java Records**: DTO'lar için immutable veri yapilari

### Angular Kod Standartlari
- **Angular Style Guide**: Official Angular team guidelines
- **State management**: Basit service + RxJS pattern
- **Component design**: Smart/Presentational pattern
- **Module organization**: Feature modules + shared module
- **Performance practices**: Lazy loading modules

### Generic CRUD Yaklasimi
- Backend için generic base siniflar
- Frontend için reusable komponentler
- Ortak validasyon yaklasimi

## Basit Deployment Stratejisi
- **Bash script deployment**: Otomatik build ve deploy
- **Environment ayrimi**: Dev ve Prod ortamlari
- **Database yedekleme**: Otomatik MySQL yedekleme
- **Temel logging**: Rotating log files

## Proje Yönetimi
- **Basit Kanban yaklasimi**: GitHub Projects veya Trello
- **Issue tracking**: GitHub veya GitLab Issues
- **Documentation**: Markdown dosyalar (GitHub/GitLab repository içinde)
- **Code reviews**: Pull request process

## Backend Mimari Yapisi (Güncellenmis)

```
com.company.cms/
+-- config/                  # Konfigürasyon siniflari
+-- core/                    # Core utility siniflar
¦   +-- generic/             # Generic base siniflar
¦   +-- security/            # Güvenlik
¦   +-- util/                # Utility siniflar
+-- domain/                  # Domain modelleri
¦   +-- entity/              # JPA entity'leri
¦   ¦   +-- user/            # Kullanici-rol entity'leri
¦   ¦   +-- page/            # Sayfa ve kategori entity'leri
¦   ¦   +-- component/       # Komponent entity'leri
¦   ¦   +-- template/        # Sablon entity'leri
¦   ¦   +-- media/           # Medya entity'leri
¦   +-- dto/                 # DTO kayitlari (records)
¦   +-- mapper/              # Entity-DTO dönüsümleri
+-- exception/               # Custom exception'lar
+-- module/                  # Is modülleri
¦   +-- user/                # Kullanici yönetimi
¦   ¦   +-- controller/      # REST controller'lar
¦   ¦   +-- repository/      # JPA repository'ler
¦   ¦   +-- service/         # Service siniflari
¦   +-- page/                # Sayfa yönetimi
¦   ¦   +-- controller/      # REST controller'lar
¦   ¦   +-- repository/      # JPA repository'ler
¦   ¦   +-- service/         # Service siniflari
¦   +-- component/           # Komponent yönetimi
¦   ¦   +-- controller/      # REST controller'lar
¦   ¦   +-- repository/      # JPA repository'ler
¦   ¦   +-- service/         # Service siniflari
¦   +-- template/            # Sablon yönetimi
¦   ¦   +-- controller/      # REST controller'lar 
¦   ¦   +-- repository/      # JPA repository'ler
¦   ¦   +-- service/         # Service siniflari
¦   +-- media/               # Medya yönetimi
¦       +-- controller/      # REST controller'lar
¦       +-- repository/      # JPA repository'ler
¦       +-- service/         # Service siniflari
+-- renderer/                # Thymeleaf renderer siniflari
¦   +-- component/           # Komponent renderer'lari
¦   +-- page/                # Sayfa renderer'lari
+-- CMSApplication.java      # Ana uygulama
```

## Frontend Mimari Yapisi (Güncellenmis)

```
src/
+-- app/
¦   +-- core/                   # Core module
¦   ¦   +-- auth/               # Authentication
¦   ¦   +-- guards/             # Route guards
¦   ¦   +-- interceptors/       # HTTP interceptors
¦   ¦   +-- services/           # Core services
¦   ¦   +-- models/             # Ortak modeller
¦   +-- modules/                # Feature modules
¦   ¦   +-- dashboard/          # Dashboard module
¦   ¦   +-- user-management/    # Kullanici yönetimi
¦   ¦   +-- page-management/    # Sayfa yönetimi
¦   ¦   ¦   +-- components/     # Sayfa yönetim komponentleri
¦   ¦   ¦   +-- models/         # Sayfa modelleri
¦   ¦   ¦   +-- services/       # Sayfa servisleri
¦   ¦   ¦   +-- page-editor/    # Sayfa düzenleyici
¦   ¦   ¦       +-- components/ # Düzenleyici komponentleri
¦   ¦   ¦       +-- services/   # Düzenleyici servisleri
¦   ¦   +-- component-library/  # Komponent kütüphanesi
¦   ¦   ¦   +-- components/     # Komponent tipleri
¦   ¦   ¦   +-- models/         # Komponent modelleri
¦   ¦   ¦   +-- services/       # Komponent servisleri
¦   ¦   +-- template-management/# Sablon yönetimi
¦   ¦   +-- media-library/      # Medya kütüphanesi
¦   +-- shared/                 # Shared module
¦   ¦   +-- components/         # Ortak komponentler
¦   ¦   +-- directives/         # Direktifler
¦   ¦   +-- pipes/              # Pipe'lar
¦   ¦   +-- services/           # Ortak servisler
¦   +-- app-routing.module.ts
¦   +-- app.module.ts
+-- assets/                     # Statik dosyalar
```