# SaaS CMS Platform - Clean Architecture Roadmap

## 📋 Project Overview

**Architecture:** Clean Architecture + Multi-Language Support  
**Target Market:** Turkish & International markets  
**Language Support:** Turkish, English (extensible)  
**Development Approach:** Feature-driven, layer-by-layer implementation  

## 🏗️ Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                 🖥️  PRESENTATION LAYER                      │
│     Controllers, DTOs, Mappers, Validators, i18n           │
├─────────────────────────────────────────────────────────────┤
│                 📋 APPLICATION LAYER                        │
│     Services, Use Cases, Commands, Queries, Events         │
├─────────────────────────────────────────────────────────────┤
│                   🎯 DOMAIN LAYER                           │
│        Entities, Enums, Interfaces, Business Rules         │
├─────────────────────────────────────────────────────────────┤
│                 🔧 INFRASTRUCTURE LAYER                     │
│    Repositories, External APIs, File Storage, Database     │
└─────────────────────────────────────────────────────────────┘
```

## 🌐 Multi-Language Architecture

### **Language Support Strategy:**
- **Backend:** Spring Boot i18n (MessageSource)
- **Frontend Admin:** Angular i18n (@angular/localize)
- **Site Content:** Database-level multi-language support
- **Default Languages:** Turkish (tr), English (en)

### **Implementation Levels:**
1. **System Messages:** Error messages, validation messages, UI labels
2. **Content Management:** Multi-language content creation
3. **Site Publishing:** Language-specific site rendering
4. **Admin Interface:** Localized admin panel

## 📁 Project Structure (Clean Architecture)

```
src/main/java/com/sitecms/
├── 🖥️ presentation/
│   ├── controller/
│   │   ├── TenantController.java
│   │   ├── ContentController.java
│   │   ├── MediaController.java
│   │   └── UserController.java
│   ├── dto/
│   │   ├── request/
│   │   ├── response/
│   │   └── mapper/
│   └── config/
│       ├── WebConfig.java
│       └── LocaleConfig.java
├── 📋 application/
│   ├── service/
│   │   ├── TenantService.java
│   │   ├── ContentService.java
│   │   ├── MediaService.java
│   │   └── UserService.java
│   ├── usecase/
│   │   ├── CreateTenantUseCase.java
│   │   ├── PublishContentUseCase.java
│   │   └── UploadMediaUseCase.java
│   ├── command/
│   ├── query/
│   └── event/
├── 🎯 domain/
│   ├── entity/
│   │   ├── Tenant.java
│   │   ├── Content.java
│   │   ├── ContentType.java
│   │   ├── MediaFile.java
│   │   └── User.java
│   ├── enums/
│   ├── exception/
│   ├── repository/
│   └── service/
├── 🔧 infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── config/
│   ├── external/
│   ├── storage/
│   └── i18n/
└── 🌐 shared/
    ├── common/
    ├── utils/
    └── constants/
```

## 🗺️ Sprint Roadmap (Clean Architecture + i18n)

### 🏁 **PHASE 1: MVP FOUNDATION (8-10 weeks)**

#### **Sprint 1: Architecture Foundation + i18n Setup (2 weeks)**
**Goal:** Clean Architecture foundation with multi-language infrastructure

**Infrastructure Layer:**
- [ ] Spring Boot 3.5.3 project setup with Clean Architecture structure
- [ ] Multi-tenant database configuration (database per tenant)
- [ ] JPA entities with multi-language support
- [ ] i18n configuration (MessageSource, LocaleResolver)
- [ ] Database migration scripts

**Domain Layer:**
- [ ] Core domain entities (Tenant, Content, User, MediaFile)
- [ ] Domain enums (TenantStatus, ContentStatus, UserRole, Language)
- [ ] Business exceptions
- [ ] Repository interfaces

**i18n Foundation:**
- [ ] Backend message bundles (messages_tr.properties, messages_en.properties)
- [ ] Language detection middleware
- [ ] Multi-language validation messages
- [ ] Date/time localization

**Development Environment:**
- [ ] Docker setup with multi-language support
- [ ] GitHub Actions CI/CD pipeline
- [ ] Environment configuration

**Output:** Clean Architecture foundation with i18n ready

#### **Sprint 2: Tenant Management + Multi-Language Admin (2 weeks)**
**Goal:** Complete tenant lifecycle with localized admin interface

**Domain Layer:**
```java
@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String subdomain;
    private String companyName;
    private String databaseName;
    
    @Enumerated(EnumType.STRING)
    private TenantStatus status;
    
    @Enumerated(EnumType.STRING)
    private Language defaultLanguage; // TR, EN
    
    private Set<Language> supportedLanguages;
    
    // Business methods
    public boolean canBeActivated() {
        return status == TenantStatus.PENDING;
    }
    
    public void activate() {
        if (!canBeActivated()) {
            throw new TenantCannotBeActivatedException();
        }
        this.status = TenantStatus.ACTIVE;
    }
}
```

**Application Layer:**
- [ ] TenantService with use cases
- [ ] CreateTenantUseCase, ActivateTenantUseCase
- [ ] Tenant DTOs with i18n support
- [ ] Tenant validation with localized messages

**Infrastructure Layer:**
- [ ] TenantRepository implementation
- [ ] Database tenant creation service
- [ ] Tenant configuration persistence

**Presentation Layer:**
- [ ] TenantController with localized responses
- [ ] Angular admin setup with i18n (@angular/localize)
- [ ] Localized tenant management UI
- [ ] Language switcher component

**i18n Features:**
- [ ] Tenant-specific language settings
- [ ] Localized error messages
- [ ] Admin panel language switching
- [ ] Validation message localization

**Output:** Complete tenant management with multi-language admin

#### **Sprint 3: User Management + Authentication (1.5 weeks)**
**Goal:** User authentication and role management with i18n

**Domain Layer:**
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String passwordHash;
    private String fullName;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    private Language preferredLanguage;
    
    private Long tenantId;
    
    // Business methods
    public boolean canAccessContent(Content content) {
        return this.tenantId.equals(content.getTenantId()) &&
               this.role.hasPermission(Permission.READ_CONTENT);
    }
}
```

**Application Layer:**
- [ ] UserService, AuthenticationService
- [ ] LoginUseCase, CreateUserUseCase
- [ ] JWT token service with language preference
- [ ] Role-based authorization

**Infrastructure Layer:**
- [ ] UserRepository implementation
- [ ] Spring Security configuration
- [ ] JWT token handling

**Presentation Layer:**
- [ ] AuthController with localized responses
- [ ] Login/logout endpoints with i18n
- [ ] User management UI with language preference
- [ ] Role management interface

**i18n Features:**
- [ ] User-specific language preference
- [ ] Localized authentication messages
- [ ] Role-based UI language
- [ ] Personalized welcome messages

**Output:** Complete authentication system with language preferences

#### **Sprint 4: Content Management Core + Multi-Language Content (2.5 weeks)**
**Goal:** Content creation and management with multi-language support

**Domain Layer:**
```java
@Entity
@Table(name = "content_types")
public class ContentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String displayName;
    private String fields; // JSON schema
    private Long tenantId;
    
    private boolean supportsMultiLanguage;
    
    public boolean isValidForLanguage(Language language) {
        return !supportsMultiLanguage || language != null;
    }
}

@Entity
@Table(name = "contents")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String slug;
    private String data; // JSON content
    
    @Enumerated(EnumType.STRING)
    private ContentStatus status;
    
    @Enumerated(EnumType.STRING)
    private Language language; // Content language
    
    private Long parentContentId; // For translations
    private Long contentTypeId;
    private Long tenantId;
    
    // SEO fields
    private String metaTitle;
    private String metaDescription;
    
    // Business methods
    public boolean canBePublished() {
        return status == ContentStatus.DRAFT && 
               title != null && !title.trim().isEmpty();
    }
    
    public void publish() {
        if (!canBePublished()) {
            throw new ContentCannotBePublishedException();
        }
        this.status = ContentStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
}
```

**Application Layer:**
- [ ] ContentService, ContentTypeService
- [ ] CreateContentUseCase, PublishContentUseCase
- [ ] Multi-language content creation workflow
- [ ] Content translation management

**Infrastructure Layer:**
- [ ] ContentRepository, ContentTypeRepository
- [ ] Multi-language content queries
- [ ] Content search with language filtering

**Presentation Layer:**
- [ ] ContentController with language-aware APIs
- [ ] Multi-language content editor UI
- [ ] Language switcher for content
- [ ] Translation management interface

**Default Content Types (Multi-Language):**
- [ ] Page (supports translations)
- [ ] Blog Post (supports translations)
- [ ] Menu (language-specific)

**i18n Features:**
- [ ] Content creation in multiple languages
- [ ] Translation linking between content versions
- [ ] Language-specific content listing
- [ ] Fallback language for missing translations

**Output:** Multi-language content management system

#### **Sprint 5: Media Management + File Localization (1.5 weeks)**
**Goal:** File upload and media management with language support

