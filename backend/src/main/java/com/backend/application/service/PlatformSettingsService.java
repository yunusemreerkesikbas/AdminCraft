package com.backend.application.service;

import com.backend.application.dto.request.PatchPlatformSettingsRequest;
import com.backend.application.dto.response.PlatformSettingsData;

public interface PlatformSettingsService {
    PlatformSettingsData getSettings();
    PlatformSettingsData patchSettings(PatchPlatformSettingsRequest request);
}
