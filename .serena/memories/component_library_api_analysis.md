# Backend Component Library API Analysis

## Overview
The Component Library API is fully implemented with uid/uuid fields in both entities and DTOs. The API endpoints are available at `/api/components` (with base context-path `/api` configured in application.yml).

---

## 1. Base Entity Structure

### BaseEntity (Parent Class)
**File**: `backend/src/main/java/com/backend/domain/entity/BaseEntity.java`

Fields inherited by all entities:
- `id` (Long) - Primary key, auto-generated
- `uuid` (String, 36 chars) - Auto-generated, unique, NOT NULL
- `uid` (String, 50 chars) - Auto-generated, unique, NOT NULL
- `createdAt` (LocalDateTime) - Auto-set on creation
- `updatedAt` (LocalDateTime) - Auto-set on creation and update
- `createdBy` (Long) - User ID who created
- `updatedBy` (Long) - User ID who last updated

Auto-generation via `@PrePersist`:
- Uses `UuidUidGenerator.generateUuid()` for uuid
- Uses `UuidUidGenerator.generateUid()` for uid

### BaseI18nEntity (For i18n Records)
**File**: `backend/src/main/java/com/backend/domain/entity/BaseI18nEntity.java`

Inherits same fields as BaseEntity, plus:
- `language` (Language enum) - TR, EN, etc.

---

## 2. Component Entity

**File**: `backend/src/main/java/com/backend/domain/entity/Component.java`

Extends: `BaseEntity`

Fields:
- `componentTypeId` (Long) - FK to ComponentType
- `code` (String, 50 chars) - Unique, NOT NULL
- `name` (String, 100 chars) - NOT NULL
- `baseData` (JsonNode) - JSON configuration
- `status` (ComponentStatus enum) - DRAFT/ACTIVE

Unique Constraints:
- `uk_component_code` on `code`
- `uk_component_uid` on `uid` (inherited)

---

## 3. ComponentI18n Entity

**File**: `backend/src/main/java/com/backend/domain/entity/ComponentI18n.java`

Extends: `BaseI18nEntity`

Fields:
- `componentId` (Long) - FK to Component
- `baseLocalizedData` (JsonNode) - Localized JSON
- `status` (ComponentStatus enum) - DRAFT/ACTIVE
- `publishedAt` (LocalDateTime) - When published

Unique Constraints:
- `uk_component_i18n_component_lang` on (componentId, language)
- `uk_component_i18n_uid` on `uid`

---

## 4. ComponentType Entity

**File**: `backend/src/main/java/com/backend/domain/entity/ComponentType.java`

Extends: `BaseEntity`

Fields:
- `code` (String, 50 chars) - Unique, NOT NULL
- `name` (String, 100 chars) - NOT NULL
- `category` (String, 50 chars) - Optional
- `icon` (String, 50 chars) - Optional
- `isSystem` (Boolean) - Default false

Unique Constraints:
- `uk_component_type_code` on `code`
- `uk_component_type_uid` on `uid`

---

## 5. Response DTOs - Current Field Structure

### ComponentResponse
**File**: `backend/src/main/java/com/backend/presentation/dto/response/ComponentResponse.java`

Record fields:
```java
Long id,
String uuid,          // ✅ INCLUDED
String uid,           // ✅ INCLUDED
Long componentTypeId,
String code,
String name,
JsonNode baseData,
ComponentStatus status,
LocalDateTime createdAt,
LocalDateTime updatedAt
```

Mapping via `ComponentResponse.from(Component entity)` includes uuid and uid from entity.

---

### ComponentDetailResponse
**File**: `backend/src/main/java/com/backend/presentation/dto/response/ComponentDetailResponse.java`

Record fields:
```java
Long id,
String uuid,          // ✅ INCLUDED
String uid,           // ✅ INCLUDED
String code,
String name,
Long componentTypeId,
String componentTypeName,
JsonNode baseData,
ComponentStatus status,
LocalDateTime createdAt,
LocalDateTime updatedAt,
Map<String, ComponentI18nResponse> translations,
Metadata metadata
```

Nested Metadata record:
```java
record Metadata(int translationCount, int publishedTranslationCount)
```

---

### ComponentListItemResponse
**File**: `backend/src/main/java/com/backend/presentation/dto/response/ComponentListItemResponse.java`

Record fields:
```java
Long id,
String code,
String name,
Long componentTypeId,
String componentTypeName,
ComponentStatus status,
Integer entryCount
```

**NOTE**: uuid and uid are NOT included in list item response. This is intentional for list performance.

---

### ComponentListResponse
**File**: `backend/src/main/java/com/backend/presentation/dto/response/ComponentListResponse.java`

Record fields:
```java
ComponentResponse component,      // Contains uuid, uid
List<ComponentI18nResponse> translations
```

Wraps ComponentResponse which includes uuid and uid.

---

### ComponentI18nResponse
**File**: `backend/src/main/java/com/backend/presentation/dto/response/ComponentI18nResponse.java`

Record fields:
```java
Long id,
String uuid,          // ✅ INCLUDED
String uid,           // ✅ INCLUDED
Long componentId,
Language language,
JsonNode baseLocalizedData,
ComponentStatus status,
LocalDateTime publishedAt,
LocalDateTime updatedAt
```

---

### ComponentTypeResponse
**File**: `backend/src/main/java/com/backend/presentation/dto/response/ComponentTypeResponse.java`

Record fields:
```java
Long id,
String uuid,          // ✅ INCLUDED
String uid,           // ✅ INCLUDED
String code,
String name,
String category,
String icon,
Boolean isSystem,
LocalDateTime createdAt,
LocalDateTime updatedAt
```

