# Prelaunch Checklist

Stage ve prod ortamlara gecmeden once tamamlanmasi gereken secret, configuration, security ve operasyon adimlari.

Bu dokumanin amaci:

- Stage ve prod icin gerekli key/env alanlarini tek yerde toplamak
- Repo icinde yapilan degisikliklerden sonra manuel tamamlanmasi gereken isleri netlestirmek
- Launch oncesi "hazir / eksik" kontrolunu kolaylastirmak

## Scope

Bu checklist su parcalari kapsar:

- `backend/` Spring Boot API
- `storefront/` Angular admin uygulamasi
- `storefront-nextjs/` demo/reference Next.js storefront
- Traefik + Docker Compose + GitHub Actions deploy akislari
- Loki/Grafana gozlemlenebilirlik kurulumu

## Architecture Reminder

- `storefront/` yalnizca admin/control-panel frontend'dir.
- `storefront-nextjs/` bu repository tarafindan deploy edilen demo/reference storefront'tur.
- Tenant storefront'lar `storefront-nextjs/` fork'lanarak ayrik repo/image olarak deploy edilir.
- Platform deploy'u demo/reference storefront'u yayinlar; tenant deploy'lari ayrik lifecycle ile yonetilir.

## Required Secrets

Asagidaki degerler stage ve prod icin ayrik uretilmeli, guvenli secret store'da tutulmali ve repoya yazilmamalidir.

| Key | Stage | Prod | Notes |
| --- | --- | --- | --- |
| `JWT_SECRET` | Required | Required | Minimum 64+ karakter, farkli degerler |
| `DB_USERNAME` | Required | Required | `root` kullanmayin; least-privilege user |
| `DB_PASSWORD` | Required | Required | Ayrik sifre |
| `CF_API_TOKEN` | Required | Required | Traefik DNS challenge icin |
| `ACME_EMAIL` | Required | Required | Let's Encrypt bildirim adresi |
| `RECAPTCHA_MASTER_KEY` | Required | Required | Platform genel anahtar |
| `SMTP_HOST` | Required | Required | Mail provider host |
| `SMTP_PORT` | Required | Required | Genelde `587` |
| `SMTP_USERNAME` | Required | Required | SMTP kullanicisi |
| `SMTP_PASSWORD` | Required | Required | SMTP sifresi |
| `GRAFANA_CLOUD_LOKI_URL` | Required | Required | Loki ingest endpoint |
| `GRAFANA_CLOUD_LOKI_USER` | Required | Required | Loki user/tenant |
| `GRAFANA_CLOUD_LOKI_TOKEN` | Required | Required | Loki token |

Important distinction:

- `RECAPTCHA_MASTER_KEY` deploy-time encryption key'dir; Google reCAPTCHA key'i degildir.
- `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` transport credential'laridir; `/config` panelinden uretilmez veya yonetilmez.
- `/config` paneli sadece runtime override yapar: email provider/from alanlari ve platform/tenant reCAPTCHA runtime key degerleri.

## Runtime-Configurable vs Deploy-Time

### `/config` veya platform settings ile runtime override edilebilenler

- `app.email.provider`
- `app.email.from-address`
- `app.email.from-name`
- `app.frontend.base-url`
- `platform.security.recaptcha.enabled`
- `platform.security.recaptcha.site_key`
- `platform.security.recaptcha.secret_key`
- tenant `security.recaptcha.enabled`
- tenant `security.recaptcha.site_key`
- tenant `security.recaptcha.secret_key`

Not:

- Bu degerler uygulama tarafinda "generate" edilmez; dis sistemlerden alinip UI/API uzerinden girilir.
- Platform reCAPTCHA secret key encrypted olarak saklanir.

### Sadece deploy-time secret / environment olarak kalmasi gerekenler

- `JWT_SECRET`
- `DB_USERNAME`
- `DB_PASSWORD`
- `CF_API_TOKEN`
- `SMTP_HOST`
- `SMTP_PORT`
- `SMTP_USERNAME`
- `SMTP_PASSWORD`
- `GRAFANA_CLOUD_LOKI_URL`
- `GRAFANA_CLOUD_LOKI_USER`
- `GRAFANA_CLOUD_LOKI_TOKEN`
- `RECAPTCHA_MASTER_KEY`

## Required Non-Secret Environment Values

| Key | Stage Example | Prod Example | Notes |
| --- | --- | --- | --- |
| `SPRING_PROFILE` | `stage` | `prod` | Deploy hedefi ile uyumlu olmali |
| `DOMAIN` | `craftive.io` | `craftive.io` | Ana platform domain'i |
| `APP_FRONTEND_BASE_URL` | `https://s1-%s.craftive.io` | `https://%s.craftive.io` | Tenant storefront URL pattern |
| `PLATFORM_DOMAIN` | `s1.craftive.io` | `craftive.io` | Platform-level host/default link davranislari icin |
| `EMAIL_FROM_ADDRESS` | `noreply@craftive.io` | `noreply@craftive.io` | Default sender |
| `EMAIL_FROM_NAME` | `Craftive` | `Craftive` | Default sender name |
| `LOG_ENV` | `stage` | `prod` | Loki label |
| `LOG_HOST` | `do-fra1-stage-01` | `do-fra1-prod-01` | Loki label |

