package com.backend.presentation.commerce;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.application.commerce.CartService;
import com.backend.application.commerce.CommerceCartRateLimitService;
import com.backend.application.commerce.dto.CartResponse;
import com.backend.shared.common.ApiResponse;
import com.backend.shared.common.RequestUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/commerce/cart")
@RequiredArgsConstructor
@Tag(name = "Commerce Cart", description = "Public tenant-scoped anonymous cart API")
public class CommerceCartController {

    public static final String CART_TOKEN_HEADER = "X-Cart-Token";

    private final CartService cartService;
    private final CommerceCartRateLimitService rateLimitService;

    @PostMapping
    @Operation(summary = "Create anonymous cart")
    public ResponseEntity<ApiResponse<CartResponse>> createCart(HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Cart created successfully", cartService.createCart()));
    }

    @GetMapping
    @Operation(summary = "Get anonymous cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
            HttpServletRequest httpRequest) {
        rateLimitService.checkReadOrThrow(RequestUtils.getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(cartToken)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to anonymous cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
            @Valid @RequestBody AddCartItemRequest request,
            HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(
                "Cart item added successfully",
                cartService.addItem(cartToken, request.variantUid(), request.quantity())));
    }

    @PatchMapping("/items/{itemUid}")
    @Operation(summary = "Update anonymous cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            @RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
            @PathVariable String itemUid,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(
                "Cart item updated successfully",
                cartService.updateItem(cartToken, itemUid, request.quantity())));
    }

    @DeleteMapping("/items/{itemUid}")
    @Operation(summary = "Delete anonymous cart item")
    public ResponseEntity<ApiResponse<CartResponse>> deleteItem(
            @RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
            @PathVariable String itemUid,
            HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
        return ResponseEntity.ok(ApiResponse.success(
                "Cart item deleted successfully",
                cartService.deleteItem(cartToken, itemUid)));
    }

    @DeleteMapping
    @Operation(summary = "Clear anonymous cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
            HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
        cartService.clearCart(cartToken);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}
