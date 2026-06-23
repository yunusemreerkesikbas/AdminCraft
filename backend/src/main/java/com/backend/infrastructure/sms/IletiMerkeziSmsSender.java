package com.backend.infrastructure.sms;

import java.io.StringReader;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.backend.domain.port.SmsSenderPort;
import com.backend.domain.sms.SmsResult;
import com.backend.shared.common.LogSanitizer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.commerce.sms.provider", havingValue = "iletimerkezi")
public class IletiMerkeziSmsSender implements SmsSenderPort {

	private static final String SUCCESS_CODE = "200";

	private final CommerceSmsProperties properties;
	private final RestTemplate restTemplate;

	public IletiMerkeziSmsSender(CommerceSmsProperties properties, RestTemplateBuilder restTemplateBuilder) {
		this.properties = properties;
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
				.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
				.build();
	}

	@Override
	public SmsResult send(String toPhone, String content) {
		if (!hasProviderConfig()) {
			return SmsResult.failure("commerce.sms.provider.config.required");
		}
		String normalizedPhone = normalizePhone(toPhone);
		if (!StringUtils.hasText(normalizedPhone)) {
			return SmsResult.failure("commerce.sms.recipient.required");
		}
		if (!StringUtils.hasText(content)) {
			return SmsResult.failure("commerce.sms.content.required");
		}
		try {
			String response = restTemplate.postForObject(
					URI.create(properties.getIletiMerkezi().getBaseUrl().trim()),
					new HttpEntity<>(requestXml(normalizedPhone, content), xmlHeaders()),
					String.class);
			return parseResponse(response);
		} catch (RestClientException | IllegalArgumentException ex) {
			log.warn("Ileti Merkezi SMS dispatch failed reason={}", LogSanitizer.sanitizeForLog(ex.getMessage()));
			return SmsResult.failure("commerce.sms.provider.request.failed");
		}
	}

	private boolean hasProviderConfig() {
		CommerceSmsProperties.IletiMerkezi config = properties.getIletiMerkezi();
		return StringUtils.hasText(config.getBaseUrl())
				&& StringUtils.hasText(config.getKey())
				&& StringUtils.hasText(config.getHash())
				&& StringUtils.hasText(config.getSender())
				&& hasValidIysConfig(config);
	}

	private boolean hasValidIysConfig(CommerceSmsProperties.IletiMerkezi config) {
		String iys = normalizedIys(config);
		return "0".equals(iys) || ("1".equals(iys) && StringUtils.hasText(config.getIysList()));
	}

	private String normalizePhone(String phone) {
		if (phone == null) {
			return null;
		}
		return phone.trim().replaceAll("\\s+", "");
	}

	private HttpHeaders xmlHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_XML);
		headers.setAccept(java.util.List.of(MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.TEXT_PLAIN));
		return headers;
	}

	private String requestXml(String phone, String content) {
		CommerceSmsProperties.IletiMerkezi config = properties.getIletiMerkezi();
		String iys = normalizedIys(config);
		String iysListXml = "1".equals(iys)
				? "\n    <iysList>%s</iysList>".formatted(escapeXml(config.getIysList().trim()))
				: "";
		return """
				<request>
				  <authentication>
				    <key>%s</key>
				    <hash>%s</hash>
				  </authentication>
				  <order>
				    <sender>%s</sender>
				    <sendDateTime></sendDateTime>
				    <iys>%s</iys>%s
				    <message>
				      <text><![CDATA[%s]]></text>
				      <receipents>
				        <number>%s</number>
				      </receipents>
				    </message>
				  </order>
				</request>
				""".formatted(
				escapeXml(config.getKey().trim()),
				escapeXml(config.getHash().trim()),
				escapeXml(config.getSender().trim()),
				escapeXml(iys),
				iysListXml,
				escapeCdata(content.trim()),
				escapeXml(phone));
	}

	private String normalizedIys(CommerceSmsProperties.IletiMerkezi config) {
		return StringUtils.hasText(config.getIys()) ? config.getIys().trim() : "0";
	}

	private SmsResult parseResponse(String responseBody) {
		if (!StringUtils.hasText(responseBody)) {
			return SmsResult.failure("commerce.sms.provider.empty.response");
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setExpandEntityReferences(false);
			Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(responseBody)));
			String code = text(document, "code");
			if (SUCCESS_CODE.equals(code)) {
				return SmsResult.success(Objects.requireNonNullElse(text(document, "id"), "iletimerkezi"));
			}
			String message = text(document, "message");
			return SmsResult.failure(StringUtils.hasText(message)
					? LogSanitizer.sanitizeForLog(message)
					: "commerce.sms.provider.failed");
		} catch (Exception ex) {
			log.warn("Ileti Merkezi SMS response parse failed reason={}", LogSanitizer.sanitizeForLog(ex.getMessage()));
			return SmsResult.failure("commerce.sms.provider.response.invalid");
		}
	}

	private String text(Document document, String tagName) {
		var nodes = document.getElementsByTagName(tagName);
		if (nodes.getLength() == 0 || nodes.item(0) == null) {
			return null;
		}
		String text = nodes.item(0).getTextContent();
		return text == null ? null : text.trim();
	}

	private String escapeXml(String value) {
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	private String escapeCdata(String value) {
		return value.replace("]]>", "]]]]><![CDATA[>");
	}
}