**Domain Layer:**
```java
@Entity
@Table(name = "media_files")
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String originalName;
    private String fileName;
    private String filePath;
    private String mimeType;
    private Long fileSize;
    
    // Image properties
    private Integer width;
    private Integer height;
    
    // Multi-language alt text
    private String altTextTr;
    private String altTextEn;
    
    private Long tenantId;
    private Long uploadedBy;
    
    public String getAltText(Language language) {
        return switch (language) {
            case TR -> altTextTr;
            case EN -> altTextEn;
            default -> altTextTr; // fallback
        };
    }
}
```

**Application Layer:**
- [ ] MediaService with language-aware operations
- [ ] UploadMediaUseCase with localized metadata
- [ ] Image processing with multi-language alt text
- [ ] Media organization and search

**Infrastructure Layer:**
- [ ] MediaRepository implementation
- [ ] Local file storage service
- [ ] Image resizing service

**Presentation Layer:**
- [ ] MediaController with localized responses
- [ ] File upload UI with language-specific metadata
- [ ] Media library with language filtering
- [ ] Drag & drop interface

**i18n Features:**
- [ ] Multi-language alt text for images
- [ ] Localized file upload messages
- [ ] Language-specific media organization
- [ ] Translated file categories

**Output:** Complete media management with language support

#### **Sprint 6: Site Publishing + Multi-Language Sites (1.5 weeks)**
**Goal:** Site rendering and publishing with language switching

**Domain Layer:**
```java
@Entity
@Table(name = "sites")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String siteName;
    private String description;
    private Set<Language> enabledLanguages;
    
    @Enumerated(EnumType.STRING)
    private Language defaultLanguage;
    
    private Long tenantId;
    
    public boolean isLanguageEnabled(Language language) {
        return enabledLanguages.contains(language);
    }
}

@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private Language language;
    
    private Long tenantId;
}
```

**Application Layer:**
- [ ] SiteService, MenuService
- [ ] PublishSiteUseCase with language-specific rendering
- [ ] Multi-language navigation management
- [ ] Site configuration with language settings

**Infrastructure Layer:**
- [ ] SiteRepository, MenuRepository
- [ ] Thymeleaf rendering with i18n
- [ ] Language-aware URL routing

**Presentation Layer:**
- [ ] SiteController with language-specific endpoints
- [ ] Site settings UI with language configuration
- [ ] Menu management with multi-language support
- [ ] Site preview with language switching

**Site Features:**
- [ ] Language-specific home pages
- [ ] Automatic language detection
- [ ] Language switcher widget
- [ ] Fallback language handling

**Output:** Multi-language site publishing with Thymeleaf

### 🔄 **PHASE 2: ADVANCED FEATURES (6 weeks)**

#### **Sprint 7: Multi-Framework Support + i18n (2 weeks)**
**Goal:** React, Vue, Angular SSR with multi-language support

**Framework Integration:**
- [ ] React SSR with React i18next
- [ ] Vue SSR with Vue I18n
- [ ] Angular SSR with Angular i18n
- [ ] Framework-specific language routing

**Features:**
- [ ] Language-aware component rendering
- [ ] Client-side language switching
- [ ] SEO-friendly language URLs (/tr/, /en/)
- [ ] Hydration with correct language

#### **Sprint 8: Advanced Content Features + Translation Workflow (2 weeks)**
**Goal:** Content versioning, scheduling, and translation management

**Features:**
- [ ] Content versioning with language variants
- [ ] Content scheduling per language
- [ ] Translation workflow (draft → review → publish)
- [ ] Translation status tracking
- [ ] Auto-translation integration (Google Translate API)

#### **Sprint 9: SEO + Analytics + Localization (2 weeks)**
**Goal:** Advanced SEO and analytics with language support

**Features:**
- [ ] Language-specific sitemaps (/sitemap-tr.xml, /sitemap-en.xml)
- [ ] Hreflang tags for SEO
- [ ] Language-specific analytics tracking
- [ ] Localized meta tags and Open Graph
- [ ] Language-aware search optimization

### 🤖 **PHASE 3: AI INTEGRATION + ADVANCED i18n (8 weeks)**

#### **Sprint 10-11: AI Content Generation (4 weeks)**
**Goal:** AI-powered content creation with multi-language support

**Features:**
- [ ] GPT/Claude integration for content generation
- [ ] Multi-language content generation
- [ ] Auto-translation suggestions
- [ ] Language-specific SEO optimization
- [ ] AI-powered localization

#### **Sprint 12-13: Advanced Language Features (4 weeks)**
**Goal:** Enterprise-level language features

**Features:**
- [ ] RTL language support (Arabic, Hebrew)
- [ ] Language-specific themes
- [ ] Professional translation workflow
- [ ] Translation quality scoring
- [ ] Advanced localization analytics

## 🌐 Multi-Language Implementation Details

### **Backend i18n Configuration:**

```java
@Configuration
public class InternationalizationConfig {
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.forLanguageTag("tr"));
        localeResolver.setSupportedLocales(Arrays.asList(
            Locale.forLanguageTag("tr"),
            Locale.forLanguageTag("en")
        ));
        return localeResolver;
    }
}
```

### **Database Schema for Multi-Language:**

```sql
-- Language support in main tables
ALTER TABLE tenants ADD COLUMN default_language ENUM('TR', 'EN') DEFAULT 'TR';
ALTER TABLE tenants ADD COLUMN supported_languages JSON DEFAULT '["TR"]';

ALTER TABLE users ADD COLUMN preferred_language ENUM('TR', 'EN') DEFAULT 'TR';

ALTER TABLE contents ADD COLUMN language ENUM('TR', 'EN') DEFAULT 'TR';
ALTER TABLE contents ADD COLUMN parent_content_id BIGINT NULL; -- For translations

-- Multi-language media alt text
ALTER TABLE media_files ADD COLUMN alt_text_tr TEXT NULL;
ALTER TABLE media_files ADD COLUMN alt_text_en TEXT NULL;

-- Language-specific menus
ALTER TABLE menus ADD COLUMN language ENUM('TR', 'EN') DEFAULT 'TR';

-- Site language configuration
CREATE TABLE site_languages (
    site_id BIGINT NOT NULL,
    language ENUM('TR', 'EN') NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (site_id, language)
);
```

### **Frontend Angular i18n:**

```typescript
// app.module.ts
import { registerLocaleData } from '@angular/common';
import { LOCALE_ID } from '@angular/core';
import localeEn from '@angular/common/locales/en';
import localeTr from '@angular/common/locales/tr';

registerLocaleData(localeEn);
registerLocaleData(localeTr);

// Language service
@Injectable()
export class LanguageService {
  currentLanguage$ = new BehaviorSubject<string>('tr');
  
  setLanguage(language: string) {
    this.currentLanguage$.next(language);
    localStorage.setItem('language', language);
  }
  
  getLanguage(): string {
    return localStorage.getItem('language') || 'tr';
  }
}
```

## 📊 Success Metrics

### **MVP Success Criteria:**
- [ ] 5-10 pilot customers with multi-language sites
- [ ] Both Turkish and English admin interfaces working
- [ ] Content creation in both languages
- [ ] SEO-friendly language URLs
- [ ] Language switching without data loss

### **Technical KPIs:**
- [ ] Language switching response time < 200ms
- [ ] Multi-language content search < 300ms
- [ ] Translation workflow completion rate > 95%
- [ ] Language-specific SEO scores > 90/100

### **Business KPIs:**
- [ ] International customer acquisition > 20%
- [ ] Multi-language site engagement +40%
- [ ] Translation workflow efficiency +60%

## 🚧 Implementation Risks & Mitigation

### **Technical Risks:**
- **Complex language routing:** Use proven i18n libraries
- **Database performance:** Optimize language-specific queries
- **Translation consistency:** Implement validation rules

### **Business Risks:**
- **Translation costs:** Start with machine translation + human review
- **Cultural adaptation:** Partner with local content experts
- **Market complexity:** Focus on TR/EN first, expand later

-- =================================================================
-- SaaS CMS Platform - Multi-Language Database Schema
-- Architecture: Clean Architecture + Multi-Tenant + i18n
-- =================================================================

-- =================================================================
-- MASTER DATABASE: platform_management
-- Purpose: Platform-level data, tenant management
-- =================================================================

CREATE DATABASE IF NOT EXISTS platform_management 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE platform_management;

