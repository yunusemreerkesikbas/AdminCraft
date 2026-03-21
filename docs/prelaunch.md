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
- [ ] Legacy `admincraft` domain referanslari dokumantasyonda ve aktif configlerde temizlendi

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
