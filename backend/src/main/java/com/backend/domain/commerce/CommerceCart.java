package com.backend.domain.commerce;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "commerce_carts", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "uid" }, name = "uk_commerce_cart_uid"),
		@UniqueConstraint(columnNames = { "token_hash" }, name = "uk_commerce_cart_token_hash")
}, indexes = {
		@Index(columnList = "status, expires_at", name = "idx_commerce_cart_status_expires"),
		@Index(columnList = "customer_id, status, expires_at", name = "idx_commerce_cart_customer_status_expires")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = { "customer", "items" })
@NoArgsConstructor
@AllArgsConstructor
public class CommerceCart extends BaseEntity {

	@Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

	@ToString.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id")
	private CommerceCustomer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommerceCartStatus status = CommerceCartStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ToString.Exclude
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<CommerceCartItem> items = new ArrayList<>();

    public void addItem(CommerceCartItem item) {
        items.add(item);
        item.setCart(this);
    }
}
