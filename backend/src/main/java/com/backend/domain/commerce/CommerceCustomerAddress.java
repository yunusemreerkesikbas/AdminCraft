package com.backend.domain.commerce;

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
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_customer_addresses", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_customer_address_uid")
}, indexes = {
		@Index(columnList = "customer_id", name = "idx_commerce_customer_address_customer"),
		@Index(columnList = "customer_id, default_delivery", name = "idx_commerce_customer_address_delivery"),
		@Index(columnList = "customer_id, default_billing", name = "idx_commerce_customer_address_billing")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCustomerAddress extends BaseEntity {

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CommerceCustomer customer;

	@Column(length = 100)
	private String label;

	@NotBlank
	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@NotBlank
	@Column(name = "last_name", nullable = false, length = 100)
	private String lastName;

	@NotBlank
	@Column(nullable = false, length = 30)
	private String phone;

	@NotBlank
	@Column(name = "country_iso", nullable = false, length = 2)
	private String countryIso = "TR";

	@NotBlank
	@Column(nullable = false, length = 100)
	private String city;

	@NotBlank
	@Column(nullable = false, length = 100)
	private String district;

	@NotBlank
	@Column(name = "address_line1", nullable = false, length = 255)
	private String addressLine1;

	@Column(name = "address_line2", length = 255)
	private String addressLine2;

	@Column(name = "postal_code", length = 20)
	private String postalCode;

	@Column(name = "default_delivery", nullable = false)
	private boolean defaultDelivery;

	@Column(name = "default_billing", nullable = false)
	private boolean defaultBilling;

	@Enumerated(EnumType.STRING)
	@Column(name = "invoice_type", nullable = false, length = 20)
	private CommerceCustomerInvoiceType invoiceType = CommerceCustomerInvoiceType.INDIVIDUAL;

	@Column(name = "company_name", length = 200)
	private String companyName;

	@Column(name = "tax_number", length = 50)
	private String taxNumber;

	@Column(name = "tax_office", length = 100)
	private String taxOffice;

	@Column(name = "invoice_identity_number", length = 50)
	private String invoiceIdentityNumber;
}
