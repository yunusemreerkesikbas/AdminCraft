# Backend Security Audit — 2026-04-25

**Scope**: `backend/` (Spring Boot 3.3.5, Java 21, multi-tenant: `platform_management` + `ac_subdomain_{id}`)
**Branch**: `feature/CMS-224` · **Tracked in**: PR #297
**Katmanlar**: OWASP Top 10 (kod düzeyi) · Multi-tenant izolasyon · Runtime/config güvenliği
**Yöntem**: 3 paralel `Explore` agent + 1 `code-reviewer` agent + manuel `Read` doğrulama
**Bulgu sayısı**: **P0: 5** · **P1: 14** · **P2: 11** · **P3: 9**

> Kapsam dışı: dependency/CVE taraması, frontend (Angular/Next.js), deployment (Cloudflare/Traefik/DigitalOcean). Frontend gerekirse ayrı raporda.

### Güncelleme — PR #297 (kod durumu)

Bu belge **2026-04-25** tarihli kod incelemesinin **tarihsel** kaydıdır; aşağıdaki maddeler sonradan uygulanan sertleştirmelerle **kısmen veya tamamen** kapanmış veya mimari olarak değişmiş olabilir. Güncel davranış için `docs/global/security-multi-tenancy.md`, `docs/modules/impex.md`, `docs/modules/config-control-panel.md`, `docs/global/environment-configuration.md` ve ilgili Spring sınıflarına bakın.

- **SEC-001 (OTP bypass):** `OTP_BYPASS_CODE` için boş YAML varsayılanı; `prod` profilinde `OtpServiceImpl` içinde hem panel hem env bypass yollarının devre dışı bırakılması; sabit süreli karşılaştırma; global bypass kodu politikası (`ConfigGlobalPropertiesAdminServiceImpl`).
- **SEC-002 / platform ImpEx:** `TenantFilter` ve `ImpExServiceImpl` — SUPER_ADMIN dahil **zorunlu tenant bağlamı**; platform veritabanına ImpEx ile düşme kaldırıldı.
- **SEC-100:** Hassas tablolar için deny-list ve şema/tırnaklı tablo adı çözümlemesi.
- **SEC-101:** Yorum temizleme + güvenli ayırıcı mantığı (koda bakın).
- **SEC-103:** `impex_audit` kalıcı kayıt + `correlation_id` indeks migrasyonu.
- **SEC-114 (IP):** Kiracı herkese açık iletişim uç noktasında `CF-Connecting-IP` için `app.security.*` yapılandırması ve hız sınırları.
- **Hâlâ açık / ayrı iş** örnekleri: SEC-105 (config admin PATCH rate-limit), SEC-107 (platform demo-requests), çoklu replika için cluster-bilinçli rate limit, medya magic-byte vb. — bu rapordaki öncelik tablosu güncellenmemiştir.

---

## 1. Executive Summary

Mimari sağlam: database-per-tenant izolasyon Hibernate seviyesinde doğru kurulmuş, `TenantContext` ThreadLocal'ları `try-finally` ile temizleniyor, `AsyncConfig.TenantContextTaskDecorator` async path'te context'i taşıyor, BCrypt cost 12 ile sektör eşiğinin üstünde, AES/GCM secret encryption'ı pattern'i doğru yazılmış, exception mesajları 500 char truncate ediliyor, audit log + correlationId her response'ta. Bunlar bu raporda **vurgulanmıyor** — gözle görülen pozitifler ve devam ettirilmeli.

