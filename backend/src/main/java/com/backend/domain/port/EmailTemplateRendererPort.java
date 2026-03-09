package com.backend.domain.port;

import com.backend.application.dto.email.EmailContext;
import com.backend.domain.enums.Language;

public interface EmailTemplateRendererPort {

    String render(EmailContext context);

    String getSubject(String subjectKey, Language language, Object... args);
}
