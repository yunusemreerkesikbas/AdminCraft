package com.backend.domain.commerce;

import java.time.LocalDateTime;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_customer_refresh_tokens", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_customer_refresh_uid"),
		@UniqueConstraint(columnNames = { "token_hash" }, name = "uk_commerce_customer_refresh_token_hash")
}, indexes = {
		@Index(columnList = "customer_id", name = "idx_commerce_customer_refresh_customer"),
		@Index(columnList = "expires_at", name = "idx_commerce_customer_refresh_expires"),
		@Index(columnList = "revoked_at", name = "idx_commerce_customer_refresh_revoked")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCustomerRefreshToken extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@Column(name = "token_hash", nullable = false, length = 64)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;

	@Column(name = "remember_me", nullable = false)
	private boolean rememberMe;

	@Column(name = "device_fingerprint", length = 255)
	private String deviceFingerprint;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@Column(name = "user_agent", length = 512)
	private String userAgent;

	public boolean isValid() {
		return revokedAt == null && expiresAt != null && LocalDateTime.now().isBefore(expiresAt);
	}

	public void revoke() {
		revokedAt = LocalDateTime.now();
	}
}