Ancak **5 adet P0 (şu an exploitable / stage'de açık)** zafiyet var. **En kritik 3'ü iç içe geçmiş bir privilege-escalation chain oluşturuyor**:

1. **`OTP_BYPASS_CODE:123456` stage'de default açık** (SEC-001) → herhangi bir email için OTP bilen attacker stage authentication'ı tamamen geçer.
2. **ImpEx SUPER_ADMIN bypass + platform-DB fallback** (SEC-002) → SUPER_ADMIN, ImpEx üzerinden `platform_management` veritabanına `INSERT/UPDATE` çalıştırabilir. `BLOCKED_KEYWORDS` listesi `DELETE/DROP/ALTER/...` engelliyor ama `UPDATE platform_admin_users SET role='SUPER_ADMIN' WHERE email='attacker@x'` veya benzeri **role escalation** çalışır.
3. **JWT-tenant vs header-tenant cross-check eksikliği** (SEC-003) → bir tenant'ın geçerli JWT'si + başka tenant'ın `X-Tenant-ID` header'ı ile çoğu tenant-scoped endpoint cross-tenant veriye erişir.

Ardından **Config Control Panel rate-limit eksikliği** (SEC-105) compromised admin'in 10-saniyede platform-wide reCAPTCHA'yı kapatıp brute-force kapısını açmasına izin veriyor; **public demo-request endpoint** (SEC-107) reCAPTCHA disable durumunda mail-bomb relay'e dönüşüyor; **eksik HTTP security header'lar** (SEC-117) admin paneli için clickjacking + MIME sniffing yüzeyini açıyor.

### İlk 48 Saat — Acil Aksiyonlar

| #   | Aksiyon                                                                                                                                                                               | Etki                              |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------- |
| 1   | `application-stage.yml:51` → `${OTP_BYPASS_CODE:}` (default sil), stage env'e güçlü değer set et veya feature flag ile prod-out                                                       | SEC-001 kapanır                   |
| 2   | `TenantFilter.java:90-94` ImpEx SUPER_ADMIN bypass'ı kaldır; ImpEx için `X-Tenant-ID` zorunlu olsun + ImpEx parser'ında `users.role`/`platform_admin_users` allow-list/deny-list ekle | SEC-002 + SEC-100 zincirini kırar |
| 3   | `TenantFilter` her authenticated request için `JWT.tenantId == TenantContext.tenantId` cross-check'ini global olarak ekle (`/auth/**`, `/platform/**`, `/cms/**` hariç)               | SEC-003 + SEC-006 kapanır         |
| 4   | `SecurityConfig` startup'ında `JWT_SECRET == default` ise prod/stage'de `IllegalStateException` fırlat (EncryptionService deseniyle paralel)                                          | SEC-004 kapanır                   |
| 5   | `ConfigAdminRecaptchaController` PATCH'lerine `@RateLimiter("configAdmin")` 5 req/min + step-up auth flag                                                                             | SEC-105 kapanır                   |

Detaylar aşağıda.

---

## 2. Multi-Tenant İzolasyon Matrisi (özet)

| Path / Endpoint Sınıfı                                    | Auth                            | TenantContext kaynağı                                  | JWT vs header check          | Rate limit                       | İzolasyon notu                                               |
| --------------------------------------------------------- | ------------------------------- | ------------------------------------------------------ | ---------------------------- | -------------------------------- | ------------------------------------------------------------ |
| `/api/auth/**`                                            | None (login/refresh/verify-otp) | None                                                   | —                            | OTP: 3/5dk per email (in-memory) | Platform-wide login flow                                     |
| `/api/platform/**` (admin)                                | `SUPER_ADMIN`                   | None (platform DB)                                     | —                            | yok                              | TenantFilter `isPlatformEndpoint` ile context kurulmuyor     |
| `/api/platform/cms/config`                                | None                            | None                                                   | —                            | yok                              | Public reCAPTCHA flag                                        |
| `/api/platform/public/demo-requests`                      | None                            | None                                                   | —                            | **yok** (sadece reCAPTCHA)       | reCAPTCHA disable iken abuse                                 |
| `/api/platform/public/newsletter/**`                      | None                            | None                                                   | —                            | honeypot + 1s-1h pencere         | OK ama re-send abuse var                                     |
| `/api/cms/**` (delivery)                                  | None                            | header/hostname                                        | —                            | **yok** (Traefik bekleniyor)     | Per-tenant DoS riski                                         |
| `/api/tenants/{id}/...`                                   | `SUPER_ADMIN`                   | None                                                   | —                            | bazılarında 20/dk                | Path id direkt tenant ID                                     |
| `/api/tenants/current/modules`                            | TENANT_ADMIN/VIEWER             | header                                                 | **var** (yalnız bu endpoint) | yok                              | İyi örnek                                                    |
| `/api/tenants/current/**` (diğer)                         | TENANT_ADMIN/VIEWER             | header                                                 | **yok**                      | yok                              | **SEC-003 hedefi**                                           |
| `/api/sites`, `/api/products`, `/api/pages`, `/api/media` | TENANT_ADMIN/VIEWER             | header                                                 | **yok**                      | yok                              | **SEC-003 hedefi**                                           |
| `/api/impex`                                              | TENANT_ADMIN / SUPER_ADMIN      | TenantFilter `:90-94` SUPER_ADMIN bypass → platform DB | **yok**                      | 5/dk JVM-lokal                   | **SEC-002 hedefi**                                           |
| `/api/config/admin/**`                                    | `CONFIG_TENANT_ADMIN`           | header (tenant-scoped)                                 | yok                          | **yok**                          | **SEC-105 hedefi**                                           |
| `/api/media/files/**`                                     | None (permitAll)                | yok                                                    | —                            | yok                              | **SEC-009 hedefi** (private dosyalar da public erişilebilir) |
| `/actuator/**`                                            | None (permitAll)                | yok                                                    | —                            | yok                              | **SEC-005 hedefi**                                           |

---

## 3. Bulgular

### 3.1 — P0 Bulgular (şu an exploitable / stage'de açık)

#### [P0] SEC-001: `OTP_BYPASS_CODE` stage profilinde default `123456` ile kayıtlı

**Kategori**: OWASP A07 — Identification & Authentication Failures
**Konum**: `backend/src/main/resources/application-stage.yml:51`
**Özet**: `${OTP_BYPASS_CODE:123456}` ifadesindeki Spring placeholder default'u, ortam değişkeni set edilmediğinde `123456` kodunu **canlı bypass değeri** olarak ayarlar. Stage'de geçerli email + bilinen OTP değeri ile 2FA atlanır.

**Exploit senaryosu**:

1. Attacker stage'deki bir admin email'ini bulur (LinkedIn / leak / `/api/users` keşfi).
2. `/api/auth/login` ile email + bilinen test password veya rememberMe akışını dener; tempToken alır.
3. `/api/auth/verify-otp` endpoint'ine OTP olarak `123456` gönderir; backend `OtpProperties.bypassCode` eşleşmesi nedeniyle kabul eder.
4. Tam authentication elde edilir; SUPER_ADMIN ise tüm tenant kontrol paneli açılır.

**PoC**:

```http
POST /api/auth/verify-otp HTTP/1.1
Host: stage.craftive.io
Content-Type: application/json
X-Tenant-ID: 1

{ "email":"admin@tenant.io", "otp":"123456", "tempToken":"<from /login>" }
```

**Root cause**: Spring `${VAR:default}` syntax'ı VAR set edilmediğinde default'a düşer. CI/CD'de env var unutulduğu durumda default aktif. Hardcoded değer git geçmişinde de kalıcı.

**Fix (diff)**:

```diff
 app:
   jwt:
     secret: ${JWT_SECRET}
   otp:
-    bypass-code: ${OTP_BYPASS_CODE:123456}
+    bypass-code: ${OTP_BYPASS_CODE:}
```

Ek: `OtpProperties` bean'inde fail-fast:

```java
@PostConstruct
void validate() {
    boolean isProd = Arrays.asList(env.getActiveProfiles()).contains("prod");
    if (isProd && bypassCode != null && !bypassCode.isBlank()) {
        throw new IllegalStateException("OTP bypass-code must be empty in prod");
    }
}
```

**Doğrulama**: `curl -X POST stage/api/auth/verify-otp -d "{...,\"otp\":\"123456\"}"` → 401 beklenir.
**Kapsam etkisi**: Stage tüm authentication.

---

#### [P0] SEC-002: `TenantFilter` SUPER_ADMIN için ImpEx tenant context bypass + `ImpExServiceImpl` platform DB fallback → role escalation

**Kategori**: OWASP A01 — Broken Access Control / Multi-tenant izolasyon
**Konum**:

- `backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java:90-94`
- `backend/src/main/java/com/backend/application/service/impex/ImpExServiceImpl.java:77-88`

**Özet**: `TenantFilter` SUPER_ADMIN için `/api/impex` path'inde tenant resolve etmeden filterChain'e geçiyor. ImpEx servisi de tenant context boşsa **`platformDataSource`'a sessizce düşüyor**. Sonuç: SUPER_ADMIN tek HTTP isteği ile `platform_management` veritabanına `INSERT/UPDATE` çalıştırabilir; `BLOCKED_KEYWORDS` listesi `DELETE/DROP/...` engelliyor ama `UPDATE` ile rol değiştirme açık kapı.

**Exploit senaryosu**:

1. Compromised veya kötü-niyetli SUPER_ADMIN `/api/impex/execute`'a bağlanır.
2. Tenant header göndermez; TenantFilter L90-94 short-circuit çalışır, context kurulmaz.
3. `ImpExServiceImpl.resolveDataSource` (L79-82) `currentUser.isSuperAdmin()` true gördüğü için `platformDataSource` döner.
4. Payload: `-- #CRAFTIVE_IMPEX\nUPDATE platform_admin_users SET role='SUPER_ADMIN' WHERE email='attacker@x.com';`
5. `platform_admin_users.role` üzerinden ek SUPER_ADMIN üretilir veya tenant rotasının kontrolü ele geçirilir.

**PoC**:

```http
POST /api/impex/execute HTTP/1.1
Authorization: Bearer <SUPER_ADMIN_JWT>
Content-Type: application/json

{ "sql": "-- #CRAFTIVE_IMPEX\nINSERT INTO tenants (subdomain,database_name,status) VALUES ('rogue','ac_rogue_99','ACTIVE');" }
```

**Root cause**:

- `TenantFilter.java:90-94`: `if (path.startsWith("/api/impex") && isSuperAdmin) { ... return; }` — platform-level ImpEx desteği için ama context kontrolü tamamen elimine ediliyor.
- `ImpExServiceImpl.java:79-82`: `if ((tenantDbName == null || tenantDbName.isBlank()) && currentUser.isSuperAdmin()) return platformDataSource;` — implicit fallback. Audit log yok (yalnız `log.info`); hangi tablo/satır değişti kayıt edilmiyor (bkz. SEC-103).
- `ImpExServiceImpl.java:31-34`: allow-list `INSERT/UPDATE/SELECT` — `users.role`, `platform_admin_users.role` gibi sensitive tabloları korumuyor.

**Fix (diff)** (TenantFilter):

```diff
-      if (path.startsWith("/api/impex") && isSuperAdmin) {
-        log.warn("ImpEx bypass for superAdmin - path: {}", path);
-        filterChain.doFilter(request, response);
-        return;
-      }
+      // ImpEx: SUPER_ADMIN must explicitly target a tenant via header.
+      // Platform-DB ImpEx is rejected; DBA migration kullanır.
```

**Fix (diff)** (ImpExServiceImpl):

```diff
 private DataSource resolveDataSource(Locale locale) {
   String tenantDbName = tenantContext.getTenantDbName();
-  if ((tenantDbName == null || tenantDbName.isBlank()) && currentUser.isSuperAdmin()) {
-    log.info("ImpEx using platform database (SUPER_ADMIN, no tenant context)");
-    return platformDataSource;
-  }
   if (tenantDbName != null && !tenantDbName.isBlank()) {
     return multiTenantConnectionProvider.getDataSource(tenantDbName);
   }
   throw new IllegalStateException(
     messageSource.getMessage("impex.error.tenant.required", null, locale));
 }
```

Ek tablo deny-list:

```java
private static final Set<String> SENSITIVE_TABLES = Set.of(
    "platform_admin_users", "users", "tenants", "tenant_modules",
    "config_property", "platform_settings");
```

**Doğrulama**: SUPER_ADMIN JWT ile `POST /api/impex/execute` (header'sız) → 400 "tenant required" beklenir.
**Kapsam etkisi**: Platform takeover. P0 maksimum etki.

---

#### [P0] SEC-003: JWT `tenantId` claim ile `X-Tenant-ID` header'ı global cross-check eksikliği → cross-tenant veri erişimi

**Kategori**: OWASP A01 — Broken Access Control / Multi-tenant izolasyon
**Konum**:

- `backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java:96-128` (header → context)
- Yalnız `TenantController.java:197-224` (`/current/modules`) JWT-header eşleşmesini kontrol ediyor.

**Özet**: `TenantFilter` tenant'ı header / hostname'den çözüyor; JWT'nin `tenantId` claim'i ile karşılaştırma yapılmıyor. Tenant A'nın geçerli JWT'si + Tenant B'nin `X-Tenant-ID` header'ı ile yapılan istekte `TenantContext` Tenant B'ye işaret eder, JPA Tenant B'nin DB'sine query atar; servislerin çoğu cross-check yapmadığı için cevap döner.

**Exploit senaryosu**:

1. Attacker kendi tenant A'sının TENANT_ADMIN hesabıyla login olur, geçerli JWT alır.
2. Tenant B'nin tenant ID veya subdomain'ini öğrenir (`/api/platform/cms/config`, public DNS, vs.).
3. Tenant A JWT + `X-Tenant-ID: <tenant_B_id>` header ile `/api/sites`, `/api/products`, `/api/users`, `/api/media` çağırır.
4. JPA Tenant B'nin DB'sine query atar (Hibernate `CurrentTenantIdentifierResolver` ThreadLocal'dan B döner).
5. Tenant B verisi okunur / yazılır.

**PoC**:

```http
GET /api/sites HTTP/1.1
Authorization: Bearer <tenant_A_TENANT_ADMIN_JWT>
X-Tenant-ID: 42
Accept: application/json
```

**Root cause**: TenantFilter L96-128 yalnız header/hostname'den çözüm yapıyor. `JwtAuthenticationFilter` JWT'yi `SecurityContextHolder`'a yerleştiriyor ama TenantFilter onu okumuyor. `@PreAuthorize("hasRole(...)")` rolün varlığını test ediyor, hangi tenant'a ait olduğunu test etmiyor.

**Fix (diff)** — TenantFilter'a global cross-check (auth/platform/public hariç):

```diff
       tenantContext.setTenantId(String.valueOf(tenant.getId()));
       tenantContext.setTenantDbName(tenant.getDatabaseName());
+
+      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
+      if (auth != null && auth.isAuthenticated()
+          && !(auth instanceof AnonymousAuthenticationToken)
+          && !isCrossTenantAllowedRole(auth)) {        // SUPER_ADMIN istisna
+        Long jwtTenantId = jwtPrincipal(auth).getTenantId();
+        if (jwtTenantId != null && !jwtTenantId.equals(tenant.getId())) {
+          log.warn("Tenant mismatch: jwt={} header={}", jwtTenantId, tenant.getId());
+          response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
+          return;
+        }
+      }
```

**Doğrulama**:

- Tenant A JWT + Tenant B header → 403 beklenir.
- SUPER_ADMIN JWT + Tenant B header → 200 (kabul, çünkü SUPER_ADMIN platform-wide).
- Aynı tenant JWT + header → 200.

**Kapsam etkisi**: TÜM tenant-scoped endpoint'ler (`/api/sites`, `/api/products`, `/api/users`, `/api/pages`, `/api/media`, `/api/categories`, `/api/components`, …). Kitlesel cross-tenant veri ifşası ve mutasyonu.

---

#### [P0] SEC-004: `application.yml:9` JWT secret hardcoded + prod startup'ta validation yok

**Kategori**: OWASP A02 — Cryptographic Failures
**Konum**: `backend/src/main/resources/application.yml:9`, `backend/src/main/java/com/backend/infrastructure/security/JwtTokenProvider.java:20-26`

**Özet**: `app.jwt.secret` dev profilinde literal string `MyVerySecureJWTSecretKeyForCraftiveApplication2024WithMoreThan32Characters!`. Prod/stage `${JWT_SECRET}` ile override ediyor; ancak ortam değişkeni boş kalır veya profile yanlış set edilirse default sızar. `EncryptionService` `RECAPTCHA_MASTER_KEY` için startup-time placeholder kontrolü yapıyor; JWT için aynı koruma yok.

**Exploit senaryosu**:

1. Prod deploy'unda `JWT_SECRET` env var'ı CI/CD secret'lardan eksik kaldı veya dev profile yanlışlıkla aktive edildi.
2. Hardcoded literal git'te public/private repo'da bulunabilir.
3. Attacker literal'i bulur, kendi JWT'sini imzalar (`role=SUPER_ADMIN`, `tenantId=any`).
4. `JwtTokenProvider.validateToken` true döner; tüm authorization geçer.

**PoC**:

```python
import jwt, time
SECRET = "MyVerySecureJWTSecretKeyForCraftiveApplication2024WithMoreThan32Characters!"
now = int(time.time())
token = jwt.encode({"sub":"x@x","role":"SUPER_ADMIN","userId":1,"tenantId":1,
                    "type":"access","iat":now,"exp":now+3600},
                   SECRET, algorithm="HS512")
```

**Root cause**:

- `application.yml:9` literal default git'te kalıcı.
- `JwtTokenProvider.java:20-26` constructor secret üstünde uzunluk veya placeholder validation yapmıyor.

**Fix (diff)**:

```diff
 app:
   jwt:
-    secret: MyVerySecureJWTSecretKeyForCraftiveApplication2024WithMoreThan32Characters!
+    secret: ${JWT_SECRET:DEV_ONLY_PLACEHOLDER_DO_NOT_USE_IN_PROD_______________________}
     expiration: ${JWT_EXPIRATION:86400000}
```

`JwtTokenProvider`:

```diff
 public JwtTokenProvider(JwtProperties jwtProperties, Environment env) {
+  String secret = jwtProperties.getSecret();
+  boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");
+  if (!isDev && secret.startsWith("DEV_ONLY_PLACEHOLDER")) {
+    throw new IllegalStateException("JWT_SECRET must be set in non-dev profiles");
+  }
+  if (secret.getBytes().length < 64) {
+    throw new IllegalStateException("JWT secret must be >= 64 bytes (HS512)");
+  }
   this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
   ...
 }
```

**Doğrulama**: `JWT_SECRET=` (boş) + prod profile başlat → startup fail. Dev profile başlat → çalışır.
**Kapsam etkisi**: Secret leak / yanlış konfig durumunda **tüm authentication kırılır**.

---

#### [P0] SEC-100: ImpEx `INSERT/UPDATE` allow-list'i sensitive tablolarda privilege escalation'a açık

**Kategori**: OWASP A01 — Broken Access Control
**Konum**: `ImpExServiceImpl.java:31-34, 121-153`
**Özet**: `ALLOWED_KEYWORDS = {INSERT, UPDATE, SELECT}`; tablo seviyesi koruma yok. TENANT_ADMIN tenant DB'sinde `UPDATE users SET role='SUPER_ADMIN' WHERE id=...` veya `INSERT INTO users (email,role,...) VALUES ('attacker@x.com','SUPER_ADMIN',...)` çalıştırabilir. SUPER_ADMIN platform DB'sinde aynı pattern (SEC-002 chain).

**PoC**:

```http
POST /api/impex/execute
Authorization: Bearer <TENANT_ADMIN_JWT>
X-Tenant-ID: <attacker_tenant>

{ "sql":"-- #CRAFTIVE_IMPEX\nUPDATE users SET role='TENANT_ADMIN' WHERE email='lowpriv@x.com';" }
```

**Fix**: SEC-002 fix'inde verilen `SENSITIVE_TABLES` deny-list + statement parsing AST tabanlı.

---

### 3.2 — P1 Bulgular (auth-gated kitlesel etki / kalıcı veri ifşası)

#### [P1] SEC-005: `/actuator/**` `permitAll` — health/info/env bilgi sızıntısı

**Kategori**: OWASP A05 — Security Misconfiguration
**Konum**: `backend/src/main/java/com/backend/infrastructure/config/SecurityConfig.java:56`
**Özet**: `requestMatchers("/actuator/**").permitAll()` tüm actuator endpoint'lerini auth-suz erişilebilir kılıyor. Stage'de `management.endpoints.web.exposure.include: health,info` (uygulama-stage.yml:83), prod'da `health` (uygulama-prod.yml:85). Yine de prod'da `/actuator/health` detaylı build/version bilgisi sızdırabilir; stage'de `info` daha geniş bilgi.
**Exploit**: Attacker `/actuator/info` → build version, git commit, environment fingerprint → hedef CVE seçimi.
**Fix (diff)**:

```diff
- .requestMatchers("/actuator/**").permitAll()
+ .requestMatchers("/actuator/health").permitAll()
+ .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN")
```

Ayrıca `application-prod.yml` üzerinde `management.endpoint.health.show-details: never` doğrula.
**Doğrulama**: `curl prod/actuator/info` → 401/403 beklenir.

---

#### [P1] SEC-006: `TenantFilter` hostname fallback chain (`X-Forwarded-Host` → `Origin` → `Referer`) → header spoofing ile cross-tenant

**Kategori**: Multi-tenant izolasyon / OWASP A05
**Konum**: `backend/src/main/java/com/backend/infrastructure/tenant/TenantFilter.java:184-239`
**Özet**: TenantFilter, header'da tenant yoksa hostname'den çıkarım yapıyor; sırasıyla `X-Forwarded-Host`, `Origin`, `Referer` deniyor. Reverse proxy bu header'ları her zaman tenant boundary'sini yansıtmaz. Attacker, public CMS endpoint'lerinde Origin/Referer header'ını forge ederek **tenant routing'i kontrol edebilir**.
**Exploit**: Public `/api/cms/site` çağrısında `Origin: https://victim-tenant.craftive.io` set et → backend victim tenant DB'sinden CMS verisini döner. Auth-gated endpoint'lerde SEC-003 ile birlikte kullanılınca cross-tenant data path'i.
**Fix (diff)** — `Origin`/`Referer` fallback'ini kaldır, yalnız reverse-proxy'nin set ettiği `X-Forwarded-Host`'a güven (Traefik / nginx config'de `proxy_set_header` zorunluluğu):

```diff
-    // 2. Check Origin header (browser sends this on CORS/fetch requests)
-    if (hostname == null) { ... }
-    // 3. Check Referer header
-    if (hostname == null) { ... }
+    // Origin / Referer attacker-controlled olabilir; sadece XFH veya serverName.
```

Ek: `app.tenant.trusted-proxies` whitelist (örn. internal CIDR) kontrolü TenantFilter girişine eklenebilir.

---

#### [P1] SEC-007: `JwtTokenProvider.validateToken` token tipi/type claim'ini doğrulamıyor → refresh token access endpoint'inde geçer

**Kategori**: OWASP A02 / A07
**Konum**: `backend/src/main/java/com/backend/infrastructure/security/JwtTokenProvider.java:172-191`, `JwtAuthenticationFilter` (validate çağrısı)
**Özet**: `validateToken` yalnız imza ve süre kontrol ediyor; `claim("type")` (`access` vs `refresh`) doğrulamıyor. `JwtAuthenticationFilter` Authorization header'ından gelen herhangi bir geçerli imzalı JWT'yi kabul ederse, `refresh` token'ı `access` yerine kullanılabilir. Refresh token'lar genelde daha uzun TTL'li ve farklı saklama yerinde olduğu için XSS/leak'te risk büyür.
**Fix (diff)**:

```diff
 public boolean validateToken(String token) {
   try {
-    Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
-    return true;
+    Claims c = Jwts.parser().verifyWith(secretKey).build()
+                   .parseSignedClaims(token).getPayload();
+    return "access".equals(c.get("type", String.class));
   } catch (...)
 }
```

`/api/auth/refresh` endpoint'inde ayrı `validateRefreshToken` helper'ı kullanılmalı.
**Doğrulama**: Refresh token'ı `Authorization: Bearer …` ile `/api/sites` çağrısı → 401 beklenir.

---

#### [P1] SEC-008: Refresh token + rememberMe TTL uzun + revocation/rotation yok

**Kategori**: OWASP A07
**Konum**: `application.yml:11` (refresh 7d), `JwtTokenProvider.java:201-207`
**Özet**: Refresh token 7 gün, rememberMe daha uzun. Stateless JWT olduğundan revocation listesi yok; çalınan refresh token TTL bitene kadar kullanılabilir.
**Fix**:

- Refresh token TTL'i 24-48 saate düşür; rememberMe için **refresh-token rotation** uygula (her refresh'te eski'yi DB'de invalidate et).
- `RefreshTokenRecord` tablosu (id, userId, tokenHash, expiresAt, revokedAt). Logout'ta revoke; her rotation'da eski'yi revokedAt set et.

---

#### [P1] SEC-009: `/media/files/**` permitAll → `isPublic=false` media public erişilebilir

**Kategori**: OWASP A01 — Broken Access Control / IDOR
**Konum**: `SecurityConfig.java:60`, `MediaController.java:338-367` (Phase-1)
**Özet**: SecurityConfig `/media/files/**` tamamen permitAll. Media kayıtlarında `isPublic` flag olsa bile authentication kontrolü yok, dolayısıyla **private/draft medya UUID'sini bilen herkes erişebilir**. UUID 122-bit entropy yüksek ama leak senaryoları (logs, share, metadata) var.
**Fix (diff)** (SecurityConfig):

```diff
- .requestMatchers("/media/files/**").permitAll()
+ // private medya MediaController içinde authz kontrolü yapacak
+ // public medya için CDN delivery tercih edilmeli (signed URL)
```

`MediaController.getFile` içine: `if (!media.isPublic() && !securityHelper.canAccessMedia(media)) → 403`.

---

#### [P1] SEC-010: `EncryptionService` legacy AES/ECB decryption fallback hâlâ açık

**Kategori**: OWASP A02 — Cryptographic Failures
**Konum**: `backend/src/main/java/com/backend/infrastructure/security/EncryptionService.java:85-98` (Phase-1)
**Özet**: GCM birincil; eski AES/ECB cipher'ı ile encrypt edilmiş kayıtların decrypt'i için ECB cipher hâlâ kodda. Yeni encrypt ECB ile yapılmasa da ECB ile şifreli rotated key'ler hâlâ kullanılıyor olabilir; ECB pattern leak'e açık.
**Fix**: Tüm `config_property` ve `tenant_settings` tablolarında ECB-encrypted kayıtları tek seferlik migration ile GCM'ye re-encrypt et, ECB decrypt path'ini sil:

```diff
- // Legacy ECB fallback for backward compat
- try { ...AES/ECB... } catch ...
+ // ECB removed — re-encrypt migration completed in V…__reencrypt_secrets.sql
```

---

#### [P1] SEC-101: ImpEx parser line-comment'leri siliyor ama block-comment (`/* */`) silmiyor

**Kategori**: OWASP A03 — Injection
**Konum**: `ImpExServiceImpl.java:110-119, 155-172`
**Özet**: `trimStatement` line-line `--` yorumlarını siliyor. `/* */` bloğu silinmiyor. `checkBlocked` `upper.startsWith(blocked)` kullandığı için `/* foo */DROP TABLE x` payload'u üst-string'de "/" ile başlar, bloklanmaz. Ayrıca MySQL conditional comment `/*!50000 ALTER ...*/` MySQL parser tarafından **execute edilir** — keyword bypass yolu.
**PoC**:

```sql
-- #CRAFTIVE_IMPEX
/*!50000 ALTER TABLE users ADD COLUMN x INT */;
```

**Fix**: Block-comment'leri de strip et + keyword check'i `\b(DELETE|DROP|...)\b` regex ile sözcük-sınırına bağla; conditional comment marker `/*!` reddedilmeli.

---

#### [P1] SEC-102: SUPER_ADMIN ImpEx tenant header'sız → platform DB'ye sessiz fallback (SEC-002 chain)

**Konum**: `ImpExServiceImpl.java:79-82` — bkz. SEC-002 (P0 detaylı). Ayrıca platform-DB için tablo deny-list zorunlu.

---

#### [P1] SEC-103: ImpEx kalıcı audit trail yok — forensic imkansız

**Kategori**: OWASP A09 — Security Logging & Monitoring Failures
**Konum**: `ImpExServiceImpl.java:55-75` (sadece `log.info`)
**Özet**: ImpEx çalıştırıldığında SQL içeriği uygulama log'larında bile sınırlı (yalnız `length`, statement preview 80 char). DB'de `impex_audit` tablosu yok. Compromised SUPER_ADMIN sessizce platform DB'yi değiştirebilir; sonradan forensic / rollback imkansız.
**Fix**:

```sql
CREATE TABLE impex_audit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  executed_at TIMESTAMP, user_id BIGINT, tenant_db VARCHAR(64),
  full_sql LONGTEXT, statement_count INT, success_count INT,
  failed_count INT, correlation_id VARCHAR(64), client_ip VARCHAR(45)
);
```

Servisin başında insert; her statement için rows-affected ve hata varsa exception class'ı.

---

#### [P1] SEC-104: `@RateLimiter` JVM-lokal — multi-replica deployment'ta gerçek limit yok

**Kategori**: Abuse / DoS
**Konum**: Resilience4j `@RateLimiter` (`ImpExController`, `EntryFieldController`); `application.yml:159-168`
**Özet**: Resilience4j default in-memory bucket. Stage/prod multi-replica olduğunda her replica'nın kendi 5 req/min limiti var → toplam N×5 req/min. ImpEx için bu, abuse window'unu büyütüyor.
**Fix**: Bucket4j Redis backend veya Spring Cloud Gateway global rate-limiter; yahut Traefik per-IP middleware (zaten önerildi). En azından ImpEx için cluster-aware şart.

---

#### [P1] SEC-105: `/api/config/admin/**` PATCH'lerde rate-limit yok → compromised admin platform-wide reCAPTCHA'yı saniyeler içinde kapatabilir

**Kategori**: OWASP A05 / Abuse
**Konum**: `backend/src/main/java/com/backend/presentation/controller/ConfigAdminRecaptchaController.java:34-69`
**Özet**: PATCH `security.recaptcha.enabled=false` → tüm reCAPTCHA-protected endpoint'ler korunmasız. Tek istek yeterli; rate-limit yok, step-up auth yok.
**Fix**:

```java
@RateLimiter(name = "configAdmin")  // 5/min
@PreAuthorize("hasRole('CONFIG_TENANT_ADMIN')")
@PostMapping(...)
public ... patch(...) { ... }
```

Ek: `security.recaptcha.enabled` toggle'ı için **step-up TOTP zorunluluğu** + 24-saatlik audit notification (mail + SUPER_ADMIN inbox).

---

#### [P1] SEC-106: `ConfigPrincipalResolver` JWT/header tenant cross-check eksik (config panel için SEC-003 paralel)

**Konum**: `ConfigPrincipalResolver.java`
**Özet**: SEC-003 paterni config panel için de geçerli; CONFIG_TENANT_ADMIN'in JWT tenant'ı header tenant'ı ile karşılaştırılmıyor.
**Fix**: SEC-003 global filter düzeltmesi config endpoint'leri kapsayacak şekilde uygulanmalı.

---

#### [P1] SEC-107: `/api/platform/public/demo-requests` reCAPTCHA disabled iken rate-limit yok → mail-bomb relay

**Kategori**: Abuse / DoS
**Konum**: `PlatformPublicDemoRequestController.java`, `PlatformDemoRequestServiceImpl.java`
**Özet**: reCAPTCHA enabled iken token doğrulanıyor; SEC-105 ile reCAPTCHA disable edilirse rate-limit yok, sınırsız demo-request submit edilebilir. Her submit SUPER_ADMIN inbox'ına mail tetikliyor (transactional after-commit).
**Fix**: Endpoint'e per-IP rate limit (Bucket4j 10/dk + 100/saat). Ayrıca reCAPTCHA disable durumunda gönderilen mail'leri **batch + dedupe** (aynı IP/email son 5 dk).

---

#### [P1] SEC-108: Newsletter subscribe email enumeration + sınırsız confirmation mail (re-send abuse)

**Kategori**: Information Disclosure / Abuse
**Konum**: `PlatformMailMarketingService.java:300-405` (Phase-2)
**Özet**: Aynı email için tekrarlanan subscribe, her seferinde yeni `confirmToken` oluşturup mail tetikliyor; eski token geçersiz olmuyor (kolon override). Ayrıca response body "already subscribed" vs "new subscription" ayrımı email enumeration imkanı verebilir.
**Fix**:

- Idempotent: aynı email son 5 dakikada PENDING_CONFIRMATION ise yeni mail göndermeden başarı dön (SEC-119 ile birleşik).
- Response mesajını **constant-time eşleştir**: "Eğer email kayıtlıysa, bir doğrulama mail'i gönderildi."

---

#### [P1] SEC-109: `UserServiceImpl.updateUser` client'tan gelen `role` field'ını kontrolsüz uyguluyor → self-promotion

**Kategori**: OWASP A01 — Broken Access Control / Mass Assignment
**Konum**: `backend/src/main/java/com/backend/application/service/UserServiceImpl.java` (Phase-2)
**Özet**: `UpdateUserRequest.role` request DTO'da var; servis bu alanı entity'ye set ediyor. TENANT_ADMIN endpoint'i güvenli ama VIEWER/regular user kendi profili güncelleme akışında (`/api/users/me`) role gönderebilirse self-promotion.
**Fix**: Servis katmanında role değişikliklerini ayrı endpoint'e (POST `/users/{id}/role`) çıkar; profile-update endpoint'i `UpdateUserProfileRequest` DTO'su kullansın (rol içermeyen).

---

#### [P1] SEC-110: ImpEx hata mesajları ham `e.getMessage()`'ı client'a döküyor → schema enumeration

**Kategori**: OWASP A05
**Konum**: `ImpExServiceImpl.java:147-150`
**Özet**: Statement fail olunca `StatementResult.failure(i, preview, e.getMessage())` döner; `e.getMessage()` JDBC'den gelen "Unknown column 'foo' in table 'bar'" gibi schema bilgisi içerir. ImpEx hata response'ları client'ta görüntüleniyor.
**Fix**:

```java
String safe = sqlExceptionTranslator.translate(e);  // "syntax error" / "constraint violation"
results.add(StatementResult.failure(i, preview, safe));
```

Ham mesajı yalnız server log'a yaz (zaten yazılıyor).

---

#### [P1] SEC-111: MediaController magic-byte server-side check yok + Unicode/double-extension bypass

**Kategori**: OWASP A04 — Insecure Design / Malicious File Upload
**Konum**: `MediaController.java:669-693` (Phase-2)
**Özet**: Allowed MIME list client Content-Type header'ından okunuyor. Magic-byte (Apache Tika) doğrulaması yok. `.exe/.bat/...` extension blok'u var ama `evil.png.svg`, `evil.html.jpg`, `evil.svg` (XSS payload), `evil.pdf` (JavaScript) bypass açık.
**Fix**: Apache Tika ile content-type detect:

```java
TikaConfig tika = TikaConfig.getDefaultConfig();
MediaType detected = tika.getDetector().detect(
    TikaInputStream.get(file.getBytes()), new Metadata());
if (!ALLOWED_MIMES.contains(detected.toString())) throw new InvalidFileException();
```

SVG için ek olarak `<script>`, `<foreignObject>`, `on*=` regex reddi (jsoup ile sanitize).

---

### 3.3 — P2 Bulgular (defense-in-depth eksiği / hardening)

#### [P2] SEC-011: CORS dev profile wildcard `http://*.localhost:4200`

**Konum**: `application.yml` cors block, `SecurityConfig.java:76-117`
**Özet**: `allowedOriginPatterns` dev'de `http://*.localhost:4200` wildcard. Geliştirici makinasında zararsız ama config'in stage/prod'a sızması durumunda subdomain takeover senaryolarında köprü olur.
**Fix**: `application.yml` (root) dev'i belirgin profile altına taşı:

```diff
- # default profile
- app.cors.allowed-origin-patterns: http://*.localhost:4200
+ # only in application-dev.yml
+ app.cors.allowed-origin-patterns: http://*.localhost:4200
```

Stage/prod profile'larında patterns boş.

---

#### [P2] SEC-012: Hesap kilitleme + OTP rate-limit JVM-lokal — multi-replica'da etkisiz

**Konum**: `AuthenticationServiceImpl.java:60-68` (`ConcurrentHashMap` rate limiter), `User.failedLoginAttempts` (DB ama lock state çekme stratejisi kontrol edilmeli)
**Özet**: OTP rate-limit ConcurrentHashMap ile her instance bağımsız. Account lockout DB'de tutuluyor ama OTP rate-limit cluster aware değil. SEC-104 paralel.
**Fix**: Redis tabanlı `RateLimiterRegistry` veya Bucket4j Redis backend.

---

#### [P2] SEC-013: Tenant DB name validation tek savunma — path/identifier boundary

**Konum**: `AsyncProvisioningExecutor.java:140-180` (Phase-1)
**Özet**: DB name regex `^ac_[a-z0-9_]+_\d+$` + backtick quote. Strong, ancak provisioning user'ı MySQL'de yalnız tenant DB scope'unda yetkili olmamalı (least privilege).
**Fix**: Provisioning service'in kullandığı MySQL user `CREATE DATABASE` yetkisine sahip olmalı ama global GRANT olmamalı; ayrıca `ac_*` pattern'iyle kısıtlanmış granular user.

---

#### [P2] SEC-014 / SEC-117: HTTP security header'lar tamamen eksik

**Kategori**: OWASP A05
**Konum**: `SecurityConfig.java:38-72` — `http.headers(...)` chain'i çağrılmamış
**Özet**: HSTS, X-Frame-Options, CSP, X-Content-Type-Options, Referrer-Policy hiç set edilmemiş. Admin paneli (storefront/) iframe'e gömülebilir (clickjacking), MIME sniffing açık.
**Fix (diff)**:

```diff
 http
   .csrf(csrf -> csrf.disable())
   .cors(cors -> cors.configurationSource(corsConfigurationSource()))
+  .headers(h -> h
+      .httpStrictTransportSecurity(hsts -> hsts
+          .includeSubDomains(true)
+          .maxAgeInSeconds(31536000))
+      .frameOptions(fo -> fo.deny())
+      .contentTypeOptions(cto -> {})
+      .referrerPolicy(rp -> rp.policy(
+          ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
+      .contentSecurityPolicy(csp -> csp.policyDirectives(
+          "default-src 'self'; frame-ancestors 'none'; object-src 'none'")))
   .sessionManagement(...)
```

---

#### [P2] SEC-113: `getFileContent` `filePath` DB-trust → path traversal (ImpEx + media chain)

**Konum**: `MediaServiceImpl.getFileContent` (Phase-2)
**Özet**: Media kaydındaki `file_path` kolonunu trust'la dosya sistem'inden okuyor. SEC-100/SEC-002 chain'iyle compromised admin `UPDATE media SET file_path='/etc/passwd' WHERE id=…` yapabilir; sonra public download endpoint'i ile local file read.
**Fix**: Yalnız storage root altına escape kontrolü:

```java
Path root = Paths.get(storageRoot).toAbsolutePath().normalize();
Path target = root.resolve(media.getFilePath()).toAbsolutePath().normalize();
if (!target.startsWith(root)) throw new SecurityException("Path escape");
```

---

#### [P2] SEC-114: `CF-Connecting-IP` / `X-Real-IP` blind trust → log/IP spoofing

**Konum**: `JwtAuthenticationFilter` ya da `IpResolver` helper
**Özet**: Cloudflare/Traefik header'ı doğrudan client IP olarak kullanılıyor olabilir. Reverse-proxy'siz erişim olursa attacker bu header'ı set edebilir.
**Fix**: Yalnız trusted proxy listesinden gelen request'lerde header'a güven; doğrudan internet erişimi varsa header'ı sil ve `request.getRemoteAddr()` kullan.

---

#### [P2] SEC-115: `GlobalExceptionHandler.resolveExceptionMessage` dinamik exception mesajlarını client'a sızdırıyor

**Konum**: `backend/src/main/java/com/backend/shared/common/GlobalExceptionHandler.java`
**Özet**: 500-char truncation var ama `truncate(ex.getMessage(), 500)` JDBC/Hibernate hatalarında schema/SQL bilgisi içeriyor olabilir. SEC-110 paralel.
**Fix**: Generic message allow-list — known exception type'lar dışında "internal_error" + correlationId döndür; ham `e.getMessage()` yalnız audit log'a yaz.

---

#### [P2] SEC-116: reCAPTCHA secret rotation tarihi audit'te yok

**Konum**: `ConfigChangeAudit` entity (Phase-2)
**Özet**: PATCH ile reCAPTCHA secret değiştirildiğinde before/after JSON masklanıyor (4+4 char) ama hangi key version aktif/passive bilgisi yok. Forensic'te eski secret hangi tarihten itibaren geçersiz tespiti yapılamıyor.
**Fix**: `key_version` kolonu + secret değişiminde versiyonu artır.

---

#### [P2] SEC-018: Provisioning init admin password entropy doğrulaması

**Konum**: `GenerateTenantAdminUserUseCase` (deep-dive yapılmadı; isim üzerinden flag)
**Özet**: Yeni tenant ilk admin password'u nasıl üretiliyor? `SecureRandom` mı, `Math.random` mı? Mail'le mi gönderiliyor, login üzerinde force-change mi?
**Fix (öneri)**: 24 char `SecureRandom` + Base32 encoded; ilk login'de mandatory password change; mail body içinde tek kullanımlık link + UUID token (1 saat TTL).

---

#### [P2] SEC-019: Bulk-delete tenant ownership cross-check (SEC-003 paralel)

**Konum**: `SiteServiceImpl.bulkDelete` (Phase-1), `MediaController.bulk-delete`
**Özet**: SEC-003 fix'i ile büyük ölçüde kapanır (TenantContext doğru DB'ye route ediyor); yine de servis katmanında **explicit tenant assertion** defense-in-depth.
**Fix**:

```java
Long tenantId = Long.valueOf(tenantContext.getTenantId());
List<Site> sites = siteRepository.findAllById(ids);
sites.forEach(s -> Assert.isTrue(s.getTenantScopeOk(tenantId), "cross-tenant"));
siteRepository.deleteAll(sites);
```

---

#### [P2] SEC-020: VIEWER rolü tüm kullanıcı email'lerini listeleyebiliyor → enumeration payload

**Konum**: `UserController.searchUsers` (Phase-2)
**Özet**: VIEWER, `/api/users` ile tüm tenant kullanıcılarının email + ad-soyadlarını görüyor. Phishing payload'u için altın değerinde liste.
**Fix**: VIEWER endpoint'inde response'tan `email` field'ını çıkar veya domain-mask (`a***@domain.com`).

---

#### [P2] SEC-021: Swagger / OpenAPI stage'de açık olabilir

**Konum**: `SecurityConfig.java:62-63`, `application-stage.yml`
**Özet**: SecurityConfig `/swagger-ui/**` permitAll. Stage'de `springdoc.api-docs.enabled` flag'i kontrol edilmeli; aktifse public attacker tüm endpoint'leri keşfedebilir.
**Fix**: Stage'de `springdoc.api-docs.enabled=false`; veya `/swagger-ui/**` SUPER_ADMIN gated.