-- -----------------------------------------------------------------
-- SUPPORTED LANGUAGES TABLE
-- Centralized language configuration
-- -----------------------------------------------------------------
CREATE TABLE supported_languages (
    id TINYINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(5) UNIQUE NOT NULL COMMENT 'ISO 639-1 code: tr, en, de',
    name VARCHAR(50) NOT NULL COMMENT 'Turkish, English, German',
    native_name VARCHAR(50) NOT NULL COMMENT 'Türkçe, English, Deutsch',
    flag_icon VARCHAR(10) NULL COMMENT 'Flag emoji or icon code',
    direction ENUM('ltr', 'rtl') DEFAULT 'ltr',
    is_active BOOLEAN DEFAULT TRUE,
    sort_order TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_code (code),
    INDEX idx_active (is_active),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB COMMENT='Supported platform languages';

-- Insert default languages
INSERT INTO supported_languages (code, name, native_name, flag_icon, sort_order) VALUES
('tr', 'Turkish', 'Türkçe', '🇹🇷', 1),
('en', 'English', 'English', '🇺🇸', 2);

-- -----------------------------------------------------------------
-- TENANTS TABLE
-- Enhanced with multi-language configuration
-- -----------------------------------------------------------------
CREATE TABLE tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Basic Info
    subdomain VARCHAR(50) UNIQUE NOT NULL COMMENT 'customer1.platform.com',
    company_name VARCHAR(100) NOT NULL,
    database_name VARCHAR(50) UNIQUE NOT NULL COMMENT 'tenant_1_db',
    
    -- Status Management
    status ENUM('pending', 'active', 'suspended', 'maintenance') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activated_at TIMESTAMP NULL,
    
    -- Language Configuration
    default_language VARCHAR(5) NOT NULL DEFAULT 'tr' COMMENT 'Default language code',
    supported_languages JSON NOT NULL DEFAULT '["tr"]' COMMENT 'Array of supported language codes',
    
    -- Contact Information
    admin_email VARCHAR(100) NOT NULL,
    admin_name VARCHAR(100) NOT NULL,
    admin_language VARCHAR(5) DEFAULT 'tr' COMMENT 'Admin preferred language',
    phone VARCHAR(20) NULL,
    
    -- Domain Configuration
    custom_domain VARCHAR(100) NULL COMMENT 'customer.com',
    ssl_enabled BOOLEAN DEFAULT FALSE,
    ssl_certificate_path VARCHAR(255) NULL,
    
    -- Technical Configuration
    database_version VARCHAR(10) DEFAULT '1.0',
    last_backup_at TIMESTAMP NULL,
    storage_used_mb BIGINT DEFAULT 0,
    
    -- Localization Settings
    timezone VARCHAR(50) DEFAULT 'Europe/Istanbul',
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    time_format VARCHAR(10) DEFAULT '24h',
    currency VARCHAR(3) DEFAULT 'TRY',
    
    -- Metadata
    notes TEXT NULL,
    created_by VARCHAR(100) DEFAULT 'system',
    
    -- Foreign Key Constraints
    FOREIGN KEY (default_language) REFERENCES supported_languages(code),
    
    INDEX idx_subdomain (subdomain),
    INDEX idx_status (status),
    INDEX idx_default_language (default_language),
    INDEX idx_custom_domain (custom_domain),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='Multi-language tenant registry';

-- -----------------------------------------------------------------
-- PLATFORM ADMINS TABLE
-- Platform administrators with language preferences
-- -----------------------------------------------------------------
CREATE TABLE platform_admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    preferred_language VARCHAR(5) DEFAULT 'tr',
    role ENUM('super_admin', 'admin', 'support') DEFAULT 'admin',
    is_active BOOLEAN DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (preferred_language) REFERENCES supported_languages(code),
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_language (preferred_language)
) ENGINE=InnoDB COMMENT='Platform administrators';

-- -----------------------------------------------------------------
-- TENANT ACTIVITIES LOG
-- Multi-language activity tracking
-- -----------------------------------------------------------------
CREATE TABLE tenant_activities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    activity_type ENUM('created', 'activated', 'suspended', 'domain_added', 'backup_created', 
                      'login', 'content_published', 'language_added', 'language_removed') NOT NULL,
    description_key VARCHAR(100) NOT NULL COMMENT 'i18n message key',
    description_params JSON NULL COMMENT 'Parameters for i18n message',
    language VARCHAR(5) NULL COMMENT 'Language of the activity',
    metadata JSON NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    performed_by VARCHAR(100) NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_tenant_activity (tenant_id, activity_type),
    INDEX idx_language (language),
    INDEX idx_performed_at (performed_at)
) ENGINE=InnoDB COMMENT='Multi-language tenant activity tracking';

-- =================================================================
-- TENANT DATABASE TEMPLATE SCHEMA
-- Schema replicated for each tenant: tenant_{id}_db
-- =================================================================

-- -----------------------------------------------------------------
-- USERS TABLE (Per Tenant)
-- Tenant users with language preferences
-- -----------------------------------------------------------------
/*
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Authentication
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    
    -- Profile
    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(255) NULL,
    
    -- Authorization
    role ENUM('admin', 'editor', 'viewer') DEFAULT 'editor',
    permissions JSON NULL COMMENT 'Additional granular permissions',
    
    -- Localization Preferences
    preferred_language VARCHAR(5) DEFAULT 'tr',
    timezone VARCHAR(50) DEFAULT 'Europe/Istanbul',
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    email_verified_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_language (preferred_language),
    INDEX idx_active (is_active)
) ENGINE=InnoDB COMMENT='Tenant users with language preferences';
*/

-- -----------------------------------------------------------------
-- CONTENT TYPES TABLE
-- Multi-language content type definitions
-- -----------------------------------------------------------------
/*
CREATE TABLE content_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Basic Info
    name VARCHAR(50) UNIQUE NOT NULL COMMENT 'page, blog_post, product',
    
    -- Multi-Language Display Names
    display_name_tr VARCHAR(100) NULL,
    display_name_en VARCHAR(100) NULL,
    description_tr TEXT NULL,
    description_en TEXT NULL,
    
    -- Configuration
    icon VARCHAR(50) NULL COMMENT 'FontAwesome icon class',
    fields JSON NOT NULL COMMENT 'Field definitions with types, validation, i18n labels',
    
    -- Feature Flags
    is_active BOOLEAN DEFAULT TRUE,
    is_system BOOLEAN DEFAULT FALSE COMMENT 'Cannot be deleted',
    supports_multi_language BOOLEAN DEFAULT TRUE,
    supports_seo BOOLEAN DEFAULT TRUE,
    supports_scheduling BOOLEAN DEFAULT TRUE,
    supports_comments BOOLEAN DEFAULT FALSE,
    
    -- URL Configuration
    url_pattern_tr VARCHAR(100) NULL COMMENT '/{slug}, /blog/{slug}',
    url_pattern_en VARCHAR(100) NULL,
    
    -- Metadata
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_name (name),
    INDEX idx_active (is_active),
    INDEX idx_system (is_system),
    INDEX idx_multi_language (supports_multi_language)
) ENGINE=InnoDB COMMENT='Multi-language content type definitions';
*/

-- -----------------------------------------------------------------
-- CONTENTS TABLE
-- Multi-language content with translation linking
-- -----------------------------------------------------------------
/*
CREATE TABLE contents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content_type_id BIGINT NOT NULL,
    
    -- Basic Content Info
    title VARCHAR(200) NOT NULL,
    slug VARCHAR(200) NOT NULL,
    excerpt TEXT NULL,
    content_data JSON NOT NULL COMMENT 'All content fields as JSON',
    
    -- Language Support
    language VARCHAR(5) NOT NULL DEFAULT 'tr',
    translation_group_id BIGINT NULL COMMENT 'Groups related translations',
    
    -- Status Management
    status ENUM('draft', 'published', 'archived', 'scheduled') DEFAULT 'draft',
    published_at TIMESTAMP NULL,
    scheduled_at TIMESTAMP NULL,
    
    -- SEO Data (Language Specific)
    meta_title VARCHAR(60) NULL,
    meta_description VARCHAR(160) NULL,
    meta_keywords VARCHAR(255) NULL,
    canonical_url VARCHAR(255) NULL,
    
    -- Open Graph (Language Specific)
    og_title VARCHAR(60) NULL,
    og_description VARCHAR(160) NULL,
    og_image VARCHAR(255) NULL,
    og_type VARCHAR(50) DEFAULT 'article',
    
    -- Hierarchy Support
    parent_id BIGINT NULL COMMENT 'For page hierarchy',
    sort_order INT DEFAULT 0,
    
    -- Versioning
    version INT DEFAULT 1,
    is_latest_version BOOLEAN DEFAULT TRUE,
    
    -- Analytics
    view_count BIGINT DEFAULT 0,
    last_viewed_at TIMESTAMP NULL,
    
    -- Workflow
    created_by BIGINT NOT NULL,
    updated_by BIGINT NULL,
    reviewed_by BIGINT NULL COMMENT 'For translation review workflow',
    reviewed_at TIMESTAMP NULL,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (content_type_id) REFERENCES content_types(id),
    FOREIGN KEY (parent_id) REFERENCES contents(id) ON DELETE CASCADE,
    FOREIGN KEY (translation_group_id) REFERENCES contents(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id),
    
    UNIQUE KEY uk_slug_language (slug, language),
    INDEX idx_content_type_status (content_type_id, status),
    INDEX idx_language (language),
    INDEX idx_translation_group (translation_group_id),
    INDEX idx_published_at (published_at),
    INDEX idx_parent (parent_id),
    INDEX idx_sort_order (sort_order),
    INDEX idx_latest_version (is_latest_version),
    FULLTEXT idx_search (title, excerpt),
    INDEX idx_scheduled (scheduled_at, status)
) ENGINE=InnoDB COMMENT='Multi-language content items';
*/

