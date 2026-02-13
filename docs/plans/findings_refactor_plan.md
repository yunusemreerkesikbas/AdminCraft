# Findings Refactor Plan (Güncellenmiş)

Migration Immutability karar adımı atlandı. V13, V14, V34 reversions doğrudan uygulanacak.

---

## 1. migration-guardrails.yml

- **Dosya:** `.github/workflows/migration-guardrails.yml`
- **Değişiklik:** `push.branches: main` → `master`

---

## 2. PageDeliveryServiceImpl

- **Dosya:** `backend/src/main/java/com/backend/application/service/PageDeliveryServiceImpl.java`
- **Değişiklik:** Satır 272-273 `typeCode` ve `robotTag` girintilerini metot gövdesiyle uyumlu hale getir (12 space)

---

## 3. R\_\_seed_components.sql

- **Dosya:** `backend/src/main/resources/db/tenant/component_library/R__seed_components.sql`
- **Değişiklik:** `s1000001`, `s2000xxx` gibi UUID'leri `a1000001`, `a2000xxx` ile değiştir (s → a)

---

## 4. JpaComponentMediaLinkRepository

- **Dosya:** `backend/src/main/java/com/backend/infrastructure/persistence/tenant/repository/JpaComponentMediaLinkRepository.java`
- **Değişiklik:** `existsByComponentIdAndMediaIdAndLinkTypeAndEntryId` için `@Query` ile NULL-safe kontrol ekle

---

## 5. V13\_\_add_component_responsive.sql

- **Dosya:** `backend/src/main/resources/db/tenant/component_library/V13__add_component_responsive.sql`
- **Değişiklik:** Prosedür/idempotent mantığı kaldır; sade DDL ile değiştir:
  - `ALTER TABLE components ADD COLUMN responsive_id BIGINT NULL AFTER style_classes`
  - `ALTER TABLE components ADD CONSTRAINT fk_component_responsive FOREIGN KEY (responsive_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL`
  - `CREATE INDEX idx_component_responsive ON components(responsive_id)`

---

## 6. V14\_\_add_entry_responsive.sql

- **Dosya:** `backend/src/main/resources/db/tenant/component_library/V14__add_entry_responsive.sql`
- **Değişiklik:** Prosedür/idempotent mantığı kaldır; sade DDL ile değiştir:
  - `ALTER TABLE component_entries ADD COLUMN responsive_id BIGINT NULL AFTER style_classes`
  - `ALTER TABLE component_entries ADD CONSTRAINT fk_entry_responsive FOREIGN KEY (responsive_id) REFERENCES responsive_media_set(id) ON DELETE SET NULL`
  - `CREATE INDEX idx_entry_responsive ON component_entries(responsive_id)`

---

## 7. V34\_\_add_recaptcha_to_sites.sql

- **Dosya:** `backend/src/main/resources/db/tenant/core/V34__add_recaptcha_to_sites.sql`
- **Değişiklik:** Prosedür/idempotent mantığı kaldır; sade DDL ile değiştir (recaptcha kolonları + idx_sites_recaptcha_enabled)

---

## 8. V28\_\_ensure_page_type.sql

- **Dosya:** `backend/src/main/resources/db/tenant/pagebuilder/V28__ensure_page_type.sql`
- **Değişiklik:** AddColumnIfNotExists içinde `tableName` ve `colName` için backtick + escape ekle

---

## 9. product/V34\_\_repair_responsive_media_links.sql (uk_product_media)

- **Dosya:** `backend/src/main/resources/db/tenant/product/V34__repair_responsive_media_links.sql`
- **Açık Karar:** NULL ile unique constraint davranışı için A veya B seçilmeli (plan'da mevcut)

---

## 10. migration-governance.md

- **Dosya:** `docs/global/migration-governance.md`
- **Değişiklik:** `origin/main` → `origin/master`

---

## 12. check-versioned-immutability.sh

- **Dosya:** `scripts/migrations/check-versioned-immutability.sh`
- **Değişiklik:** `BASE_REF` fallback: `main` → `master`

---

## 13. lint-migrations.sh

- **Dosya:** `scripts/migrations/lint-migrations.sh`
- **Değişiklik:** Satır 22'deki `rg` çağrısına `-P` (PCRE) flag ekle

---

## Uygulama Sırası

1. 1–4: Workflow, Java (PageDeliveryService, JPA), seed
2. 5–7: V13, V14, V34 reversions
3. 8: V28 identifier güvenliği
4. 9: uk_product_media (karar sonrası)
5. 10–13: Docs + scripts
