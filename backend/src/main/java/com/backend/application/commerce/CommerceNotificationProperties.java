package com.backend.application.commerce;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.commerce.notifications")
public class CommerceNotificationProperties {

	private boolean retryJobEnabled = true;

	private int maxRetryAttempts = 3;

	private int retryBatchSize = 50;

	private Duration retryDelay = Duration.ofMinutes(15);

	public boolean isRetryJobEnabled() {
		return retryJobEnabled;
	}

	public void setRetryJobEnabled(boolean retryJobEnabled) {
		this.retryJobEnabled = retryJobEnabled;
	}

	public int getMaxRetryAttempts() {
		return Math.max(0, maxRetryAttempts);
	}

	public void setMaxRetryAttempts(int maxRetryAttempts) {
		this.maxRetryAttempts = maxRetryAttempts;
	}

	public int getRetryBatchSize() {
		return Math.max(1, retryBatchSize);
	}

	public void setRetryBatchSize(int retryBatchSize) {
		this.retryBatchSize = retryBatchSize;
	}

	public Duration getRetryDelay() {
		return retryDelay == null ? Duration.ofMinutes(15) : retryDelay;
	}

	public void setRetryDelay(Duration retryDelay) {
		this.retryDelay = retryDelay;
	}
}
