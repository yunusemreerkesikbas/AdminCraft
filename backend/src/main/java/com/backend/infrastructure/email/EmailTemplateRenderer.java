package com.backend.infrastructure.email;

import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.backend.application.dto.email.EmailContext;
import com.backend.domain.enums.Language;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTemplateRenderer {

    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;
    private final EmailProperties emailProperties;

    public String render(EmailContext emailContext) {
        Context context = new Context(toLocale(emailContext.getLanguage()));

        context.setVariable("fromName", emailProperties.getFromName());

        if (emailContext.getVariables() != null) {
            emailContext.getVariables().forEach(context::setVariable);
        }

        String templateName = emailContext.getTemplateName();
        log.debug("Rendering email template: {}", templateName);

        return templateEngine.process(templateName, context);
    }

    public String render(String templateName, Map<String, Object> variables, Language language) {
        Context context = new Context(toLocale(language));
        context.setVariable("fromName", emailProperties.getFromName());

        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        return templateEngine.process(templateName, context);
    }

    public String getSubject(String subjectKey, Language language, Object... args) {
        return messageSource.getMessage(subjectKey, args, toLocale(language));
    }

    private Locale toLocale(Language language) {
        return switch (language) {
            case TR -> new Locale("tr", "TR");
            case EN -> Locale.ENGLISH;
            default -> new Locale("tr", "TR");
        };
    }
}