## Secret Rotation Actions

Asagidaki key'ler ifsa olmus veya riskli kabul edilmelidir. Launch oncesi rotate edilmelidir.

- Tavily/MCP tarafinda kullanilan API key
- `JWT_SECRET`
- `CF_API_TOKEN`
- `RECAPTCHA_MASTER_KEY`
- `SMTP_PASSWORD`
- `GRAFANA_CLOUD_LOKI_*`

Rotation sonrasi:

1. GitHub Actions `stage` ve `production` environment secret'larini guncelle.
2. Droplet veya merkezi secret store icindeki `.env.stage` / `.env.prod` degerlerini guncelle.
3. Eski secret'larin artik calismadigini dogrula.

## Secret Generation Guide

Asagidaki degerler repodan turetilmez; guvenli sistemlerden veya guvenli random üretimle olusturulur.

| Key | Nereden / Nasil Olusturulur | Not |
| --- | --- | --- |
| `JWT_SECRET` | Password manager generator veya `openssl rand -base64 48` | En az 64+ karakter; stage ve prod farkli olmali |
| `DB_USERNAME` | MySQL icinde yeni kullanici olustur | `root` kullanmayin; least-privilege yetki verin |
| `DB_PASSWORD` | Password manager generator veya `openssl rand -base64 32` | Stage ve prod ayri sifre kullanin |
| `CF_API_TOKEN` | Cloudflare dashboard > API Tokens | Sadece gereken zone ve DNS edit izinleri verin |
| `ACME_EMAIL` | Operasyonel e-posta adresi | Let's Encrypt bildirimleri icin aktif mailbox olmali |
| `RECAPTCHA_MASTER_KEY` | Password manager generator veya guvenli random secret | Panelde saklanacak ana platform secret |
| `SMTP_HOST` / `SMTP_PORT` | Mail provider dokumani | Genelde Brevo, Mailgun, SES, Postmark vb. |
| `SMTP_USERNAME` / `SMTP_PASSWORD` | Mail provider dashboard | Ayrik SMTP credentials kullanin |
| `GRAFANA_CLOUD_LOKI_URL` | Grafana Cloud stack detaylari | Logs ingest endpoint |
| `GRAFANA_CLOUD_LOKI_USER` | Grafana Cloud stack detaylari | Tenant/user id |
| `GRAFANA_CLOUD_LOKI_TOKEN` | Grafana Cloud access policy / token | Yalniz logs write yetkisi verin |

## Why Secret Rotation Is Required

Secret rotation gereklidir cunku bir secret repository, commit history, local agent config veya paylasilmis env dosyasina bir kez girdiginde "guvenli" kabul edilemez.

Pratik nedenler:

- Git'ten silinmis olsa bile commit history, clone'lar ve local cache'lerde kalabilir.
- Tracked local config dosyalari ekip ici veya CI ortamlarina tasinmis olabilir.
- Hangi degerin kim tarafindan goruldugu sonradan kesin olarak kanitlanamaz.
- Prelaunch asamasinda rotation yapmak, canliya gecisten sonra incident yonetmekten cok daha dusuk maliyetlidir.

Bu nedenle bir secret icin "muhtemelen gorulmedi" varsayimi yerine "gormus olabilirler" varsayimi ile hareket edilmelidir.

## Confirmed Repository Exposure Status

Su anki git incelemesine gore:

- `.mcp.json` dosyasi **gecmiste git'e commit edilmis** ve daha sonra silinmis.
- `.claude/settings.local.json` dosyasi **halen tracked/local-state** niteliginde.
- `.env.example` yalnizca placeholder iceriyor; gercek credential icermiyor.
- `.env.stage` ve `.env.prod` su anda tracked degil; bu incelemede bu iki dosyanin git history'de yer aldigina dair dogrudan kanit gorulmedi.

Bu nedenle minimum aksiyon:

- `.mcp.json` ile baglantili tum aktif API key'leri rotate et.
- Local agent/config dosyalarinda tutulmus olabilecek diger credential'lari da guvensiz kabul et.
- Stage/prod secret setlerini canli oncesi temiz kaynaklardan yeniden uret.

## Stage/Prod Handoff Checklist

Asagidaki liste deploy sorumlulugu devredilmeden once tamamlanmis olmali.

### 1. Secret Store Hazirligi

