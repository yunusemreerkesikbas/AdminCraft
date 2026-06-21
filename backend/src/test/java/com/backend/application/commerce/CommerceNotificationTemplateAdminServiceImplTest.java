package com.backend.application.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.backend.application.commerce.dto.CommerceNotificationTemplateCommand;
import com.backend.application.service.mail.TemplateVariableRenderer;
import com.backend.domain.commerce.CommerceNotificationChannel;
import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationTemplate;
import com.backend.domain.commerce.repository.CommerceNotificationTemplateRepository;
import com.backend.testutil.BaseServiceTest;

class CommerceNotificationTemplateAdminServiceImplTest extends BaseServiceTest {

	@Mock private CommerceNotificationTemplateRepository templateRepository;
	@Mock private CommerceModuleAccessGuard commerceModuleAccessGuard;

	private CommerceNotificationTemplateAdminServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new CommerceNotificationTemplateAdminServiceImpl(
				templateRepository,
				commerceModuleAccessGuard,
				new TemplateVariableRenderer());
	}

	@Test
	void listTemplates_ShouldFilterByEventLanguageAndActive() {
		when(templateRepository.findAll(
				CommerceNotificationEventType.ORDER_PAID,
				CommerceNotificationChannel.EMAIL,
				"TR",
				true))
				.thenReturn(List.of(template("tpl-uid", "TR", true)));

		var result = service.listTemplates(CommerceNotificationEventType.ORDER_PAID, "tr-TR", true);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).templateUid()).isEqualTo("tpl-uid");
		assertThat(result.get(0).eventType()).isEqualTo("ORDER_PAID");
		verify(commerceModuleAccessGuard).assertEnabledForCurrentTenant();
	}

	@Test
	void updateTemplate_ShouldUpdateEditableFieldsOnly() {
		CommerceNotificationTemplate template = template("tpl-uid", "TR", true);
		when(templateRepository.findByUid("tpl-uid")).thenReturn(Optional.of(template));
		when(templateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		var result = service.updateTemplate(
				"tpl-uid",
				new CommerceNotificationTemplateCommand(" Updated subject ", " Updated {{orderNumber}} ", false));

		assertThat(result.subject()).isEqualTo("Updated subject");
		assertThat(result.content()).isEqualTo("Updated {{orderNumber}}");
		assertThat(result.active()).isFalse();
		assertThat(result.eventType()).isEqualTo("ORDER_PAID");
		assertThat(result.channel()).isEqualTo("EMAIL");
		assertThat(result.language()).isEqualTo("TR");
	}

	@Test
	void updateTemplate_ShouldRejectInvalidContent() {
		when(templateRepository.findByUid("tpl-uid")).thenReturn(Optional.of(template("tpl-uid", "TR", true)));

		assertThatThrownBy(() -> service.updateTemplate(
				"tpl-uid",
				new CommerceNotificationTemplateCommand("Subject", " ", true)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("commerce.notification.template.content.required");
	}

	@Test
	void previewTemplate_ShouldRenderSampleVariables() {
		CommerceNotificationTemplate template = template("tpl-uid", "TR", true);
		template.setSubject("Order {{orderNumber}}");
		template.setContent("Hello {{customerName}}, total {{orderTotal}} {{currencyIso}}");
		when(templateRepository.findByUid("tpl-uid")).thenReturn(Optional.of(template));

		var result = service.previewTemplate("tpl-uid");

		assertThat(result.subject()).isEqualTo("Order ORD-20260621-000001");
		assertThat(result.content()).contains("Hello Jane Doe, total 1250.00 TRY");
	}

	private CommerceNotificationTemplate template(String uid, String language, boolean active) {
		CommerceNotificationTemplate template = new CommerceNotificationTemplate();
		template.setId(1L);
		template.setUid(uid);
		template.setTemplateKey(CommerceNotificationEventType.ORDER_PAID);
		template.setChannel(CommerceNotificationChannel.EMAIL);
		template.setLanguage(language);
		template.setSubject("Subject");
		template.setContent("Content");
		template.setActive(active);
		template.setCreatedAt(LocalDateTime.of(2026, 6, 21, 12, 0));
		template.setUpdatedAt(LocalDateTime.of(2026, 6, 21, 12, 0));
		return template;
	}
}