-- -----------------------------------------------------------------
-- CONTENT TRANSLATIONS TABLE
-- Translation relationship and workflow tracking
-- -----------------------------------------------------------------
/*
CREATE TABLE content_translations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    original_content_id BIGINT NOT NULL,
    translated_content_id BIGINT NOT NULL,
    source_language VARCHAR(5) NOT NULL,
    target_language VARCHAR(5) NOT NULL,
    
    -- Translation Workflow
    translation_status ENUM('pending', 'in_progress', 'review', 'completed') DEFAULT 'pending',
    translator_id BIGINT NULL,
    reviewer_id BIGINT NULL,
    
    -- Quality Metrics
    translation_quality_score DECIMAL(3,2) NULL COMMENT '0.00 to 1.00',
    auto_translated BOOLEAN DEFAULT FALSE,
    human_reviewed BOOLEAN DEFAULT FALSE,
    
    -- Workflow Timestamps
    translation_started_at TIMESTAMP NULL,
    translation_completed_at TIMESTAMP NULL,
    review_completed_at TIMESTAMP NULL,
    
    -- Metadata
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (original_content_id) REFERENCES contents(id) ON DELETE CASCADE,
    FOREIGN KEY (translated_content_id) REFERENCES contents(id) ON DELETE CASCADE,
    FOREIGN KEY (translator_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    
    UNIQUE KEY uk_translation_pair (original_content_id, target_language),
    INDEX idx_translation_status (translation_status),
    INDEX idx_languages (source_language, target_language),
    INDEX idx_translator (translator_id),
    INDEX idx_quality (translation_quality_score)
) ENGINE=InnoDB COMMENT='Content translation workflow';
*/

-- -----------------------------------------------------------------
-- MEDIA FILES TABLE
-- Multi-language media metadata
-- -----------------------------------------------------------------
/*
CREATE TABLE media_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- File Properties
    original_name VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL COMMENT 'UUID-based filename',
    file_path VARCHAR(500) NOT NULL,
    file_url VARCHAR(500) NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    file_extension VARCHAR(10) NOT NULL,
    
    -- Image Properties
    width INT NULL,
    height INT NULL,
    
    -- Multi-Language Metadata
    alt_text_tr VARCHAR(255) NULL,
    alt_text_en VARCHAR(255) NULL,
    caption_tr TEXT NULL,
    caption_en TEXT NULL,
    description_tr TEXT NULL,
    description_en TEXT NULL,
    
    -- Organization
    folder_path VARCHAR(255) DEFAULT '/',
    tags JSON NULL COMMENT 'Array of tags for organization',
    
    -- Usage Analytics
    usage_count INT DEFAULT 0,
    last_used_at TIMESTAMP NULL,
    
    -- Metadata
    uploaded_by BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    
    INDEX idx_file_name (file_name),
    INDEX idx_mime_type (mime_type),
    INDEX idx_folder (folder_path),
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_usage (usage_count),
    FULLTEXT idx_search_tr (original_name, alt_text_tr, caption_tr),
    FULLTEXT idx_search_en (original_name, alt_text_en, caption_en)
) ENGINE=InnoDB COMMENT='Multi-language media metadata';
*/

-- -----------------------------------------------------------------
-- SITE SETTINGS TABLE
-- Multi-language site configuration
-- -----------------------------------------------------------------
/*
CREATE TABLE site_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL,
    setting_value JSON NULL,
    language VARCHAR(5) NULL COMMENT 'NULL for global settings',
    setting_type ENUM('text', 'number', 'boolean', 'json', 'file', 'i18n_text') DEFAULT 'text',
    category VARCHAR(50) DEFAULT 'general',
    display_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    updated_by BIGINT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (updated_by) REFERENCES users(id),
    
    UNIQUE KEY uk_key_language (setting_key, language),
    INDEX idx_category (category),
    INDEX idx_language (language),
    INDEX idx_public (is_public),
    INDEX idx_type (setting_type)
) ENGINE=InnoDB COMMENT='Multi-language site settings';
*/

-- -----------------------------------------------------------------
-- MENUS TABLE
-- Language-specific navigation menus
-- -----------------------------------------------------------------
/*
CREATE TABLE menus (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT 'primary, footer, sidebar',
    language VARCHAR(5) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_name_language (name, language),
    INDEX idx_language (language),
    INDEX idx_active (is_active)
) ENGINE=InnoDB COMMENT='Language-specific menus';
*/

-- -----------------------------------------------------------------
-- MENU ITEMS TABLE
-- Multi-language menu items with hierarchy
-- -----------------------------------------------------------------
/*
CREATE TABLE menu_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    menu_id BIGINT NOT NULL,
    
    -- Item Properties
    title VARCHAR(100) NOT NULL,
    url VARCHAR(255) NULL,
    content_id BIGINT NULL COMMENT 'Link to content item',
    external_url VARCHAR(255) NULL,
    target VARCHAR(10) DEFAULT '_self',
    css_class VARCHAR(100) NULL,
    icon VARCHAR(50) NULL,
    
    -- Hierarchy
    parent_id BIGINT NULL,
    sort_order INT DEFAULT 0,
    
    -- Visibility
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (menu_id) REFERENCES menus(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES menu_items(id) ON DELETE CASCADE,
    FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE SET NULL,
    
    INDEX idx_menu_sort (menu_id, sort_order),
    INDEX idx_parent (parent_id),
    INDEX idx_active (is_active),
    INDEX idx_content (content_id)
) ENGINE=InnoDB COMMENT='Multi-language menu items';
*/

-- -----------------------------------------------------------------
-- CONTENT TAGS TABLE
-- Multi-language content tagging
-- -----------------------------------------------------------------
/*
CREATE TABLE content_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL,
    language VARCHAR(5) NOT NULL,
    description_tr TEXT NULL,
    description_en TEXT NULL,
    color VARCHAR(7) NULL COMMENT 'Hex color code',
    usage_count INT DEFAULT 0,
    translation_group_id BIGINT NULL COMMENT 'Link related language versions',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_slug_language (slug, language),
    INDEX idx_name (name),
    INDEX idx_language (language),
    INDEX idx_usage (usage_count),
    INDEX idx_translation_group (translation_group_id)
) ENGINE=InnoDB COMMENT='Multi-language content tags';
*/

-- -----------------------------------------------------------------
-- CONTENT TAG RELATIONS
-- -----------------------------------------------------------------
/*
CREATE TABLE content_tag_relations (
    content_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (content_id, tag_id),
    FOREIGN KEY (content_id) REFERENCES contents(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES content_tags(id) ON DELETE CASCADE,
    
    INDEX idx_tag_content (tag_id, content_id)
) ENGINE=InnoDB COMMENT='Content to tag relationships';
*/

-- -----------------------------------------------------------------
-- USER SESSIONS TABLE
-- Session management with language tracking
-- -----------------------------------------------------------------
/*
CREATE TABLE user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    preferred_language VARCHAR(5) DEFAULT 'tr',
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_active (user_id, is_active),
    INDEX idx_token (session_token),
    INDEX idx_expires (expires_at),
    INDEX idx_language (preferred_language)
) ENGINE=InnoDB COMMENT='Session management with language';
*/

-- =================================================================
-- MULTI-LANGUAGE HELPER VIEWS
-- =================================================================

-- Language-aware content view
/*
CREATE VIEW content_with_translations AS
SELECT 
    c.id,
    c.content_type_id,
    c.title,
    c.slug,
    c.language,
    c.status,
    c.published_at,
    c.translation_group_id,
    COUNT(ct.translated_content_id) as translation_count,
    GROUP_CONCAT(DISTINCT ct.target_language) as available_languages
FROM contents c
LEFT JOIN content_translations ct ON c.id = ct.original_content_id
WHERE c.is_latest_version = TRUE
GROUP BY c.id;
*/

-- =================================================================
-- SAMPLE DATA AND INITIALIZATION
-- =================================================================

-- Sample platform admin
INSERT INTO platform_admins (email, password_hash, full_name, preferred_language, role) VALUES
('admin@platform.com', '$2y$10$example_hash_here', 'Platform Administrator', 'tr', 'super_admin');

-- Sample tenant with multi-language support
INSERT INTO tenants (
    subdomain, company_name, database_name, status,
    admin_email, admin_name, admin_language,
    default_language, supported_languages,
    timezone, currency
) VALUES (
    'demo', 'Demo Şirketi A.Ş.', 'tenant_demo_db', 'active',
    'admin@demo.com', 'Ahmet Yılmaz', 'tr',
    'tr', '["tr", "en"]',
    'Europe/Istanbul', 'TRY'
);

-- =================================================================
-- TENANT DATABASE CREATION PROCEDURE
-- =================================================================

DELIMITER //