---

### 3.4 — P3 Bulgular (hardening / observability)

| ID      | Konum                                    | Özet                                                                                            | Önerilen Aksiyon                                                           |
| ------- | ---------------------------------------- | ----------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- |
| SEC-022 | `JwtTokenProvider.java:22`               | `Keys.hmacShaKeyFor(...getBytes())` UTF-8 default; secret < 64 byte ise zayıf algoritma seçilir | Constructor'da `>=64 byte` assert (SEC-004 fix'iyle birlikte)              |
| SEC-023 | `JwtAuthenticationFilter`                | JWT signature failure'larda `log.error` exception leak'i; INFO seviyesi yeter                   | Log seviyesini DEBUG'a çek                                                 |
| SEC-024 | `application.yml` Hikari pool            | Tenant pool max 5 / min 1; cluster spike'ında tükenir                                           | `app.tenant.pool.max-size` env-tunable yap                                 |
| SEC-025 | `AsyncConfig.TenantContextTaskDecorator` | OK ama Kafka/SQS eklenirse consumer-side context restore unutulmamalı                           | Kod yorumu + ADR doc'a not                                                 |
| SEC-118 | `CmsDeliveryController` batch            | Per-IP rate limit + N+1 query riski                                                             | `WHERE uid IN (:uids)` + Caffeine TTL 60s + per-IP RL                      |
| SEC-119 | `PlatformMailMarketingService.subscribe` | Idempotent değil — aynı email sınırsız mail                                                     | son 5dk PENDING_CONFIRMATION → no-op (SEC-108 ile çakışıyor)               |
| SEC-120 | `RecaptchaServiceImpl.java:110-114`      | Threshold 0.5 hardcoded                                                                         | `ConfigPropertyService.getDecimal(... "security.recaptcha.threshold" ...)` |
| SEC-121 | `MediaController` bulk-delete            | `BulkDeleteRequest.ids` size validation yok                                                     | `@Size(max=100)` + response truncation                                     |
| SEC-026 | `application.yml` rate-limit configs     | Resilience4j entry/impex limit'leri 5/min sabit                                                 | env-tunable + per-tenant override                                          |

