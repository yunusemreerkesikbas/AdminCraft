package com.backend.application.commerce.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        String cartToken,
        String cartUid,
        String status,
        LocalDateTime expiresAt,
        List<CartItemResponse> items,
        CartTotalsResponse totals) {
}
