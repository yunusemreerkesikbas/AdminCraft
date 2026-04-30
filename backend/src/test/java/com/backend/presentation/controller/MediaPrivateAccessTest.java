package com.backend.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.backend.application.service.MediaContainerService;
import com.backend.application.service.MediaI18nService;
import com.backend.application.service.MediaProcessingService;
import com.backend.application.service.MediaService;
import com.backend.domain.entity.Media;
import com.backend.shared.common.SecurityHelper;
import jakarta.validation.Validator;

/**
 * SEC-009: verifies that private media files require authentication.
 */
@ExtendWith(MockitoExtension.class)
class MediaPrivateAccessTest {

    @Mock private MediaService mediaService;
    @Mock private MediaI18nService i18nService;
    @Mock private MediaContainerService containerService;
    @Mock private MediaProcessingService processingService;
    @Mock private MessageSource messageSource;
    @Mock private SecurityHelper securityHelper;
    @Mock private Validator validator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MediaController controller = new MediaController(
                mediaService, i18nService, containerService,
                processingService, messageSource, securityHelper, new ObjectMapper(), validator);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("No DB record → 404")
    void noDbRecord_404() throws Exception {
        when(mediaService.findByFileName("ghost.png")).thenReturn(Optional.empty());

        mockMvc.perform(get("/media/files/ghost.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Public media + anonymous → 200")
    void publicMedia_anonymous_200() throws Exception {
        Media media = mediaWithPublic("file.png", true);
        when(mediaService.findByFileName("file.png")).thenReturn(Optional.of(media));
        when(mediaService.getFileContent("file.png")).thenReturn(new byte[]{1, 2, 3});
        // isAuthenticated() not called for public media — no stub needed

        mockMvc.perform(get("/media/files/file.png"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Private media + anonymous → 403")
    void privateMedia_anonymous_403() throws Exception {
        Media media = mediaWithPublic("secret.png", false);
        when(mediaService.findByFileName("secret.png")).thenReturn(Optional.of(media));
        when(securityHelper.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/media/files/secret.png"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Private media + authenticated → 200")
    void privateMedia_authenticated_200() throws Exception {
        Media media = mediaWithPublic("secret.png", false);
        when(mediaService.findByFileName("secret.png")).thenReturn(Optional.of(media));
        when(mediaService.getFileContent("secret.png")).thenReturn(new byte[]{4, 5, 6});
        when(securityHelper.isAuthenticated()).thenReturn(true);

        mockMvc.perform(get("/media/files/secret.png"))
                .andExpect(status().isOk());
    }

    private Media mediaWithPublic(String fileName, boolean isPublic) {
        Media m = new Media();
        m.setFileName(fileName);
        m.setIsPublic(isPublic);
        m.setMimeType("image/png");
        return m;
    }
}