CREATE PROCEDURE CreateMultiLanguageTenantDatabase(
    IN tenant_id BIGINT, 
    IN db_name VARCHAR(50),
    IN default_lang VARCHAR(5),
    IN supported_langs JSON
)
BEGIN
    DECLARE sql_stmt TEXT;
    DECLARE done INT DEFAULT FALSE;
    DECLARE table_name VARCHAR(100);
    
    -- Cursor for table creation
    DECLARE table_cursor CURSOR FOR 
        SELECT 'users', 'content_types', 'contents', 'content_translations',
               'media_files', 'site_settings', 'menus', 'menu_items',
               'content_tags', 'content_tag_relations', 'user_sessions';
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- Create database
    SET sql_stmt = CONCAT('CREATE DATABASE IF NOT EXISTS ', db_name, 
                         ' CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    -- Use database
    SET sql_stmt = CONCAT('USE ', db_name);
    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    -- Create all tenant tables (implementation would read from template)
    -- Insert default content types
    CALL InsertDefaultContentTypes(default_lang, supported_langs);
    
    -- Create default menus for each language
    CALL CreateDefaultMenus(supported_langs);
    
    -- Insert default site settings
    CALL InsertDefaultSiteSettings(default_lang, supported_langs);
    
END //

DELIMITER ;

-- =================================================================
-- PERFORMANCE OPTIMIZATIONS
-- =================================================================

-- Partitioning for large content tables (future optimization)
/*
ALTER TABLE contents 
PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
*/

-- Full-text search optimization
/*
ALTER TABLE contents ADD FULLTEXT(title, excerpt);
ALTER TABLE media_files ADD FULLTEXT(original_name, alt_text_tr, alt_text_en);
*/

-- =================================================================
-- MIGRATION SCRIPTS FOR MULTI-LANGUAGE SUPPORT
-- =================================================================

-- Migration: Add language support to existing tenant
DELIMITER //

CREATE PROCEDURE AddLanguageToTenant(
    IN tenant_db_name VARCHAR(50),
    IN new_language VARCHAR(5)
)
BEGIN
    DECLARE sql_stmt TEXT;
    
    -- Use tenant database
    SET sql_stmt = CONCAT('USE ', tenant_db_name);
    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    -- Add language columns to media_files if not exists
    SET sql_stmt = CONCAT('ALTER TABLE media_files ADD COLUMN alt_text_', new_language, ' VARCHAR(255) NULL');
    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    SET sql_stmt = CONCAT('ALTER TABLE media_files ADD COLUMN caption_', new_language, ' TEXT NULL');
    SET @sql = sql_stmt;
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    -- Create default menu for new language
    INSERT INTO menus (name, language, display_name) VALUES
    ('primary', new_language, 'Primary Menu'),
    ('footer', new_language, 'Footer Menu');
    
END //

DELIMITER ;

-- =================================================================
-- BACKUP AND MAINTENANCE PROCEDURES
-- =================================================================

-- Language-aware backup procedure
DELIMITER //

CREATE PROCEDURE BackupTenantWithLanguages(IN tenant_id BIGINT)
BEGIN
    DECLARE db_name VARCHAR(50);
    DECLARE supported_langs JSON;
    DECLARE backup_path VARCHAR(255);
    
    -- Get tenant info
    SELECT database_name, supported_languages 
    INTO db_name, supported_langs
    FROM tenants 
    WHERE id = tenant_id;
    
    -- Create backup with language metadata
    SET backup_path = CONCAT('/backups/', db_name, '_', 
                           DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s'), 
                           '_multilang.sql');
    
    -- Update backup timestamp
    UPDATE tenants 
    SET last_backup_at = NOW() 
    WHERE id = tenant_id;
    
    -- Log activity with language info
    INSERT INTO tenant_activities (
        tenant_id, activity_type, description_key, 
        description_params, performed_by
    ) VALUES (
        tenant_id, 'backup_created', 'backup.created',
        JSON_OBJECT('backup_path', backup_path, 'languages', supported_langs),
        'system'
    );
    
END //

DELIMITER ;

-- =================================================================
-- ANALYTICS AND REPORTING VIEWS
-- =================================================================

-- Multi-language content statistics
CREATE VIEW content_language_stats AS
SELECT 
    language,
    COUNT(*) as total_contents,
    COUNT(CASE WHEN status = 'published' THEN 1 END) as published_contents,
    COUNT(CASE WHEN status = 'draft' THEN 1 END) as draft_contents,
    AVG(view_count) as avg_views,
    MAX(updated_at) as last_update
FROM contents
WHERE is_latest_version = TRUE
GROUP BY language;

-- Translation completion rates
CREATE VIEW translation_completion_stats AS
SELECT 
    source_language,
    target_language,
    COUNT(*) as total_translations,
    COUNT(CASE WHEN translation_status = 'completed' THEN 1 END) as completed,
    COUNT(CASE WHEN translation_status = 'pending' THEN 1 END) as pending,
    AVG(translation_quality_score) as avg_quality,
    COUNT(CASE WHEN human_reviewed = TRUE THEN 1 END) as human_reviewed
FROM content_translations
GROUP BY source_language, target_language;

-- =================================================================
-- INDEXES FOR PERFORMANCE
-- =================================================================

-- Composite indexes for common queries
-- Already defined in table creation, but additional ones:

/*
-- Multi-language content queries
CREATE INDEX idx_content_lang_status_published ON contents(language, status, published_at);
CREATE INDEX idx_content_translation_group_lang ON contents(translation_group_id, language);

-- Media file language queries  
CREATE INDEX idx_media_usage_lang ON media_files(usage_count, uploaded_at);

-- Translation workflow queries
CREATE INDEX idx_translation_workflow ON content_translations(translation_status, translator_id);
CREATE INDEX idx_translation_quality ON content_translations(translation_quality_score, human_reviewed);

-- Menu language queries
CREATE INDEX idx_menu_lang_active ON menus(language, is_active);
CREATE INDEX idx_menu_items_menu_sort ON menu_items(menu_id, sort_order, is_active);
*/

// =================================================================
// SaaS CMS Platform - Application Services Examples
// Clean Architecture + Multi-Language Support
// =================================================================

// =================================================================
// APPLICATION LAYER OVERVIEW
// =================================================================

/*
Application Services in Clean Architecture:
1. Orchestrate business workflows
2. Coordinate domain objects
3. Handle transactions
4. Publish domain events
5. Convert between DTOs and domain objects
6. Implement use cases
7. Handle multi-language business logic

Key Principles:
- No business logic (that's in Domain layer)
- Thin layer - just orchestration
- Transaction boundaries
- Error handling and validation
- Multi-tenant and multi-language aware
*/

// =================================================================
// 1. TENANT MANAGEMENT APPLICATION SERVICES
// =================================================================

@Service
@Transactional
public class TenantApplicationService {
    
    private final TenantRepository tenantRepository;
    private final DatabaseProvisioningService dbProvisioningService;
    private final DomainEventPublisher eventPublisher;
    private final TenantMapper tenantMapper;
    private final MessageSource messageSource;
    
    // Use Case: Create New Tenant with Multi-Language Support
    public TenantResponseDto createTenant(CreateTenantCommand command) {
        
        // 1. Validate business rules
        validateTenantCreation(command);
        
        // 2. Check subdomain availability
        if (tenantRepository.existsBySubdomain(command.getSubdomain())) {
            throw new SubdomainAlreadyExistsException(command.getSubdomain());
        }
        
        // 3. Create domain object
        Tenant tenant = Tenant.create(
            TenantId.generate(),
            new Subdomain(command.getSubdomain()),
            new CompanyName(command.getCompanyName()),
            Language.fromCode(command.getDefaultLanguage()),
            command.getSupportedLanguages().stream()
                .map(Language::fromCode)
                .collect(Collectors.toSet()),
            new AdminContact(command.getAdminEmail(), command.getAdminName())
        );
        
        // 4. Save tenant (domain event will be published)
        tenantRepository.save(tenant);
        
        // 5. Provision database infrastructure
        String databaseName = generateDatabaseName(tenant.getId());
        dbProvisioningService.createTenantDatabase(
            databaseName, 
            tenant.getDefaultLanguage(),
            tenant.getSupportedLanguages()
        );
        
        // 6. Update tenant with database info
        tenant.configureDatabaseConnection(databaseName);
        tenantRepository.save(tenant);
        
        // 7. Publish domain events
        eventPublisher.publishEvents(tenant.getDomainEvents());
        tenant.clearDomainEvents();
        
        return tenantMapper.toResponseDto(tenant);
    }
    
    // Use Case: Add Language Support to Existing Tenant
    public TenantResponseDto addLanguageSupport(AddLanguageCommand command) {
        
        // 1. Load tenant
        Tenant tenant = tenantRepository.findById(command.getTenantId())
            .orElseThrow(() -> new TenantNotFoundException(command.getTenantId()));
        
        // 2. Business validation
        Language newLanguage = Language.fromCode(command.getLanguageCode());
        if (tenant.supportsLanguage(newLanguage)) {
            throw new LanguageAlreadySupportedException(newLanguage, tenant.getId());
        }
        
        // 3. Add language support (domain logic)
        tenant.addLanguageSupport(newLanguage);
        
        // 4. Update infrastructure
        dbProvisioningService.addLanguageToTenantDatabase(
            tenant.getDatabaseName(), 
            newLanguage
        );
        
        // 5. Save and publish events
        tenantRepository.save(tenant);
        eventPublisher.publishEvents(tenant.getDomainEvents());
        tenant.clearDomainEvents();
        
        return tenantMapper.toResponseDto(tenant);
    }
    
    // Use Case: Configure Custom Domain with Language Routing
    public TenantResponseDto configureCustomDomain(ConfigureCustomDomainCommand command) {
        
        Tenant tenant = tenantRepository.findById(command.getTenantId())
            .orElseThrow(() -> new TenantNotFoundException(command.getTenantId()));
        
        // Business validation
        CustomDomain customDomain = new CustomDomain(command.getDomainName());
        if (tenantRepository.existsByCustomDomain(customDomain)) {
            throw new CustomDomainAlreadyExistsException(customDomain);
        }
        
        // Configure domain with language routing strategy
        tenant.configureCustomDomain(
            customDomain, 
            LanguageRoutingStrategy.fromString(command.getRoutingStrategy()) // subdomain, path, header
        );
        
        tenantRepository.save(tenant);
        eventPublisher.publishEvents(tenant.getDomainEvents());
        tenant.clearDomainEvents();
        
        return tenantMapper.toResponseDto(tenant);
    }
    
    private void validateTenantCreation(CreateTenantCommand command) {
        // Validation logic with localized error messages
        if (command.getSupportedLanguages().isEmpty()) {
            throw new ValidationException(
                messageSource.getMessage("tenant.validation.languages.required", null, Locale.getDefault())
            );
        }
        
        if (!command.getSupportedLanguages().contains(command.getDefaultLanguage())) {
            throw new ValidationException(
                messageSource.getMessage("tenant.validation.default.language.not.supported", null, Locale.getDefault())
            );
        }
    }
}

// =================================================================
// 2. CONTENT MANAGEMENT APPLICATION SERVICES
// =================================================================

@Service
@Transactional
public class ContentApplicationService {
    
    private final ContentRepository contentRepository;
    private final ContentTypeRepository contentTypeRepository;
    private final UserRepository userRepository;
    private final TenantContextService tenantContextService;
    private final ContentValidationService contentValidationService;
    private final SeoOptimizationService seoOptimizationService;
    private final DomainEventPublisher eventPublisher;
    private final ContentMapper contentMapper;
    
    // Use Case: Create Content in Specific Language
    public ContentResponseDto createContent(CreateContentCommand command) {
        
        // 1. Get current tenant and user context
        TenantId tenantId = tenantContextService.getCurrentTenantId();
        UserId userId = tenantContextService.getCurrentUserId();
        Language contentLanguage = Language.fromCode(command.getLanguageCode());
        
        // 2. Load and validate content type
        ContentType contentType = contentTypeRepository.findById(command.getContentTypeId())
            .orElseThrow(() -> new ContentTypeNotFoundException(command.getContentTypeId()));
        
        if (!contentType.supportsLanguage(contentLanguage)) {
            throw new LanguageNotSupportedForContentTypeException(contentLanguage, contentType.getName());
        }
        
        // 3. Validate content data against content type schema
        ContentData contentData = ContentData.fromJson(command.getContentData());
        contentValidationService.validateContentData(contentData, contentType);
        
        // 4. Create content domain object
        Content content = Content.create(
            ContentId.generate(),
            contentType.getId(),
            tenantId,
            new ContentTitle(command.getTitle()),
            new ContentSlug(command.getSlug(), contentLanguage), // Language-aware slug
            contentData,
            contentLanguage,
            userId
        );
        
        // 5. Apply SEO optimization
        if (command.getAutoOptimizeSeo()) {
            SeoMetadata seoData = seoOptimizationService.optimizeForLanguage(
                content.getTitle(), 
                content.getExcerpt(), 
                contentLanguage
            );
            content.applySeoMetadata(seoData);
        }
        
        // 6. Save content
        contentRepository.save(content);
        
        // 7. Publish domain events
        eventPublisher.publishEvents(content.getDomainEvents());
        content.clearDomainEvents();
        
        return contentMapper.toResponseDto(content);
    }
    
    // Use Case: Create Translation of Existing Content
    public ContentResponseDto createTranslation(CreateTranslationCommand command) {
        
        // 1. Load original content
        Content originalContent = contentRepository.findById(command.getOriginalContentId())
            .orElseThrow(() -> new ContentNotFoundException(command.getOriginalContentId()));
        
        Language targetLanguage = Language.fromCode(command.getTargetLanguageCode());
        UserId translatorId = tenantContextService.getCurrentUserId();
        
        // 2. Business validation
        if (originalContent.getLanguage().equals(targetLanguage)) {
            throw new SameLanguageTranslationException(targetLanguage);
        }
        
        if (originalContent.hasTranslationInLanguage(targetLanguage)) {
            throw new TranslationAlreadyExistsException(originalContent.getId(), targetLanguage);
        }
        
        // 3. Create translation
        Content translation = originalContent.createTranslation(
            ContentId.generate(),
            targetLanguage,
            new ContentTitle(command.getTranslatedTitle()),
            new ContentSlug(command.getTranslatedSlug(), targetLanguage),
            ContentData.fromJson(command.getTranslatedContentData()),
            translatorId
        );
        
        // 4. Create translation tracking record
        TranslationWorkflow workflow = TranslationWorkflow.create(
            originalContent.getId(),
            translation.getId(),
            originalContent.getLanguage(),
            targetLanguage,
            translatorId,
            command.isAutoTranslated()
        );
        
        // 5. Save both content and workflow
        contentRepository.save(translation);
        translationWorkflowRepository.save(workflow);
        
        // 6. Publish events
        eventPublisher.publishEvents(translation.getDomainEvents());
        translation.clearDomainEvents();
        
        return contentMapper.toResponseDto(translation);
    }
    
    // Use Case: Publish Content with Language-Specific Validation
    public ContentResponseDto publishContent(PublishContentCommand command) {
        
        Content content = contentRepository.findById(command.getContentId())
            .orElseThrow(() -> new ContentNotFoundException(command.getContentId()));
        
        UserId publisherId = tenantContextService.getCurrentUserId();
        
        // 1. Business validation
        if (!content.canBePublishedBy(publisherId)) {
            throw new InsufficientPermissionsException("content.publish", publisherId);
        }
        
        // 2. Language-specific validation
        contentValidationService.validateForPublication(content, content.getLanguage());
        
        // 3. SEO validation for language
        if (!seoOptimizationService.isOptimalForLanguage(content, content.getLanguage())) {
            // Log warning but don't block publication
            log.warn("Content {} has suboptimal SEO for language {}", 
                    content.getId(), content.getLanguage());
        }
        
        // 4. Publish content (domain logic)
        content.publish(publisherId);
        
        // 5. Handle scheduling if specified
        if (command.getScheduledPublishAt() != null) {
            content.schedulePublication(command.getScheduledPublishAt());
        }
        
        // 6. Save and publish events
        contentRepository.save(content);
        eventPublisher.publishEvents(content.getDomainEvents());
        content.clearDomainEvents();
        
        return contentMapper.toResponseDto(content);
    }
    
    // Use Case: Get Content with Language Fallback
    public ContentResponseDto getContentBySlug(String slug, String languageCode) {
        
        TenantId tenantId = tenantContextService.getCurrentTenantId();
        Language requestedLanguage = Language.fromCode(languageCode);
        
        // 1. Try to find content in requested language
        Optional<Content> content = contentRepository.findBySlugAndLanguageAndTenant(
            slug, requestedLanguage, tenantId
        );
        
        if (content.isPresent()) {
            return contentMapper.toResponseDto(content.get());
        }
        
        // 2. Fallback to tenant's default language
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException(tenantId));
        
        content = contentRepository.findBySlugAndLanguageAndTenant(
            slug, tenant.getDefaultLanguage(), tenantId
        );
        
        if (content.isPresent()) {
            // Add fallback indicator to response
            ContentResponseDto dto = contentMapper.toResponseDto(content.get());
            dto.setIsFallbackLanguage(true);
            dto.setRequestedLanguage(languageCode);
            return dto;
        }
        
        // 3. No content found in any language
        throw new ContentNotFoundException(slug, languageCode);
    }
    
    // Use Case: Bulk Translation Operation
    public BulkTranslationResponseDto bulkTranslateContent(BulkTranslationCommand command) {
        
        TenantId tenantId = tenantContextService.getCurrentTenantId();
        Language sourceLanguage = Language.fromCode(command.getSourceLanguageCode());
        Language targetLanguage = Language.fromCode(command.getTargetLanguageCode());
        
        // 1. Get all content needing translation
        List<Content> contentToTranslate = contentRepository.findContentNeedingTranslation(
            tenantId, sourceLanguage, targetLanguage, command.getContentTypeIds()
        );
        
        List<TranslationResult> results = new ArrayList<>();
        
        // 2. Process each content item
        for (Content content : contentToTranslate) {
            try {
                // Create translation command
                CreateTranslationCommand translationCommand = CreateTranslationCommand.builder()
                    .originalContentId(content.getId())
                    .targetLanguageCode(targetLanguage.getCode())
                    .autoTranslated(true)
                    .build();
                
                // Use AI translation service or human translator
                if (command.isUseAiTranslation()) {
                    TranslationData translationData = aiTranslationService.translateContent(
                        content, targetLanguage
                    );
                    translationCommand.setTranslatedTitle(translationData.getTitle());
                    translationCommand.setTranslatedSlug(translationData.getSlug());
                    translationCommand.setTranslatedContentData(translationData.getContentData());
                }
                
                ContentResponseDto translation = createTranslation(translationCommand);
                results.add(TranslationResult.success(content.getId(), translation.getId()));
                
            } catch (Exception e) {
                results.add(TranslationResult.failure(content.getId(), e.getMessage()));
            }
        }
        
        return BulkTranslationResponseDto.builder()
            .totalProcessed(contentToTranslate.size())
            .successCount(results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum())
            .results(results)
            .build();
    }
}

