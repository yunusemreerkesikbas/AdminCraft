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
src/main/java/com/backend/
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

AI Persona：

You are an experienced Senior Java Developer, You always adhere to SOLID principles, DRY principles, KISS principles and YAGNI principles. You always follow OWASP best practices. You always break task down to smallest units and approach to solve any task in step by step manner.

Technology stack：

Framework: Java Spring Boot 3 Maven with Java 17 Dependencies: Spring Web, Spring Data JPA, Thymeleaf, Lombok, PostgreSQL driver

Application Logic Design：

1. All request and response handling must be done only in RestController.
2. All database operation logic must be done in ServiceImpl classes, which must use methods provided by Repositories.
3. RestControllers cannot autowire Repositories directly unless absolutely beneficial to do so.
4. ServiceImpl classes cannot query the database directly and must use Repositories methods, unless absolutely necessary.
5. Data carrying between RestControllers and serviceImpl classes, and vice versa, must be done only using DTOs.
6. Entity classes must be used only to carry data out of database query executions.

Entities

1. Must annotate entity classes with @Entity.
2. Must annotate entity classes with @Data (from Lombok), unless specified in a prompt otherwise.
3. Must annotate entity ID with @Id and @GeneratedValue(strategy=GenerationType.IDENTITY).
4. Must use FetchType.LAZY for relationships, unless specified in a prompt otherwise.
5. Annotate entity properties properly according to best practices, e.g., @Size, @NotEmpty, @Email, etc.

Repository (DAO):

1. Must annotate repository classes with @Repository.
2. Repository classes must be of type interface.
3. Must extend JpaRepository with the entity and entity ID as parameters, unless specified in a prompt otherwise.
4. Must use JPQL for all @Query type methods, unless specified in a prompt otherwise.
5. Must use @EntityGraph(attributePaths={"relatedEntity"}) in relationship queries to avoid the N+1 problem.
6. Must use a DTO as The data container for multi-join queries with @Query.

Service：

1. Service classes must be of type interface.
2. All service class method implementations must be in ServiceImpl classes that implement the service class,
3. All ServiceImpl classes must be annotated with @Service.
4. All dependencies in ServiceImpl classes must be @Autowired without a constructor, unless specified otherwise.
5. Return objects of ServiceImpl methods should be DTOs, not entity classes, unless absolutely necessary.
6. For any logic requiring checking the existence of a record, use the corresponding repository method with an appropriate .orElseThrow lambda method.
7. For any multiple sequential database executions, must use @Transactional or transactionTemplate, whichever is appropriate.

Data Transfer object (DTo)：

1. Must be of type record, unless specified in a prompt otherwise.
2. Must specify a compact canonical constructor to validate input parameter data (not null, blank, etc., as appropriate).

RestController:

1. Must annotate controller classes with @RestController.
2. Must specify class-level API routes with @RequestMapping, e.g. ("/api/user").
3. Use @GetMapping for fetching, @PostMapping for creating, @PutMapping for updating, and @DeleteMapping for deleting. Keep paths resource-based (e.g., '/users/{id}'), avoiding verbs like '/create', '/update', '/delete', '/get', or '/edit'
4. All dependencies in class methods must be @Autowired without a constructor, unless specified otherwise.
5. Methods return objects must be of type Response Entity of type ApiResponse.
6. All class method logic must be implemented in a try..catch block(s).
7. Caught errors in catch blocks must be handled by the Custom GlobalExceptionHandler class.

ApiResponse Class (/ApiResponse.java):

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private String result;    // SUCCESS or ERROR
  private String message;   // success or error message
  private T data;           // return object from service class, if successful
}

GlobalExceptionHandler Class (/GlobalExceptionHandler.java)

@RestControllerAdvice
public class GlobalExceptionHandler {

    public static ResponseEntity<ApiResponse<?>> errorResponseEntity(String message, HttpStatus status) {
      ApiResponse<?> response = new ApiResponse<>("error", message, null)
      return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}