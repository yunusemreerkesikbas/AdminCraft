package com.backend.presentation.commerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import com.backend.application.commerce.CommerceNotificationTemplateAdminService;
import com.backend.application.commerce.dto.CommerceNotificationTemplateCommand;
import com.backend.application.commerce.dto.CommerceNotificationTemplatePreviewResponse;
import com.backend.application.commerce.dto.CommerceNotificationTemplateResponse;
import com.backend.domain.commerce.CommerceNotificationEventType;

@ExtendWith(MockitoExtension.class)
class CommerceAdminNotificationTemplateControllerTest {

	@Mock private CommerceNotificationTemplateAdminService templateAdminService;
	@Mock private MessageSource messageSource;

	@Test
	void list_ShouldReturnTemplates() {
		CommerceAdminNotificationTemplateController controller =
				new CommerceAdminNotificationTemplateController(templateAdminService, messageSource);
		when(templateAdminService.listTemplates(CommerceNotificationEventType.ORDER_PAID, "TR", true))
				.thenReturn(List.of(template()));
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Templates retrieved");

		var result = controller.list(CommerceNotificationEventType.ORDER_PAID, "TR", true);

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Templates retrieved");
		assertThat(result.getBody().getData()).hasSize(1);
	}

	@Test
	void update_ShouldPassCommandToService() {
		CommerceAdminNotificationTemplateController controller =
				new CommerceAdminNotificationTemplateController(templateAdminService, messageSource);
		when(templateAdminService.updateTemplate(anyString(), any())).thenReturn(template());
		when(messageSource.getMessage(anyString(), any(), anyString(), any(Locale.class)))
				.thenAnswer(invocation -> "Template updated");

		var result = controller.update(
				"tpl-uid",
				new CommerceNotificationTemplateRequest("Subject", "Content", false));

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getMessage()).isEqualTo("Template updated");
		ArgumentCaptor<CommerceNotificationTemplateCommand> commandCaptor =
				ArgumentCaptor.forClass(CommerceNotificationTemplateCommand.class);
		verify(templateAdminService).updateTemplate(org.mockito.ArgumentMatchers.eq("tpl-uid"), commandCaptor.capture());
		assertThat(commandCaptor.getValue().subject()).isEqualTo("Subject");
		assertThat(commandCaptor.getValue().content()).isEqualTo("Content");
		assertThat(commandCaptor.getValue().active()).isFalse();
	}

	@Test
	void preview_ShouldReturnRenderedTemplate() {
		CommerceAdminNotificationTemplateController controller =
				new CommerceAdminNotificationTemplateController(templateAdminService, messageSource);
		when(templateAdminService.previewTemplate("tpl-uid"))
				.thenReturn(new CommerceNotificationTemplatePreviewResponse("tpl-uid", "Rendered subject", "Rendered content"));

		var result = controller.preview("tpl-uid");

		assertThat(result.getBody()).isNotNull();
		assertThat(result.getBody().getData().subject()).isEqualTo("Rendered subject");
		assertThat(result.getBody().getData().content()).isEqualTo("Rendered content");
	}

	private CommerceNotificationTemplateResponse template() {
		return new CommerceNotificationTemplateResponse(
				"tpl-uid",
				"ORDER_PAID",
				"EMAIL",
				"TR",
				"Subject",
				"Content",
				true,
				LocalDateTime.of(2026, 6, 21, 12, 0),
				LocalDateTime.of(2026, 6, 21, 12, 0));
	}
}
