package com.backend.application.service;

import com.backend.application.dto.request.PatchPlatformSettingsRequest;
import com.backend.presentation.dto.response.PlatformSettingsResponse;

public interface PlatformSettingsService {
    PlatformSettingsResponse getSettings();
    PlatformSettingsResponse patchSettings(PatchPlatformSettingsRequest request);
}
