package com.backend.application.usecase;

import com.backend.application.dto.response.AdminUserResponse;
import com.backend.application.service.PasswordGeneratorService;
import com.backend.application.service.TenantAdminCreationService;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateTenantAdminUserUseCaseTest {

  @Mock
  private TenantPlatformRepository tenantRepository;

  @Mock
  private TenantAdminCreationService tenantAdminCreationService;

  @Mock
  private PasswordGeneratorService passwordGeneratorService;

  @InjectMocks
  private GenerateTenantAdminUserUseCase useCase;

  private Tenant activeTenant;

  @BeforeEach
  void setUp() {
    activeTenant = Tenant.builder()
        .id(1L)
        .subdomain("acme")
        .databaseName("ac_tenant_1")
        .status("ACTIVE")
        .hasAdminUser(false)
        .build();

    ReflectionTestUtils.setField(useCase, "frontendBaseUrl", "http://%s.localhost:4200");
  }

  @Test
  void shouldGenerateAdminUser_WhenTenantIsActiveAndNoAdmin() {
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant));
    when(passwordGeneratorService.generate()).thenReturn("Temp#Pass1!");

    AdminUserResponse response = useCase.execute(1L);

    verify(tenantAdminCreationService).createAdminUser(1L, "ac_tenant_1", "admin@acme.com", "Temp#Pass1!");
    verify(tenantRepository).save(any(Tenant.class));

    assertThat(response.email()).isEqualTo("admin@acme.com");
    assertThat(response.subdomain()).isEqualTo("acme");
    assertThat(response.loginUrl()).isEqualTo("http://acme.localhost:4200");
    assertThat(response.temporaryPassword()).isEqualTo("Temp#Pass1!");
  }

  @Test
  void shouldFail_WhenTenantNotActive() {
    Tenant t = Tenant.builder().id(2L).subdomain("beta").databaseName("ac_tenant_2").status("PENDING")
        .hasAdminUser(false).build();
    when(tenantRepository.findById(2L)).thenReturn(Optional.of(t));

    assertThatThrownBy(() -> useCase.execute(2L))
        .isInstanceOf(IllegalStateException.class);

    verify(tenantAdminCreationService, never()).createAdminUser(anyLong(), anyString(), anyString(), anyString());
  }

  @Test
  void shouldFail_WhenAdminAlreadyExists() {
    Tenant t = Tenant.builder().id(3L).subdomain("gamma").databaseName("ac_tenant_3").status("ACTIVE")
        .hasAdminUser(true).build();
    when(tenantRepository.findById(3L)).thenReturn(Optional.of(t));

    assertThatThrownBy(() -> useCase.execute(3L))
        .isInstanceOf(IllegalStateException.class);
  }
}