---

## 4. Config / Secrets Envanteri

| Variable                           | Dev default                       | Stage                       | Prod            | Risk Notu                      |
| ---------------------------------- | --------------------------------- | --------------------------- | --------------- | ------------------------------ |
| `JWT_SECRET`                       | **HARDCODED literal** (app.yml:9) | `${JWT_SECRET}`             | `${JWT_SECRET}` | SEC-004 — prod fail-fast şart  |
| `JWT_EXPIRATION`                   | 86400000 (24h)                    | inherited                   | inherited       | OK (SEC-008 refresh için ayrı) |
| `JWT_REFRESH_EXPIRATION`           | 604800000 (7d)                    | inherited                   | inherited       | SEC-008 — kısalt + rotation    |
| `OTP_BYPASS_CODE`                  | empty                             | **`123456` default**        | empty           | **SEC-001 P0 — ACİL**          |
| `RECAPTCHA_MASTER_KEY`             | placeholder                       | env                         | env             | OK — startup validation var    |
| `DB_USERNAME` / `DB_PASSWORD`      | `root`/`1234`                     | env                         | env             | OK — dev-only default          |
| `SMTP_PASSWORD`                    | empty                             | env                         | env             | OK                             |
| `SPACES_ACCESS_KEY` / `SECRET_KEY` | empty                             | env                         | env             | OK                             |
| `APP_FRONTEND_BASE_URL`            | localhost:4200                    | `https://s1-%s.craftive.io` | env             | OK                             |

