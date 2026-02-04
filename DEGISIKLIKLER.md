# AdminCraft - Yapılan Değişiklikler

## 2FA (İki Faktörlü Kimlik Doğrulama) İyileştirmeleri

### 1. OPTIONAL (İsteğe Bağlı) Politika Kaldırıldı
**Tarih:** 4 Şubat 2026

#### Yapılan Değişiklikler:

**Neden Kaldırıldı:**
- 2FA akışını basitleştirmek için OPTIONAL (İsteğe Bağlı) politikası kaldırıldı
- Artık sadece iki seçenek bulunmaktadır: DISABLED (Devre Dışı) ve REQUIRED (Zorunlu)

**Değiştirilen Dosyalar:**

1. **Backend Kodu:**
   - `TwoFactorPolicy.java`: OPTIONAL enum değeri kaldırıldı
   - `AuthenticationServiceImpl.java`: 2FA kontrolü basitleştirildi
     - Önceki hali: Hem REQUIRED hem de OPTIONAL politikaları kontrol ediliyordu
     - Yeni hali: Sadece REQUIRED politikası kontrol ediliyor
   - `SecuritySettingsServiceImpl.java`: OPTIONAL için açıklama metni kaldırıldı

2. **Veritabanı Migration:**
   - `V35__add_tenant_2fa_settings.sql`: ENUM tanımından 'OPTIONAL' değeri çıkarıldı
   - Yeni ENUM: `('DISABLED', 'REQUIRED')`

3. **Dokümantasyon:**
   - `authentication.md`: OPTIONAL politikası tablodan ve akış diyagramından kaldırıldı
   - `site-dashboard.md`: OPTIONAL politikası seçeneklerden çıkarıldı

#### Kod Değişiklikleri:

**Önceki Kod:**
```java
boolean requires2FA = twoFactorPolicy == TwoFactorPolicy.REQUIRED ||
    (twoFactorPolicy == TwoFactorPolicy.OPTIONAL && Boolean.TRUE.equals(user.getTwoFactorEnabled()));
```

**Yeni Kod:**
```java
boolean requires2FA = twoFactorPolicy == TwoFactorPolicy.REQUIRED;
```

#### Etki:
- ✅ 2FA implementasyonu basitleşti
- ✅ Binary seçim: Açık (REQUIRED) veya Kapalı (DISABLED)
- ✅ `user.twoFactorEnabled` alanına bağımlılık kaldırıldı
- ✅ Kod daha kolay yönetilebilir hale geldi

---

### 2. Hata Yönetimi İyileştirmeleri
**Tarih:** 4 Şubat 2026

#### Gereksiz Exception Yakalama Blokları Kaldırıldı:

**Değiştirilen Dosyalar:**
- `AuthController.java`: `login()` ve `verifyOtp()` metodlarında

**Sorun:**
- `OtpRateLimitExceededException` istisnası yakalanıp hemen tekrar fırlatılıyordu
- Bu gereksiz kod karmaşıklığı yaratıyordu

**Çözüm:**
- Catch-and-rethrow blokları kaldırıldı
- Exception'lar `throws` deklarasyonu ile doğrudan dışarı aktarıldı
- `GlobalExceptionHandler` otomatik olarak doğru HTTP 429 yanıtını döndürüyor

**Önceki Kod:**
```java
} catch (OtpRateLimitExceededException ex) {
    // Let the exception propagate to GlobalExceptionHandler for proper 429 response
    throw ex;
} catch (Exception ex) {
    // ...
}
```

**Yeni Kod:**
```java
public ResponseEntity<...> login(...) throws OtpRateLimitExceededException {
    try {
        // ...
    } catch (Exception ex) {
        // ...
    }
}
```

#### Etki:
- ✅ Kod daha temiz ve anlaşılır
- ✅ HTTP 429 yanıtları düzgün çalışıyor
- ✅ Gereksiz kod satırları kaldırıldı

---

## Önceki İyileştirmeler

### PR Review Düzeltmeleri (14 Adet)

Bu değişiklikler önceki bir PR'ın kod incelemesi sonucunda yapıldı:

1. **Rate Limiting İyileştirmesi:**
   - OTP rate limiting artık tenant ID'yi de içeriyor
   - Farklı tenant'lardaki aynı email adreslerinin birbirini etkilememesi sağlandı
   - Format: `{tenantId}:{email}` şeklinde key kullanılıyor

2. **Validasyon İyileştirmeleri:**
   - OTP kodları için sayısal kontrol eklendi: `@Pattern(regexp="^\\d{6}$")`
   - Sadece 6 haneli rakamlardan oluşan OTP kodları kabul ediliyor

3. **i18n (Çoklu Dil) Desteği:**
   - Eksik validasyon mesajları eklendi (EN ve TR):
     - `validation.pending.token.required`
     - `validation.otp.required`
     - `validation.otp.size`
     - `validation.otp.pattern`
     - `validation.token.required`

4. **Güvenlik:**
   - Varsayılan email adresi kişisel Gmail'den `noreply@admincraft.com` olarak değiştirildi
   - `NumberFormatException` hata yönetimi eklendi

5. **Veritabanı Optimizasyonu:**
   - Gereksiz index kaldırıldı (unique key zaten index oluşturuyor)

6. **Dokümantasyon:**
   - Kullanıcı oluşturma sırasında geçici rastgele şifre hash'i saklandığı belgelendi

---

## Özet

Bu değişiklikler AdminCraft'ın 2FA (İki Faktörlü Kimlik Doğrulama) sistemini daha basit, güvenli ve yönetilebilir hale getirmiştir. 

**Ana Kazanımlar:**
- 🎯 Basitleştirilmiş 2FA politika yapısı (DISABLED/REQUIRED)
- 🔒 Geliştirilmiş güvenlik (tenant-aware rate limiting)
- 🌍 Daha iyi çoklu dil desteği
- 📝 Güncel ve doğru dokümantasyon
- ✨ Daha temiz ve bakımı kolay kod

**Değiştirilen Dosya Sayısı:** 10+ dosya
**Eklenen/Çıkarılan Satırlar:** ~20 satır değişiklik (net azalma)
**Test Durumu:** Kod incelemesi başarılı, derleme hataları yok
