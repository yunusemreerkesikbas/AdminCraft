package com.backend.presentation.commerce;

import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
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
import com.backend.application.commerce.CommerceCustomerPrincipal;
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
    private final MessageSource messageSource;

    @PostMapping
    @Operation(summary = "Create anonymous cart")
    public ResponseEntity<ApiResponse<CartResponse>> createCart(
			Authentication authentication,
			HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
		return cartResponse("commerce.cart.created", cartService.createCart(principal(authentication)));
    }

    @GetMapping
    @Operation(summary = "Get anonymous cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
			@RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
			Authentication authentication,
            HttpServletRequest httpRequest) {
        rateLimitService.checkReadOrThrow(RequestUtils.getClientIpAddress(httpRequest));
		return cartResponse("commerce.cart.retrieved", cartService.getCart(cartToken, principal(authentication)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to anonymous cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
			Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request,
            HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
		return cartResponse("commerce.cart.item.added",
				cartService.addItem(cartToken, principal(authentication), request.variantUid(), request.quantity()));
    }

    @PatchMapping("/items/{itemUid}")
    @Operation(summary = "Update anonymous cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
			@RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
			Authentication authentication,
            @PathVariable String itemUid,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
		return cartResponse("commerce.cart.item.updated",
				cartService.updateItem(cartToken, principal(authentication), itemUid, request.quantity()));
    }

    @DeleteMapping("/items/{itemUid}")
    @Operation(summary = "Delete anonymous cart item")
    public ResponseEntity<ApiResponse<CartResponse>> deleteItem(
			@RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
			Authentication authentication,
            @PathVariable String itemUid,
            HttpServletRequest httpRequest) {
        rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
		return cartResponse("commerce.cart.item.deleted", cartService.deleteItem(cartToken, principal(authentication), itemUid));
    }

    @DeleteMapping
    @Operation(summary = "Clear anonymous cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
			@RequestHeader(value = CART_TOKEN_HEADER, required = false) String cartToken,
			Authentication authentication,
			HttpServletRequest httpRequest) {
		rateLimitService.checkMutationOrThrow(RequestUtils.getClientIpAddress(httpRequest));
		cartService.clearCart(cartToken, principal(authentication));
		return ResponseEntity.ok(ApiResponse.success(message("commerce.cart.cleared"), null));
    }

    private ResponseEntity<ApiResponse<CartResponse>> cartResponse(String messageKey, CartResponse response) {
		ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
		if (response.cartToken() != null) {
			builder.header(CART_TOKEN_HEADER, response.cartToken());
		}
		return builder.body(ApiResponse.success(message(messageKey), response));
    }

	private CommerceCustomerPrincipal principal(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof CommerceCustomerPrincipal principal)) {
			return null;
		}
		return principal;
	}

    private String message(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, key, locale);
    }
}