---

## 5. Remediation Roadmap

### Sprint #1 (bu hafta — 48-saat hedefi)

- **SEC-001** stage `OTP_BYPASS_CODE` default'unu kaldır
- **SEC-002 + SEC-100 + SEC-102** ImpEx SUPER_ADMIN bypass'ı kapat + sensitive tablo deny-list (tek workstream — birlikte çıkmalı)
- **SEC-003 + SEC-006 + SEC-106** TenantFilter global JWT-header tenant cross-check (tek değişiklik üç bulguyu kapatır)
- **SEC-004** JWT secret startup validation
- **SEC-005** `/actuator/**` SUPER_ADMIN gated

### Sprint #2 (bu sprint)

- SEC-007 token-type validation
- SEC-009 + SEC-113 media authz + path-traversal guard
- SEC-103 ImpEx audit table + log-everything migration
- SEC-105 config admin rate-limit + step-up
- SEC-107 demo-request per-IP rate-limit
- SEC-108 + SEC-119 newsletter idempotent
- SEC-109 user mass-assignment fix
- SEC-110 + SEC-115 exception message sanitization
- SEC-111 magic-byte file validation
- SEC-014/SEC-117 HTTP security headers

### Sprint #3 (sonraki sprint)

- SEC-008 refresh-token rotation + revocation table
- SEC-010 ECB re-encrypt migration + legacy path silme
- SEC-104 + SEC-012 cluster-aware rate-limiter (Redis)
- SEC-114 trusted-proxy header policy
- SEC-018 provisioning init admin password hardening
- SEC-019 bulk-delete tenant assertion
- SEC-020 VIEWER user listing email mask