---

## 6. REST Controller Endpoints

**File**: `backend/src/main/java/com/backend/presentation/controller/ComponentController.java`

Base Path: `/api/components` (context-path: `/api` + `@RequestMapping("/components")`)

### Endpoints:

1. **POST /api/components** - Create Component
   - Request: `ComponentCreateRequest`
   - Response: `ApiResponse<ComponentResponse>` (includes uuid, uid)
   - Auth: TENANT_ADMIN

2. **GET /api/components** - List All Components
   - Response: `ApiResponse<List<ComponentListItemResponse>>` (NO uuid/uid)
   - Auth: TENANT_ADMIN

3. **GET /api/components/{id}** - Get Component by ID
   - Optional param: `?include=translations`
   - Without `include`: `ApiResponse<ComponentResponse>` (includes uuid, uid)
   - With `include=translations`: `ApiResponse<ComponentDetailResponse>` (includes uuid, uid)
   - Auth: TENANT_ADMIN

4. **PUT /api/components/{id}** - Update Component
   - Request: `ComponentCreateRequest`
   - Response: `ApiResponse<ComponentResponse>` (includes uuid, uid)
   - Auth: TENANT_ADMIN

5. **DELETE /api/components/{id}** - Delete Component
   - Response: `ApiResponse<Void>`
   - Auth: TENANT_ADMIN

6. **GET /api/components/{id}/i18n/{language}** - Get Component i18n
   - Response: `ApiResponse<ComponentI18nResponse>` (includes uuid, uid)
   - Auth: TENANT_ADMIN

7. **PUT /api/components/{id}/i18n/{language}** - Upsert Component i18n
   - Request: `ComponentI18nRequest`
   - Response: `ApiResponse<ComponentI18nResponse>` (includes uuid, uid)
   - Auth: TENANT_ADMIN

8. **POST /api/components/{id}/publish/{language}** - Publish i18n
   - Response: `ApiResponse<ComponentI18nResponse>` (includes uuid, uid)
   - Auth: TENANT_ADMIN

---

## 7. Key Findings - Summary

### UUID/UID Presence:

| DTO | uuid | uid | Notes |
|-----|------|-----|-------|
| ComponentResponse | ✅ YES | ✅ YES | Full detail response |
| ComponentDetailResponse | ✅ YES | ✅ YES | For detail view with translations |
| ComponentListItemResponse | ❌ NO | ❌ NO | List optimization - intentional |
| ComponentListResponse | ✅ YES | ✅ YES | Wraps ComponentResponse |
| ComponentI18nResponse | ✅ YES | ✅ YES | i18n records have their own uid |
| ComponentTypeResponse | ✅ YES | ✅ YES | Full detail response |

### Entity Structure:

- All entities inherit from BaseEntity (Component, ComponentType) or BaseI18nEntity (ComponentI18n)
- uuid and uid are automatically generated on @PrePersist
- uuid: 36 chars, globally unique
- uid: 50 chars, short unique identifier

### DTO Mapping:

- `ComponentResponse.from()` maps entity.getUuid() and entity.getUid() directly
- `ComponentDetailResponse.from()` maps entity.getUuid() and entity.getUid()
- `ComponentI18nResponse.from()` maps entity.getUuid() and entity.getUid()
- `ComponentTypeResponse.from()` maps entity.getUuid() and entity.getUid()

### Current API Behavior:

- **Detail endpoints** (`GET /{id}`, `GET /{id}?include=translations`): Include uuid, uid
- **List endpoint** (`GET /`): Excludes uuid, uid from items (intentional for performance)
- **Create/Update/Delete endpoints**: Return ComponentResponse with uuid, uid
- **i18n endpoints**: Return ComponentI18nResponse with their own uuid, uid

---

## 8. Design Observations

1. **Consistent UUID/UID Generation**: Both BaseEntity and BaseI18nEntity use same UuidUidGenerator
2. **Unique Constraints**: All entities have unique constraints on uid column (database-level integrity)
3. **Performance Optimization**: List endpoint excludes uuid/uid for reduced payload size
4. **i18n Independence**: Component and ComponentI18n have separate uuid/uid values
5. **Database Schema**: Entity tables have indexes on uid and other common query fields

---

## Files Summary

### Domain Entities:
- `backend/src/main/java/com/backend/domain/entity/BaseEntity.java`
- `backend/src/main/java/com/backend/domain/entity/BaseI18nEntity.java`
- `backend/src/main/java/com/backend/domain/entity/Component.java`
- `backend/src/main/java/com/backend/domain/entity/ComponentI18n.java`
- `backend/src/main/java/com/backend/domain/entity/ComponentType.java`

### Response DTOs:
- `backend/src/main/java/com/backend/presentation/dto/response/ComponentResponse.java`
- `backend/src/main/java/com/backend/presentation/dto/response/ComponentDetailResponse.java`
- `backend/src/main/java/com/backend/presentation/dto/response/ComponentListItemResponse.java`
- `backend/src/main/java/com/backend/presentation/dto/response/ComponentListResponse.java`
- `backend/src/main/java/com/backend/presentation/dto/response/ComponentI18nResponse.java`
- `backend/src/main/java/com/backend/presentation/dto/response/ComponentTypeResponse.java`

### Controller:
- `backend/src/main/java/com/backend/presentation/controller/ComponentController.java`

### Request DTO:
- `backend/src/main/java/com/backend/presentation/dto/request/ComponentCreateRequest.java`

### Config:
- `backend/src/main/resources/application.yml` (context-path: `/api`)
