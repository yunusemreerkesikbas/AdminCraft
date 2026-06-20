package com.backend.application.commerce;

import java.util.List;

import com.backend.application.commerce.dto.CheckoutLegalResponse;
import com.backend.application.commerce.dto.CommerceLegalAcceptanceCommand;
import com.backend.application.commerce.dto.CommerceLegalTemplateCommand;
import com.backend.application.commerce.dto.CommerceLegalTemplatePreviewResponse;
import com.backend.application.commerce.dto.CommerceLegalTemplateResponse;
import com.backend.domain.commerce.CommerceCheckout;
import com.backend.domain.commerce.CommerceLegalTemplateStatus;
import com.backend.domain.commerce.CommerceLegalTemplateType;

public interface CommerceLegalService {

	List<CommerceLegalTemplateResponse> listTemplates(
			CommerceLegalTemplateType type,
			String language,
			CommerceLegalTemplateStatus status);

	CommerceLegalTemplateResponse getTemplate(String templateUid);

	CommerceLegalTemplateResponse createTemplate(CommerceLegalTemplateCommand command);

	CommerceLegalTemplateResponse updateTemplate(String templateUid, CommerceLegalTemplateCommand command);

	CommerceLegalTemplateResponse publishTemplate(String templateUid);

	CommerceLegalTemplateResponse archiveTemplate(String templateUid);

	CommerceLegalTemplatePreviewResponse previewTemplate(String templateUid);

	CheckoutLegalResponse legalForCheckout(CommerceCheckout checkout, String language);

	String captureAcceptanceJson(
			CommerceCheckout checkout,
			String language,
			List<CommerceLegalAcceptanceCommand> acceptances);
}
