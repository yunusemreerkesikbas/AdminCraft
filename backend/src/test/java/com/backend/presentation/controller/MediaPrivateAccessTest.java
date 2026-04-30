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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.backend.application.service.MediaService;
import com.backend.domain.entity.Media;
import com.backend.shared.common.SecurityHelper;

// SEC-009: verifies that private media files require authentication (MediaFileController — no class-level @PreAuthorize).
@ExtendWith(MockitoExtension.class)
class MediaPrivateAccessTest {

    @Mock
    private MediaService mediaService;
    @Mock
    private SecurityHelper securityHelper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MediaFileController controller = new MediaFileController(mediaService, securityHelper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Public media + anonymous → 200")
    void publicMedia_anonymous_200() throws Exception {
        Media media = mediaWithPublic("file.png", true);
        when(mediaService.findByFileName("file.png")).thenReturn(Optional.of(media));
        when(mediaService.getFileContent("file.png")).thenReturn(new byte[] { 1, 2, 3 });

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
        when(mediaService.getFileContent("secret.png")).thenReturn(new byte[] { 4, 5, 6 });
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
