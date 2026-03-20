package com.backend.application.dto.delivery;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(NON_NULL)
public record LayoutLinkDeliveryDto(
    String uid,
    String label,
    String href,
    boolean isExternal,
    String target,
    String color
) {}
