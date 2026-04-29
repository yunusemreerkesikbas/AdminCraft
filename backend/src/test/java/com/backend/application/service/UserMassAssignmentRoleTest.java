package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.application.dto.UpdateUserInput;
import com.backend.domain.entity.User;
import com.backend.domain.enums.UserRole;
import com.backend.domain.exception.UserNotFoundException;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.UserRepository;
import com.backend.shared.common.SecurityHelper;

/**
 * SEC-109: verifies that role changes are blocked in the generic update path
 * and that the dedicated updateUserRole() method enforces invariants.
 */
@ExtendWith(MockitoExtension.class)
class UserMassAssignmentRoleTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordGeneratorService passwordGeneratorService;
    @Mock private EmailService emailService;
    @Mock private OtpService otpService;
    @Mock private TenantContextPort tenantContext;
    @Mock private SecurityHelper securityHelper;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(
                userRepository, passwordEncoder, passwordGeneratorService,
                emailService, otpService, tenantContext, securityHelper);
    }

    @Test
    @DisplayName("updateUser with role field → role ignored, not persisted")
    void updateUser_roleFieldIgnored() {
        User user = userWithRole(42L, UserRole.VIEWER);
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserInput input = new UpdateUserInput(
                null, UserRole.SUPER_ADMIN, "Alice", null, null, null, null, null, null);
        service.updateUser(42L, input);

        // role must remain VIEWER — the input role was ignored (SEC-109)
        org.assertj.core.api.Assertions.assertThat(user.getRole()).isEqualTo(UserRole.VIEWER);
    }

    @Test
    @DisplayName("updateUserRole self-promotion → AccessDeniedException")
    void updateUserRole_selfPromotion_denied() {
        when(securityHelper.getCurrentUserIdOrNull()).thenReturn(99L);

        assertThatThrownBy(() -> service.updateUserRole(99L, UserRole.TENANT_ADMIN))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("own role");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("updateUserRole SUPER_ADMIN assignment → AccessDeniedException")
    void updateUserRole_superAdminAssignment_denied() {
        when(securityHelper.getCurrentUserIdOrNull()).thenReturn(1L);

        assertThatThrownBy(() -> service.updateUserRole(99L, UserRole.SUPER_ADMIN))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("SUPER_ADMIN");

        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("updateUserRole valid change → role persisted")
    void updateUserRole_validChange_persisted() {
        User user = userWithRole(55L, UserRole.TENANT_ADMIN);
        when(securityHelper.getCurrentUserIdOrNull()).thenReturn(1L);
        when(userRepository.findById(55L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        service.updateUserRole(55L, UserRole.VIEWER);

        org.assertj.core.api.Assertions.assertThat(user.getRole()).isEqualTo(UserRole.VIEWER);
        verify(userRepository).save(user);
    }

    private User userWithRole(Long id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        u.setEmail("test@example.com");
        return u;
    }
}
