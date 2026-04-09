package com.backend.domain.port;

import java.util.Map;

import com.backend.application.dto.email.EmailContext;
import com.backend.domain.enums.Language;

public interface EmailTemplateRendererPort {

    String render(EmailContext context);

    String render(String templateName, Map<String, Object> variables, Language language);

    String getSubject(String subjectKey, Language language, Object... args);
}
