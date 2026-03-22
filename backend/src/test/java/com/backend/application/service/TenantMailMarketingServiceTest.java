package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.port.FrontendConfigPort;
import com.backend.domain.port.MailConfigPort;
import com.backend.domain.port.MailSenderPort;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.port.TenantMailSenderPort;
import com.backend.domain.repository.MailCampaignRepository;
import com.backend.domain.repository.MailOutboxRepository;
import com.backend.domain.repository.MailProviderConfigRepository;
import com.backend.domain.repository.MailTemplateRepository;
import com.backend.domain.repository.NewsletterSubscriberRepository;
import com.backend.domain.repository.NewsletterSubscriberSubscriptionRepository;
import com.backend.shared.common.SecurityHelper;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantMailMarketingService Tests")
class TenantMailMarketingServiceTest {

	@Mock
	private MailTemplateRepository templateRepository;

	@Mock
	private NewsletterSubscriberRepository subscriberRepository;

	@Mock
	private NewsletterSubscriberSubscriptionRepository subscriberSubscriptionRepository;

	@Mock
	private MailProviderConfigRepository providerConfigRepository;

	@Mock
	private MailCampaignRepository campaignRepository;

	@Mock
	private MailOutboxRepository outboxRepository;

	@Mock
	private TenantModuleAccessService tenantModuleAccessService;

	@Mock
	private MailConfigPort mailConfig;

	@Mock
	private MailSenderPort mailSender;

	@Mock
	private TenantMailSenderPort tenantMailSender;

	@Mock
	private EncryptionServicePort encryptionService;

	@Mock
	private TemplateVariableRenderer templateVariableRenderer;

	@Mock
	private FrontendConfigPort frontendConfig;

	@Mock
	private TenantContextPort tenantContext;

	@Mock
	private SecurityHelper securityHelper;

	@Mock
	private PlatformTransactionManager tenantTransactionManager;

	@InjectMocks
	private TenantMailMarketingService tenantMailMarketingService;

	@Test
	@DisplayName("getTemplateTypes should fail fast when mail marketing module is disabled")
	void getTemplateTypes_ShouldFailFast_WhenMailMarketingModuleDisabled() {
		doThrow(new IllegalStateException("mail.marketing.module.not.enabled"))
				.when(tenantModuleAccessService)
				.assertEnabledForCurrentTenant(
						ModuleCode.MAIL_MARKETING,
						"mail.marketing.tenant.context.required",
						"mail.marketing.module.not.enabled");

		assertThatThrownBy(() -> tenantMailMarketingService.getTemplateTypes())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("mail.marketing.module.not.enabled");

		verifyNoInteractions(templateRepository, campaignRepository, subscriberSubscriptionRepository);
	}
}