### Hardening Backlog

- SEC-011 CORS profile separation
- SEC-013 Provisioning MySQL grant minimization
- SEC-021 Swagger stage gating
- SEC-022..SEC-026 (tablodaki P3'ler)
- SEC-116 reCAPTCHA key_version
- SEC-118 CMS delivery N+1 + per-IP RL
- SEC-120 reCAPTCHA threshold tenant-override
- SEC-121 media bulk-delete size limit

---

## 6. Multi-Tenant İzolasyon Pozitifleri (devam ettirilmeli)

| Mekanizma                                   | Konum                                    | Not                                     |
| ------------------------------------------- | ---------------------------------------- | --------------------------------------- |
| ThreadLocal cleanup                         | `TenantFilter.java:149-152`              | finally block ile clear()               |
| Async context propagation                   | `AsyncConfig.TenantContextTaskDecorator` | `@Async` + `CompletableFuture` için OK  |
| `CurrentTenantIdentifierResolver` fallback  | `platform_management`                    | safe default                            |
| `MultiTenantConnectionProvider` LRU + close | Guava cache, removal listener `close()`  | leak yok                                |
| `TenantDbExecutor.withTenant`               | `try-finally` ile prev context restore   | nested-safe                             |
| DB name validation                          | `AsyncProvisioningExecutor.java:140-180` | regex + backtick quote                  |
| BCrypt cost 12                              | `SecurityConfig.java:35`                 | sektör eşiğinin üstünde                 |
| AES/GCM secret encryption                   | `EncryptionService`                      | random IV / 128-bit auth tag            |
| Audit log for config changes                | `ConfigChangeAudit`                      | actor + before/after + correlationId    |
| Honeypot + timing window                    | newsletter                               | bot-resistant                           |
| 500-char exception truncation               | `GlobalExceptionHandler:396`             | (SEC-115 ile birlikte sıkılaştırılmalı) |
| MDC tenant + correlationId                  | her log                                  | trace edilebilir                        |

---

## 7. Kapsam Dışı / İleri İş

- **Frontend (Angular admin + Next.js storefront)**: XSS sink'leri, Angular template'leri, JWT storage (localStorage vs httpOnly cookie), Next.js SSR'da SSRF, CSP report endpoint'i. Ayrı audit önerilir.
- **Dependency / CVE taraması**: `mvn dependency-check:check` (OWASP DC) + `npm audit` Angular/Next için. CI'a dahil et.
- **Deployment**: Cloudflare WAF rules, Traefik per-IP middleware, Docker image scan (Trivy), TLS pinning, MySQL TLS (`useSSL=true&requireSSL=true`).
- **Penetrasyon testi**: SEC-001..SEC-004 fix'lerinden sonra external pentest önerilir.
- **Threat model dokümanı**: ImpEx feature için STRIDE; production'da hâlâ etkin tutulacak mı kararı verilmeli.
- **Audit log retention politikası**: `ConfigChangeAudit`, yeni `impex_audit` tablolarının saklama süresi (KVKK/GDPR).

---

## 8. Özet Tablo (tüm bulgular)

| ID      | Severity | Kategori             | Tek-cümle başlık                                                         |
| ------- | -------- | -------------------- | ------------------------------------------------------------------------ |
| SEC-001 | P0       | Auth                 | OTP_BYPASS_CODE stage'de `123456` default açık                           |
| SEC-002 | P0       | Multi-tenant + Authz | ImpEx SUPER_ADMIN tenant bypass + platform DB fallback → role escalation |
| SEC-003 | P0       | Multi-tenant         | JWT-header tenant cross-check yok → cross-tenant veri erişimi            |
| SEC-004 | P0       | Crypto/Config        | JWT secret hardcoded + prod startup validation yok                       |
| SEC-100 | P0       | Authz                | ImpEx INSERT/UPDATE allow-list privilege escalation'a açık               |
| SEC-005 | P1       | Config               | /actuator/\*\* permitAll → bilgi sızıntısı                               |
| SEC-006 | P1       | Multi-tenant         | Hostname Origin/Referer fallback spoofing                                |
| SEC-007 | P1       | Auth                 | validateToken token tipi (access/refresh) doğrulamıyor                   |
| SEC-008 | P1       | Auth                 | Refresh token uzun TTL + rotation/revocation yok                         |
| SEC-009 | P1       | Authz                | /media/files/\*\* permitAll → private dosya leak                         |
| SEC-010 | P1       | Crypto               | EncryptionService legacy AES/ECB fallback hâlâ açık                      |
| SEC-101 | P1       | Injection            | ImpEx parser block-comment + MySQL conditional comment bypass            |
| SEC-102 | P1       | Multi-tenant         | SUPER_ADMIN ImpEx tenant header'sız → platform DB silent fallback        |
| SEC-103 | P1       | Audit                | ImpEx kalıcı audit trail yok                                             |
| SEC-104 | P1       | Abuse/DoS            | @RateLimiter JVM-lokal — cluster'da etkisiz                              |
| SEC-105 | P1       | Config               | /api/config/admin/\*\* PATCH rate-limit yok → reCAPTCHA fast-disable     |
| SEC-106 | P1       | Multi-tenant         | ConfigPrincipalResolver JWT/header tenant cross-check eksik              |
| SEC-107 | P1       | Abuse/DoS            | demo-requests reCAPTCHA disabled iken rate-limit yok → mail-bomb         |
| SEC-108 | P1       | Abuse                | Newsletter email enumeration + sınırsız confirmation mail                |
| SEC-109 | P1       | Mass-assign          | UserServiceImpl.updateUser role kontrolsüz → self-promotion              |
| SEC-110 | P1       | Info Disc.           | ImpEx hata mesajları ham JDBC exception → schema enumeration             |
| SEC-111 | P1       | File Upload          | MediaController magic-byte yok + extension bypass                        |
| SEC-011 | P2       | Config               | CORS dev wildcard origin pattern                                         |
| SEC-012 | P2       | Auth                 | OTP/account lockout JVM-lokal (cluster aware değil)                      |
| SEC-013 | P2       | Provisioning         | DB name validation tek savunma; MySQL user least-privilege               |
| SEC-014 | P2       | Headers              | HTTP security headers (HSTS/CSP/X-Frame/CTO) eksik                       |
| SEC-018 | P2       | Provisioning         | İlk admin password entropy + force-change kontrol gerekli                |
| SEC-019 | P2       | Multi-tenant         | Bulk-delete tenant ownership servis-katmanı assertion                    |
| SEC-020 | P2       | Info Disc.           | VIEWER tüm kullanıcı email'lerini görüyor                                |
| SEC-021 | P2       | Config               | Swagger stage'de açık olabilir                                           |
| SEC-113 | P2       | Path Trav.           | getFileContent file_path DB-trust + ImpEx chain ile escape               |
| SEC-114 | P2       | Spoofing             | CF-Connecting-IP / X-Real-IP blind trust                                 |
| SEC-115 | P2       | Info Disc.           | GlobalExceptionHandler dinamik mesajları client'a sızdırıyor             |
| SEC-116 | P2       | Audit                | reCAPTCHA secret rotation key_version yok                                |
| SEC-022 | P3       | Crypto               | JWT secret byte uzunluğu doğrulanmıyor                                   |
| SEC-023 | P3       | Logging              | JWT signature fail log seviyesi                                          |
| SEC-024 | P3       | Capacity             | Tenant Hikari pool 5 — env-tunable şart                                  |
| SEC-025 | P3       | Async                | Kafka/SQS eklenirse consumer context restore notu                        |
| SEC-026 | P3       | Config               | Resilience4j limit'leri tunable + per-tenant                             |
| SEC-118 | P3       | Abuse/DoS            | CMS Delivery batch endpoint per-IP RL + N+1                              |
| SEC-119 | P3       | Abuse                | Newsletter subscribe idempotent değil                                    |
| SEC-120 | P3       | Config               | reCAPTCHA threshold 0.5 hardcoded                                        |
| SEC-121 | P3       | Validation           | Media bulk-delete size validation yok                                    |

---

**Audit auditörü**: Claude Code (Opus 4.7) — `/code-reviewer` agent + manuel verification
**Onay süreci**: Bulgular issue tracker'a tek tek aktarılmalı; SEC-001..SEC-005 P0/P1 hızlı patch öncelikli.
**Yeniden değerlendirme tarihi**: 2026-07-25 (P0/P1 fix'leri sonrasında)
