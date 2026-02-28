# Implementation Plan: Response’da Null Data Davranışı

## 1. Mevcut Durum Özeti

Backend’de response’larda **null değerli alanların JSON’da hiç gönderilmemesi** iki katmanda uygulanıyor:

### 1.1 Global davranış (tüm API)

**Dosya:** `backend/src/main/java/com/backend/infrastructure/config/JacksonConfig.java`

```java
return builder
    .serializationInclusion(JsonInclude.Include.NON_NULL)
    // ...
```

ObjectMapper **global** olarak `NON_NULL` kullandığı için:

- Tüm DTO’lar serialize edilirken `null` olan alanlar JSON’da **yazılmıyor**.
- Sadece storefront değil, **tüm REST cevapları** (admin API, CMS delivery, platform API) bu kurala tabi.

### 1.2 ApiResponse wrapper

**Dosya:** `backend/src/main/java/com/backend/shared/common/ApiResponse.java`

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String result;
    private String message;
    private T data;   // null ise JSON'da "data" anahtarı yok
    private Integer code;
}
```

- `data == null` olduğunda (hata cevabı, delete success vb.) response’ta **`"data"` anahtarı hiç olmuyor**.
- Örnek: `ApiResponse.error("Page not found")` → JSON: `{ "result": "ERROR", "message": "Page not found" }` (data yok).

### 1.3 Ek olarak NON_NULL kullanan DTO’lar

Global config zaten NON_NULL olduğu için bunlar tekrarlayıcı; davranışı pekiştiriyorlar:

| Dosya | Amaç |
|-------|------|
| `application/dto/delivery/SiteDeliveryResponse.java` | Site config (storefront) |
| `application/dto/delivery/PageDeliveryResponse.java` | Sayfa delivery |
| `application/dto/delivery/ContentSlotDeliveryResponse.java` | Slot + ComponentsWrapper |
| `application/dto/delivery/ContentSlotsWrapper.java` | Content slot listesi |
| `presentation/dto/response/SiteTechnicalResponse.java` | Teknik ayarlar (iç içe record’lar) |
| `presentation/dto/response/ProductFieldDefinitionResponse.java` | Ürün alan tanımı |

Bu DTO’lar storefront’a giden veya admin’e dönen response’ların bir kısmı; **etki alanı** aslında global config ile **tüm response’lar**.

---

## 2. Storefront Tarafındaki Kullanım

**Dosya:** `storefront-nextjs/lib/cms-client.ts`

```typescript
const payload = (await response.json()) as ApiResponse<T>;
// ...
if (payload.result === "ERROR") {
  return null;
}
return payload.data ?? null;  // data yoksa veya null ise null dön
```

- `payload.data` backend’de gönderilmediği için `undefined` oluyor; `?? null` ile `null`’a çevriliyor.
- Sayfa bulunamadığında (`result: "ERROR"`) zaten `null` dönülüyor; `data`’nın olmaması ek bir sorun yaratmıyor.

**Dosya:** `storefront-nextjs/lib/types.ts`

```typescript
export interface ApiResponse<T> {
  result: ApiResult;
  message: string;
  data: T;        // runtime'da null/undefined olabiliyor
  code?: number | null;
}
```

- Tip `data: T` zorunlu gibi görünüyor; gerçekte hata/boş cevaplarda `data` bazen gelmiyor. İsteğe bağlı yapmak için `data?: T` daha doğru olur.

---

## 3. Dokümantasyonla Uyum

**Dosya:** `docs/modules/cms-delivery.md`

Şu an yazılan:

```json
{ "result": "ERROR", "message": "Page not found", "data": null }
```

Mevcut davranışta `data` null olduğu için **gönderilmiyor**; gerçek cevap:

```json
{ "result": "ERROR", "message": "Page not found" }
```

Dokümanı buna göre güncellemek faydalı (aşağıda öneri var).

---

## 4. Bu Geliştirmeye Gerek Var mı?

### 4.1 Mevcut yaklaşımı korumak (önerilen)

**Neden mantıklı:**

- **Payload küçülür:** Özellikle çok optional alanlı DTO’larda `"x": null` tekrarları kalkar.
- **Yaygın REST pratiği:** Birçok public API null alanları omit eder.
- **Storefront zaten uyumlu:** `payload.data ?? null` ve `result === "ERROR"` ile hem eksik `data` hem null doğru işleniyor.
- **Tutarlılık:** Tek bir global kural (JacksonConfig) ile tüm API aynı davranıyor.

**Ne yapılabilir (opsiyonel):**

- TS tipi: `ApiResponse<T>` içinde `data?: T` yapılabilir (runtime ile uyum).
- Doküman: `cms-delivery.md` içinde “data null ise gönderilmez” açıklaması ve örnek JSON güncellenebilir.

### 4.2 Null’ları olduğu gibi döndürmek

**Ne değişir:**

- `JacksonConfig` ve `ApiResponse` (ve diğer DTO’lardaki) `NON_NULL` kaldırılır.
- Her optional alan için `"alan": null` JSON’da görünür; hata cevaplarında `"data": null` da gelir.

**Artıları:**

- Client tarafında “bu alan yok” ile “bu alan null” ayrımı yapılabilir (genelde gerekmez).
- Bazı strict schema/OpenAPI tüketicileri explicit null’ı bekleyebilir.

**Eksileri:**

- Cevap boyutu artar.
- Mevcut storefront davranışı değişmez ama gereksiz veri taşınır.

**Ne zaman tercih edilebilir:**  
Contract’ta “her optional alan mutlaka key ile, değeri null olabilir” denmesi gerekiyorsa veya mevcut tüketiciler explicit null’a bağımlıysa.

---

## 5. Önerilen Karar ve Aksiyonlar

| Soru | Öneri |
|------|--------|
| Null’ları omit etmek gerekli mi? | **Evet, mevcut davranış (sadece dolu datayı göndermek) korunabilir.** Gerekli değilse bile zararlı değil; payload ve okunabilirlik açısından faydalı. |
| Data’yı olduğu gibi mi, sadece dolu mu dönelim? | **Sadece dolu (null omit) kalsın.** Data’yı “olduğu gibi” demek: null alanları da yazmak; bu durumda global NON_NULL kaldırılır, önerilmez. |

**Kısa aksiyon listesi:**

1. **Davranış:** `JacksonConfig` ve `ApiResponse` üzerindeki `NON_NULL` kalsın; ek DTO’lardaki `@JsonInclude(NON_NULL)` isteğe bağlı (redundant ama zararsız).
2. **Dokümantasyon:** `docs/modules/cms-delivery.md` içinde “Page not found” örneğini şöyle güncelle:
   - Gerçek response: `"data"` anahtarı yok.
   - İsteğe bağlı: “Error ve data’sız success cevaplarında `data` alanı gönderilmez (NON_NULL).”
3. **Storefront (opsiyonel):** `lib/types.ts` içinde `ApiResponse<T>` için `data?: T` kullanılabilir; runtime ile uyum artar.

Bu plan, mevcut codebase ve `docs/README.md` yapısına uyumlu bir implementasyon özeti ve karar gerekçesidir.
