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
@Table(name = "commerce_customer_social_identities", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_customer_social_uid"),
		@UniqueConstraint(columnNames = { "provider", "provider_subject" }, name = "uk_commerce_customer_social_provider_subject")
}, indexes = {
		@Index(columnList = "customer_id", name = "idx_commerce_customer_social_customer")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCustomerSocialIdentity extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@Column(nullable = false, length = 30)
	private String provider;

	@Column(name = "provider_subject", nullable = false, length = 191)
	private String providerSubject;

	@Column(name = "email_snapshot", length = 254)
	private String emailSnapshot;

	@Column(name = "linked_at", nullable = false)
	private LocalDateTime linkedAt;
}
