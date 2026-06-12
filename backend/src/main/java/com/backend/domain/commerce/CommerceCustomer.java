package com.backend.domain.commerce;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.backend.domain.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_customers", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_customer_uid"),
		@UniqueConstraint(columnNames = { "email_normalized" }, name = "uk_commerce_customer_email_normalized")
}, indexes = {
		@Index(columnList = "status", name = "idx_commerce_customer_status"),
		@Index(columnList = "email_verified", name = "idx_commerce_customer_email_verified")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "addresses", "consents", "refreshTokens", "socialIdentities" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCustomer extends BaseEntity {

	@NotBlank
	@Email
	@Column(nullable = false, length = 254)
	private String email;

	@NotBlank
	@Column(name = "email_normalized", nullable = false, length = 254)
	private String emailNormalized;

	@Column(name = "password_hash", length = 255)
	private String passwordHash;

	@NotBlank
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@NotBlank
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@NotBlank
	@Column(nullable = false, length = 30)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private CommerceCustomerGender gender;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CommerceCustomerStatus status = CommerceCustomerStatus.ACTIVE;

	@Column(name = "email_verified", nullable = false)
	private boolean emailVerified;

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@Column(name = "last_login_at")
	private LocalDateTime lastLoginAt;

	@Column(name = "last_login_ip", length = 45)
	private String lastLoginIp;

	@ToString.Exclude
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@OrderBy("id ASC")
	private List<CommerceCustomerAddress> addresses = new ArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CommerceCustomerConsent> consents = new ArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CommerceCustomerRefreshToken> refreshTokens = new ArrayList<>();

	@ToString.Exclude
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<CommerceCustomerSocialIdentity> socialIdentities = new ArrayList<>();

	public boolean canLogin() {
		return status == CommerceCustomerStatus.ACTIVE;
	}

	public void recordLogin(String ipAddress) {
		lastLoginAt = LocalDateTime.now();
		lastLoginIp = ipAddress;
	}
}
