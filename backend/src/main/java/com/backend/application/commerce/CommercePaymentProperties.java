package com.backend.application.commerce;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.commerce.payment")
public class CommercePaymentProperties {

	private String callbackBaseUrl = "http://localhost:8080/api";
}
