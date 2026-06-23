package com.backend.infrastructure.sms;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

import com.backend.domain.sms.SmsResult;
import com.sun.net.httpserver.HttpServer;

class IletiMerkeziSmsSenderTest {

	private HttpServer server;

	@AfterEach
	void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void send_ShouldReturnSuccessWithProviderMessageId_WhenProviderAcceptsRequest() throws Exception {
		AtomicReference<String> requestBody = new AtomicReference<>();
		AtomicReference<String> contentType = new AtomicReference<>();
		startServer(resource("sms/iletimerkezi/send-sms-success-response.xml"), requestBody, contentType);
		IletiMerkeziSmsSender sender = new IletiMerkeziSmsSender(properties(serverUrl()), new RestTemplateBuilder());

		SmsResult result = sender.send(" +90 555 111 22 33 ", "Order paid");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getMessageId()).isEqualTo("323223");
		assertThat(contentType.get()).startsWith("text/xml");
		assertThat(normalizedXml(requestBody.get()))
				.isEqualTo(normalizedXml(resource("sms/iletimerkezi/send-sms-request.xml")));
	}

	@Test
	void send_ShouldReturnFailure_WhenProviderReturnsBusinessError() throws Exception {
		startServer(resource("sms/iletimerkezi/send-sms-error-response.xml"), new AtomicReference<>(), new AtomicReference<>());
		IletiMerkeziSmsSender sender = new IletiMerkeziSmsSender(properties(serverUrl()), new RestTemplateBuilder());

		SmsResult result = sender.send("905551112233", "Order paid");

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getErrorMessage()).isEqualTo("Uyelik bilgileri hatali");
	}

	@Test
	void send_ShouldReturnFailure_WhenCredentialsAreMissing() {
		CommerceSmsProperties properties = properties("http://localhost");
		properties.getIletiMerkezi().setHash(null);
		IletiMerkeziSmsSender sender = new IletiMerkeziSmsSender(properties, new RestTemplateBuilder());

		SmsResult result = sender.send("905551112233", "Order paid");

		assertThat(result.isSuccess()).isFalse();
		assertThat(result.getErrorMessage()).isEqualTo("commerce.sms.provider.config.required");
	}

	@Test
	void consoleSender_ShouldReturnStableSyntheticMessageId() {
		ConsoleSmsSender sender = new ConsoleSmsSender();

		SmsResult result = sender.send("905551112233", "Order paid");

		assertThat(result.isSuccess()).isTrue();
		assertThat(result.getMessageId()).startsWith("console-sms-");
	}

	private void startServer(
			String responseBody,
			AtomicReference<String> requestBody,
			AtomicReference<String> contentType) throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/v1/send-sms", exchange -> {
			requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
			byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/xml; charset=UTF-8");
			exchange.sendResponseHeaders(200, bytes.length);
			try (OutputStream response = exchange.getResponseBody()) {
				response.write(bytes);
			}
		});
		server.start();
	}

	private String serverUrl() {
		return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/send-sms";
	}

	private CommerceSmsProperties properties(String baseUrl) {
		CommerceSmsProperties properties = new CommerceSmsProperties();
		properties.getIletiMerkezi().setBaseUrl(baseUrl);
		properties.getIletiMerkezi().setKey("api-key");
		properties.getIletiMerkezi().setHash("api-hash");
		properties.getIletiMerkezi().setSender("CRAFTIVE");
		return properties;
	}

	private String resource(String path) {
		try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
			if (stream == null) {
				throw new IllegalStateException("Missing test resource: " + path);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private String normalizedXml(String xml) {
		return xml.replaceAll(">\\s+<", "><").trim();
	}
}
