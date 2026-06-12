package com.backend.domain.commerce;

import java.time.LocalDateTime;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "commerce_customer_consents", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_customer_consent_uid")
}, indexes = {
		@Index(columnList = "customer_id", name = "idx_commerce_customer_consent_customer"),
		@Index(columnList = "consent_type", name = "idx_commerce_customer_consent_type")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCustomerConsent extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@Enumerated(EnumType.STRING)
	@Column(name = "consent_type", nullable = false, length = 40)
	private CommerceCustomerConsentType consentType;

	@Column(nullable = false)
	private boolean accepted;

	@Column(name = "accepted_at", nullable = false)
	private LocalDateTime acceptedAt;

	@Column(nullable = false, length = 50)
	private String source;

	@Column(name = "ip_address", length = 45)
	private String ipAddress;

	@Column(name = "user_agent", length = 512)
	private String userAgent;
}
