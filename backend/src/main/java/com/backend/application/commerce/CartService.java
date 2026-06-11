package com.backend.application.commerce;

import com.backend.application.commerce.dto.CartResponse;

public interface CartService extends CommerceApplicationService {

    CartResponse createCart();

    CartResponse getCart(String cartToken);

    CartResponse addItem(String cartToken, String variantUid, Integer quantity);

    CartResponse updateItem(String cartToken, String itemUid, Integer quantity);

    CartResponse deleteItem(String cartToken, String itemUid);

    void clearCart(String cartToken);
}