// =================================================================
// 3. TRANSLATION WORKFLOW APPLICATION SERVICE
// =================================================================

@Service
@Transactional
public class TranslationWorkflowApplicationService {
    
    private final TranslationWorkflowRepository workflowRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TranslationQualityService qualityService;
    private final DomainEventPublisher eventPublisher;
    
    // Use Case: Assign Translation Task
    public TranslationTaskResponseDto assignTranslationTask(AssignTranslationCommand command) {
        
        // 1. Load workflow
        TranslationWorkflow workflow = workflowRepository.findById(command.getWorkflowId())
            .orElseThrow(() -> new TranslationWorkflowNotFoundException(command.getWorkflowId()));
        
        // 2. Load translator
        User translator = userRepository.findById(command.getTranslatorId())
            .orElseThrow(() -> new UserNotFoundException(command.getTranslatorId()));
        
        // 3. Business validation
        if (!translator.canTranslate(workflow.getSourceLanguage(), workflow.getTargetLanguage())) {
            throw new TranslatorNotQualifiedException(
                translator.getId(), 
                workflow.getSourceLanguage(), 
                workflow.getTargetLanguage()
            );
        }
        
        // 4. Assign translator (domain logic)
        workflow.assignTranslator(translator.getId(), command.getDeadline());
        
        // 5. Send notification
        notificationService.notifyTranslatorAssigned(
            translator.getId(), 
            workflow.getId(),
            workflow.getTargetLanguage()
        );
        
        // 6. Save and publish events
        workflowRepository.save(workflow);
        eventPublisher.publishEvents(workflow.getDomainEvents());
        workflow.clearDomainEvents();
        
        return translationTaskMapper.toResponseDto(workflow);
    }
    
