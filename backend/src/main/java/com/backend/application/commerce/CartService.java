package com.backend.application.commerce;

import com.backend.application.commerce.dto.CartResponse;

public interface CartService extends CommerceApplicationService {

    CartResponse createCart();

	CartResponse createCart(CommerceCustomerPrincipal principal);

    CartResponse getCart(String cartToken);

	CartResponse getCart(String cartToken, CommerceCustomerPrincipal principal);

    CartResponse addItem(String cartToken, String variantUid, Integer quantity);

	CartResponse addItem(String cartToken, CommerceCustomerPrincipal principal, String variantUid, Integer quantity);

    CartResponse updateItem(String cartToken, String itemUid, Integer quantity);

	CartResponse updateItem(String cartToken, CommerceCustomerPrincipal principal, String itemUid, Integer quantity);

    CartResponse deleteItem(String cartToken, String itemUid);

	CartResponse deleteItem(String cartToken, CommerceCustomerPrincipal principal, String itemUid);

    void clearCart(String cartToken);

	void clearCart(String cartToken, CommerceCustomerPrincipal principal);
}
