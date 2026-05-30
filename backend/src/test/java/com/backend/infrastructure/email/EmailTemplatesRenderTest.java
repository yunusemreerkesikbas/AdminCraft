package com.backend.infrastructure.email;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class EmailTemplatesRenderTest {

    private final TemplateEngine templateEngine = templateEngine();

    @ParameterizedTest
    @MethodSource("templates")
    void rendersSystemEmailTemplate(TemplateCase templateCase) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("fromName", "Craftive");
        templateCase.variables().forEach(context::setVariable);

        String html = templateEngine.process(templateCase.templateName(), context);

        assertThat(html)
                .contains("Craftive")
                .contains(templateCase.expectedText())
                .doesNotContain("${");
        if (templateCase.variables().containsKey("otpCode")) {
            assertThat(html).contains(templateCase.variables().get("otpCode").toString());
        }
    }

    static Stream<TemplateCase> templates() {
        return Stream.of(
                new TemplateCase(
                        "email/email-verify-en",
                        Map.of("verificationLink", "https://example.test/set-password?token=abc", "expiryHours", 24),
                        "Activate My Account"),
                new TemplateCase(
                        "email/email-verify-tr",
                        Map.of("verificationLink", "https://example.test/set-password?token=abc", "expiryHours", 24),
                        "Hesabımı Aktifleştir"),
                new TemplateCase(
                        "email/password-reset-en",
                        Map.of("resetLink", "https://example.test/reset-password?token=abc", "expiryHours", 1),
                        "Reset Password"),
                new TemplateCase(
                        "email/password-reset-tr",
                        Map.of("resetLink", "https://example.test/reset-password?token=abc", "expiryHours", 1),
                        "Şifremi Sıfırla"),
                new TemplateCase(
                        "email/login-otp-en",
                        Map.of("otpCode", "123456", "expiryMinutes", 5),
                        "123456"),
                new TemplateCase(
                        "email/login-otp-tr",
                        Map.of("otpCode", "123456", "expiryMinutes", 5),
                        "Doğrulama Kodu"),
                new TemplateCase(
                        "email/operation-otp-en",
                        Map.of("otpCode", "123456", "expiryMinutes", 5),
                        "Confirm Security Change"),
                new TemplateCase(
                        "email/operation-otp-tr",
                        Map.of("otpCode", "123456", "expiryMinutes", 5),
                        "Güvenlik Değişikliğini Onaylayın"),
                new TemplateCase(
                        "email/newsletter-confirm-en",
                        Map.of("confirmLink", "https://example.test/newsletter/confirm?token=abc"),
                        "Confirm Subscription"),
                new TemplateCase(
                        "email/newsletter-confirm-tr",
                        Map.of("confirmLink", "https://example.test/newsletter/confirm?token=abc"),
                        "Aboneliği Onayla"),
                new TemplateCase(
                        "email/demo-request-confirmation-en",
                        Map.of("name", "Ada"),
                        "We Received Your Request"),
                new TemplateCase(
                        "email/demo-request-confirmation-tr",
                        Map.of("name", "Ada"),
                        "Talebiniz Alındı"));
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        TemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private record TemplateCase(String templateName, Map<String, Object> variables, String expectedText) {
    }
}