    // Use Case: Submit Translation for Review
    public TranslationTaskResponseDto submitTranslationForReview(SubmitTranslationCommand command) {
        
        // 1. Load workflow and content
        TranslationWorkflow workflow = workflowRepository.findById(command.getWorkflowId())
            .orElseThrow(() -> new TranslationWorkflowNotFoundException(command.getWorkflowId()));
        
        Content translatedContent = contentRepository.findById(workflow.getTranslatedContentId())
            .orElseThrow(() -> new ContentNotFoundException(workflow.getTranslatedContentId()));
        
        // 2. Business validation
        if (!workflow.canBeSubmittedForReview()) {
            throw new InvalidWorkflowStateException(workflow.getStatus(), "Cannot submit for review");
        }
        
        // 3. Auto quality check
        TranslationQuality quality = qualityService.assessTranslationQuality(
            workflow.getOriginalContentId(),
            workflow.getTranslatedContentId(),
            workflow.getTargetLanguage()
        );
        
        // 4. Submit for review (domain logic)
        workflow.submitForReview(quality.getScore(), command.getTranslatorNotes());
        
        // 5. Assign reviewer if needed
        if (command.getReviewerId() != null) {
            User reviewer = userRepository.findById(command.getReviewerId())
                .orElseThrow(() -> new UserNotFoundException(command.getReviewerId()));
            
            workflow.assignReviewer(reviewer.getId());
            
            notificationService.notifyReviewerAssigned(
                reviewer.getId(),
                workflow.getId(),
                workflow.getTargetLanguage()
            );
        }
        
        // 6. Save and publish events
        workflowRepository.save(workflow);
        eventPublisher.publishEvents(workflow.getDomainEvents());
        workflow.clearDomainEvents();
        
        return translationTaskMapper.toResponseDto(workflow);
    }
    
    // Use Case: Complete Translation Review
    public TranslationTaskResponseDto completeTranslationReview(CompleteReviewCommand command) {
        
        TranslationWorkflow workflow = workflowRepository.findById(command.getWorkflowId())
            .orElseThrow(() -> new TranslationWorkflowNotFoundException(command.getWorkflowId()));
        
        UserId reviewerId = tenantContextService.getCurrentUserId();
        
        // 1. Business validation
        if (!workflow.canBeReviewed()) {
            throw new InvalidWorkflowStateException(workflow.getStatus(), "Cannot be reviewed");
        }
        
        if (!workflow.isAssignedReviewer(reviewerId)) {
            throw new UnauthorizedReviewerException(reviewerId, workflow.getId());
        }
        
        // 2. Complete review (domain logic)
        ReviewResult reviewResult = ReviewResult.builder()
            .approved(command.isApproved())
            .qualityScore(command.getQualityScore())
            .reviewerComments(command.getReviewerComments())
            .suggestedChanges(command.getSuggestedChanges())
            .build();
        
        workflow.completeReview(reviewerId, reviewResult);
        
        // 3. Handle review outcome
        if (reviewResult.isApproved()) {
            // Auto-publish translated content if configured
            if (workflow.shouldAutoPublishOnApproval()) {
                Content translatedContent = contentRepository.findById(workflow.getTranslatedContentId())
                    .orElseThrow(() -> new ContentNotFoundException(workflow.getTranslatedContentId()));
                
                translatedContent.publish(reviewerId);
                contentRepository.save(translatedContent);
            }
            
            // Notify translator of approval
            notificationService.notifyTranslationApproved(
                workflow.getTranslatorId(),
                workflow.getId(),
                reviewResult.getQualityScore()
            );
        } else {
            // Send back for revision
            notificationService.notifyTranslationRejected(
                workflow.getTranslatorId(),
                workflow.getId(),
                reviewResult.getReviewerComments()
            );
        }
        
        // 4. Save and publish events
        workflowRepository.save(workflow);
        eventPublisher.publishEvents(workflow.getDomainEvents());
        workflow.clearDomainEvents();
        
        return translationTaskMapper.toResponseDto(workflow);
    }
}

// =================================================================
// 4. SITE PUBLISHING APPLICATION SERVICE
// =================================================================

@Service
@Transactional
public class SitePublishingApplicationService {
    
    private final SiteRepository siteRepository;
    private final ContentRepository contentRepository;
    private final MenuRepository menuRepository;
    private final SiteRenderingService siteRenderingService;
    private final SeoOptimizationService seoOptimizationService;
    private final CacheService cacheService;
    private final DomainEventPublisher eventPublisher;
    
    // Use Case: Publish Multi-Language Site
    public SitePublicationResponseDto publishSite(PublishSiteCommand command) {
        
        TenantId tenantId = tenantContextService.getCurrentTenantId();
        UserId publisherId = tenantContextService.getCurrentUserId();
        
        // 1. Load site configuration
        Site site = siteRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new SiteNotFoundException(tenantId));
        
        // 2. Validate publishing permissions
        if (!site.canBePublishedBy(publisherId)) {
            throw new InsufficientPermissionsException("site.publish", publisherId);
        }
        
        List<PublicationResult> languageResults = new ArrayList<>();
        
        // 3. Publish site for each supported language
        for (Language language : site.getSupportedLanguages()) {
            try {
                PublicationResult result = publishSiteForLanguage(site, language, command);
                languageResults.add(result);
            } catch (Exception e) {
                languageResults.add(PublicationResult.failure(language, e.getMessage()));
            }
        }
        
        // 4. Update site publication status
        boolean allSuccessful = languageResults.stream().allMatch(PublicationResult::isSuccess);
        if (allSuccessful) {
            site.markAsPublished(publisherId);
        } else {
            site.markAsPartiallyPublished(publisherId);
        }
        
        // 5. Clear caches
        cacheService.clearSiteCache(tenantId);
        
        // 6. Save and publish events
        siteRepository.save(site);
        eventPublisher.publishEvents(site.getDomainEvents());
        site.clearDomainEvents();
        