- [ ] GitHub `stage` environment secret'lari tanimlandi
- [ ] GitHub `production` environment secret'lari tanimlandi
- [ ] Droplet veya merkezi secret store icinde `.env.stage` hazirlandi
- [ ] Droplet veya merkezi secret store icinde `.env.prod` hazirlandi
- [ ] Stage ve prod secret degerleri birbirinden farkli

### 2. Infra ve DNS

- [ ] `app.craftive.io` routing dogrulandi
- [ ] `api.craftive.io` routing dogrulandi
- [ ] Demo/reference storefront hostlari dogrulandi
- [ ] Stage tenant pattern `s1-<tenant>.craftive.io` olarak tanimli
- [ ] Prod tenant pattern `<tenant>.craftive.io` olarak tanimli
- [ ] Wildcard / tekil DNS kayitlari Cloudflare tarafinda kontrol edildi

### 3. Backend ve Mail

- [ ] Ayrik DB kullanicilari olusturuldu
- [ ] `DB_USERNAME` / `DB_PASSWORD` stage ve prod icin dogrulandi
- [ ] SMTP test maili gonderildi
- [ ] `EMAIL_FROM_ADDRESS` ve `EMAIL_FROM_NAME` onaylandi
- [ ] `RECAPTCHA_MASTER_KEY` runtime tarafinda dogrulandi

### 4. Observability

- [ ] `LOG_ENV=stage` ve `LOG_ENV=prod` degerleri ayarlandi
- [ ] `LOG_HOST` degerleri droplet naming ile uyumlu
- [ ] Alloy/Loki log akisi stage'de test edildi
- [ ] Prod alerting / dashboard erisimi teyit edildi

### 5. Son Dogrulama

- [ ] Stage deploy sonrasi `/api/actuator/health` basarili
- [ ] Angular admin stage domaininde aciliyor
- [ ] Demo/reference storefront stage domaininde aciliyor
- [ ] Prod deploy oncesi rollback plani hazir
- [ ] Prod deploy sonrasi health, login, mail ve log smoke testleri tamamlandi

## Stage Readiness Checklist

- [ ] Stage icin tum secret'lar yeni degerlerle tanimlandi
- [ ] `DB_USERNAME` least-privilege user olarak olusturuldu
- [ ] `docker-compose.stage.yml` + `docker-compose.prod.yml` ile servisler dogru environment dosyasi ile kalkiyor
- [ ] Backend health endpoint basarili: `/api/actuator/health`
- [ ] Angular admin stage domaininde aciliyor
- [ ] Demo/reference `storefront-nextjs` stage domaininde aciliyor
- [ ] Tenant host routing `api.` ve `app.` subdomainleri ile cakismiyor
- [ ] Loki/Alloy log akisi calisiyor
- [ ] SMTP test maili basarili
- [ ] reCAPTCHA ayarlari config panel ve runtime tarafinda dogrulandi

## Production Readiness Checklist

- [ ] Tum prod secret'lari stage'den farkli ve rotate edilmis durumda
- [ ] GitHub production environment approval akisi aktif
- [ ] Prod deploy workflow demo/reference storefront image'ini build/push ediyor
- [ ] Traefik DNS challenge icin `CF_API_TOKEN` calisiyor
- [ ] `app.<domain>`, `api.<domain>`, root domain ve tenant wildcard kayitlari dogru
- [ ] Demo/reference storefront prod'da ayakta
- [ ] Tenant-specific storefront deploy script'i ayrik image ile calisiyor
- [ ] Backup / rollback proseduru dogrulandi
- [ ] Monitoring, alerting ve basic smoke testler tanimli

## Repository Hygiene Checklist

- [ ] Local-only dosyalar repoda tracked degil
- [ ] `.env.local`, debug loglari, tmp dosyalari `.gitignore` tarafindan kapsaniyor
- [ ] Repo icinde gercek credential kalmadi
- [ ] Legacy `craftive` domain referanslari dokumantasyonda ve aktif configlerde temizlendi

## Manual Inputs Needed From Owner

Asagidaki degerler repository icinden turetilemez; bunlarin owner tarafindan saglanmasi gerekir:

- Yeni uretilmis tum stage/prod secret degerleri
- Cloudflare DNS token
- SMTP provider credentials
- Grafana Cloud Loki credentials
- Stage ve prod host naming karari (`LOG_HOST`, domain yapisi, droplet isimleri)
- DB kullanicisi ve sifresi

## Suggested Execution Order

1. Secret rotation
2. GitHub environment secret guncelleme
3. Droplet env dosyalarinin guncellenmesi
4. Stage deploy + smoke test
5. Prod deploy oncesi son gozden gecirme
6. Prod deploy + health checks + rollback readiness

## Related Docs

- [`README.md`](README.md)
- [`global/devops.md`](global/devops.md)
- [`global/environment-configuration.md`](global/environment-configuration.md)
- [`storefront-nextjs/README.md`](storefront-nextjs/README.md)
