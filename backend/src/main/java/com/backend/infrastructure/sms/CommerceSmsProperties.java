package com.backend.infrastructure.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.commerce.sms")
public class CommerceSmsProperties {

	private String provider = "disabled";
	private int timeoutSeconds = 10;
	private IletiMerkezi iletiMerkezi = new IletiMerkezi();

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public int getTimeoutSeconds() {
		return Math.max(1, timeoutSeconds);
	}

	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}

	public IletiMerkezi getIletiMerkezi() {
		return iletiMerkezi;
	}

	public void setIletiMerkezi(IletiMerkezi iletiMerkezi) {
		this.iletiMerkezi = iletiMerkezi == null ? new IletiMerkezi() : iletiMerkezi;
	}

	public static class IletiMerkezi {

		private String baseUrl = "https://api.iletimerkezi.com/v1/send-sms";
		private String key;
		private String hash;
		private String sender;
		private String iys = "0";
		private String iysList;

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getHash() {
			return hash;
		}

		public void setHash(String hash) {
			this.hash = hash;
		}

		public String getSender() {
			return sender;
		}

		public void setSender(String sender) {
			this.sender = sender;
		}

		public String getIys() {
			return iys;
		}

		public void setIys(String iys) {
			this.iys = iys;
		}

		public String getIysList() {
			return iysList;
		}

		public void setIysList(String iysList) {
			this.iysList = iysList;
		}
	}
}