        return SitePublicationResponseDto.builder()
            .siteId(site.getId())
            .tenantId(tenantId)
            .publishedAt(site.getLastPublishedAt())
            .languageResults(languageResults)
            .overallSuccess(allSuccessful)
            .build();
    }
    
    private PublicationResult publishSiteForLanguage(Site site, Language language, PublishSiteCommand command) {
        
        // 1. Load language-specific content
        List<Content> publishedContent = contentRepository.findPublishedContentByLanguage(
            site.getTenantId(), language
        );
        
        // 2. Load language-specific menus
        List<Menu> menus = menuRepository.findBySiteAndLanguage(site.getId(), language);
        
        // 3. Validate content completeness
        SiteContentValidation validation = validateSiteContent(publishedContent, menus, language);
        if (!validation.isValid() && command.isStrictValidation()) {
            throw new SiteValidationException(language, validation.getErrors());
        }
        
        // 4. Generate language-specific site structure
        SiteStructure siteStructure = SiteStructure.builder()
            .language(language)
            .pages(publishedContent.stream()
                .filter(c -> c.getContentType().getName().equals("page"))
                .collect(Collectors.toList()))
            .blogPosts(publishedContent.stream()
                .filter(c -> c.getContentType().getName().equals("blog_post"))
                .collect(Collectors.toList()))
            .menus(menus)
            .build();
        
        // 5. Render site for language
        RenderedSite renderedSite = siteRenderingService.renderSite(
            site, 
            siteStructure, 
            language,
            command.getThemeFramework() // Thymeleaf, React, Vue, Angular
        );
        
        // 6. Generate language-specific SEO assets
        SeoAssets seoAssets = seoOptimizationService.generateSeoAssets(
            site, 
            publishedContent, 
            language
        );
        
        // 7. Deploy rendered site
        DeploymentResult deployment = deploymentService.deploySiteForLanguage(
            site.getTenantId(),
            language,
            renderedSite,
            seoAssets
        );
        
        return PublicationResult.builder()
            .language(language)
            .success(deployment.isSuccess())
            .contentCount(publishedContent.size())
            .menuCount(menus.size())
            .seoAssetsGenerated(seoAssets.getAssetCount())
            .deploymentUrl(deployment.getDeploymentUrl())
            .build();
    }
    
    // Use Case: Preview Site in Specific Language
    public SitePreviewResponseDto previewSite(PreviewSiteCommand command) {
        
        TenantId tenantId = tenantContextService.getCurrentTenantId();
        Language previewLanguage = Language.fromCode(command.getLanguageCode());
        
        // 1. Load site and content
        Site site = siteRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new SiteNotFoundException(tenantId));
        
        List<Content> content = contentRepository.findContentByLanguageIncludingDrafts(
            tenantId, previewLanguage
        );
        
        List<Menu> menus = menuRepository.findBySiteAndLanguage(site.getId(), previewLanguage);
        
        // 2. Generate preview with draft content
        SiteStructure previewStructure = SiteStructure.builder()
            .language(previewLanguage)
            .pages(content.stream()
                .filter(c -> c.getContentType().getName().equals("page"))
                .collect(Collectors.toList()))
            .blogPosts(content.stream()
                .filter(c -> c.getContentType().getName().equals("blog_post"))
                .collect(Collectors.toList()))
            .menus(menus)
            .build();
        
        // 3. Render preview
        RenderedSite previewSite = siteRenderingService.renderPreview(
            site,
            previewStructure,
            previewLanguage,
            command.getThemeFramework()
        );
        
        // 4. Generate temporary preview URL
        String previewUrl = previewService.generatePreviewUrl(
            tenantId, 
            previewLanguage, 
            command.getPreviewToken()
        );
        
        return SitePreviewResponseDto.builder()
            .siteId(site.getId())
            .language(previewLanguage)
            .previewUrl(previewUrl)
            .contentCount(content.size())
            .lastUpdated(content.stream()
                .map(Content::getUpdatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()))
            .build();
    }
}

// =================================================================
// 5. USER MANAGEMENT APPLICATION SERVICE
// =================================================================

@Service
@Transactional
public class UserApplicationService {
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final EmailService emailService;
    private final DomainEventPublisher eventPublisher;
    
    // Use Case: Create User with Language Preference
    public UserResponseDto createUser(CreateUserCommand command) {
        
        TenantId tenantId = tenantContextService.getCurrentTenantId();
        Language preferredLanguage = Language.fromCode(command.getPreferredLanguageCode());
        
        // 1. Validate tenant supports user's preferred language
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new TenantNotFoundException(tenantId));
        
        if (!tenant.supportsLanguage(preferredLanguage)) {
            throw new LanguageNotSupportedException(preferredLanguage, tenantId);
        }
        
        // 2. Check email uniqueness within tenant
        if (userRepository.existsByEmailAndTenantId(command.getEmail(), tenantId)) {
            throw new UserEmailAlreadyExistsException(command.getEmail(), tenantId);
        }
        
        // 3. Create user domain object
        User user = User.create(
            UserId.generate(),
            tenantId,
            new UserEmail(command.getEmail()),
            new UserName(command.getFullName()),
            UserRole.fromString(command.getRole()),
            preferredLanguage,
            passwordEncoder.encode(command.getPassword())
        );
        
        // 4. Save user
        userRepository.save(user);
        
        // 5. Send welcome email in user's preferred language
        emailService.sendWelcomeEmail(
            user.getEmail(),
            user.getFullName(),
            preferredLanguage,
            tenant.getSubdomain()
        );
        
        // 6. Publish domain events
        eventPublisher.publishEvents(user.getDomainEvents());
        user.clearDomainEvents();
        
        return userMapper.toResponseDto(user);
    }
    
    // Use Case: Login with Language Context
    public LoginResponseDto login(LoginCommand command) {
        
        // 1. Find user by email
        User user = userRepository.findByEmail(command.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException());
        
        // 2. Validate password
        if (!passwordEncoder.matches(command.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        
        // 3. Check user and tenant status
        if (!user.isActive()) {
            throw new UserNotActiveException(user.getId());
        }
        
        Tenant tenant = tenantRepository.findById(user.getTenantId())
            .orElseThrow(() -> new TenantNotFoundException(user.getTenantId()));
        
        if (!tenant.isActive()) {
            throw new TenantNotActiveException(tenant.getId());
        }
        
        // 4. Determine interface language
        Language interfaceLanguage = determineInterfaceLanguage(
            command.getPreferredLanguage(),
            user.getPreferredLanguage(),
            tenant.getDefaultLanguage()
        );
        
        // 5. Generate JWT token with language context
        String accessToken = jwtTokenService.generateAccessToken(
            user.getId(),
            user.getTenantId(),
            user.getRole(),
            interfaceLanguage
        );
        
        String refreshToken = jwtTokenService.generateRefreshToken(user.getId());
        
        // 6. Update last login
        user.updateLastLogin(interfaceLanguage);
        userRepository.save(user);
        
        // 7. Publish login event
        eventPublisher.publishEvent(new UserLoggedInEvent(
            user.getId(),
            user.getTenantId(),
            interfaceLanguage,
            LocalDateTime.now()
        ));
        
        return LoginResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .user(userMapper.toResponseDto(user))
            .tenant(tenantMapper.toBasicDto(tenant))
            .interfaceLanguage(interfaceLanguage.getCode())
            .expiresIn(jwtTokenService.getAccessTokenExpirationTime())
            .build();
    }
    
    private Language determineInterfaceLanguage(String requestedLang, Language userPref, Language tenantDefault) {
        
        // 1. Use requested language if valid and supported
        if (requestedLang != null && Language.isValidCode(requestedLang)) {
            Language requested = Language.fromCode(requestedLang);
            if (tenantRepository.supportsLanguage(tenantContextService.getCurrentTenantId(), requested)) {
                return requested;
            }
        }
        
        // 2. Fall back to user preference
        if (userPref != null) {
            return userPref;
        }
        
        // 3. Fall back to tenant default
        return tenantDefault;
    }
}

// =================================================================
// SUPPORTING CLASSES AND PATTERNS
// =================================================================

// Command Pattern Examples
@Data
@Builder
public class CreateContentCommand {
    private ContentTypeId contentTypeId;
    private String title;
    private String slug;
    private String contentData; // JSON
    private String languageCode;
    private boolean autoOptimizeSeo;
    private LocalDateTime scheduledPublishAt;
}

@Data
@Builder
public class CreateTranslationCommand {
    private ContentId originalContentId;
    private String targetLanguageCode;
    private String translatedTitle;
    private String translatedSlug;
    private String translatedContentData; // JSON
    private boolean autoTranslated;
}

// Response DTO Examples
@Data
@Builder
public class ContentResponseDto {
    private String id;
    private String contentTypeId;
    private String title;
    private String slug;
    private String language;
    private String status;
    private LocalDateTime publishedAt;
    private Map<String, Object> contentData;
    private SeoMetadataDto seo;
    private boolean isFallbackLanguage;
    private String requestedLanguage;
    private List<String> availableLanguages;
}

@Data
@Builder
public class BulkTranslationResponseDto {
    private int totalProcessed;
    private int successCount;
    private int failureCount;
    private List<TranslationResult> results;
    private Duration processingTime;
}

// Domain Event Examples
public class ContentCreatedEvent extends DomainEvent {
    private final ContentId contentId;
    private final Language language;
    private final TenantId tenantId;
    private final UserId createdBy;
    
    // Constructor and getters
}

public class TranslationCompletedEvent extends DomainEvent {
    private final ContentId originalContentId;
    private final ContentId translatedContentId;
    private final Language sourceLanguage;
    private final Language targetLanguage;
    private final double qualityScore;
    
    // Constructor and getters
}

---
alwaysApply: true
---
you are an expert Angular programmer using TypeScript, Angular 19 and  that focuses on producing clear, readable code.

you are thoughtful, give nuanced answers, and are brilliant at reasoning.

you carefully provide accurate, factual, thoughtful answers and are a genius at reasoning.

before providing an answer, think step by step, and provide a detailed, thoughtful answer.

if you need more information, ask for it.

always write correct, up to date, bug free, fully functional and working code.

focus on performance, readability, and maintainability.

before providing an answer, double check your work

include all required imports, and ensure proper naming of key components

do not nest code more than 2 levels deep

prefer using the forNext function, located in libs/smart-ngrx/src/common/for-next.function.ts instead of for(let i;i < length;i++), forEach or for(x of y)

code should obey the rules defined in the .eslintrc.json, .prettierrc, .htmlhintrc, and .editorconfig files

functions and methods should not have more than 4 parameters

functions should not have more than 50 executable lines

lines should not be more than 80 characters

when refactoring existing code, keep jsdoc comments intact

be concise and minimize extraneous prose.

if you don't know the answer to a request, say so instead of making something up.