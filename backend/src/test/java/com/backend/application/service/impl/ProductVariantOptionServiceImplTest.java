package com.backend.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.backend.application.dto.request.ProductVariantOptionValueRequest;
import com.backend.application.dto.request.UpdateProductVariantOptionRequest;
import com.backend.application.service.ProductVariantOptionServiceImpl;
import com.backend.domain.entity.ProductVariantOption;
import com.backend.domain.entity.ProductVariantOptionValue;
import com.backend.domain.enums.ProductVariantOptionDisplayType;
import com.backend.domain.exception.BusinessRuleViolationException;
import com.backend.infrastructure.persistence.repository.ProductVariantOptionRepository;
import com.backend.infrastructure.persistence.repository.ProductVariantRepository;
import com.backend.testutil.BaseServiceTest;

class ProductVariantOptionServiceImplTest extends BaseServiceTest {

    @Mock
    private ProductVariantOptionRepository optionRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private ProductVariantOptionServiceImpl service;

    @Test
    void update_ShouldKeepExistingValue_WhenValueIdIsProvided() {
        ProductVariantOption option = optionWithValues(value(10L, "red", "Red"));
        when(optionRepository.findByIdWithValues(1L)).thenReturn(Optional.of(option));
        when(optionRepository.save(any(ProductVariantOption.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProductVariantOptionRequest request = new UpdateProductVariantOptionRequest(
                null,
                null,
                null,
                null,
                List.of(new ProductVariantOptionValueRequest(10L, "Crimson", "#dc143c", 0, true)));

        service.update(1L, request);

        assertThat(option.getValues()).hasSize(1);
        ProductVariantOptionValue updated = option.getValues().getFirst();
        assertThat(updated.getId()).isEqualTo(10L);
        assertThat(updated.getCode()).isEqualTo("red");
        assertThat(updated.getLabel()).isEqualTo("Crimson");
        assertThat(updated.getSwatchValue()).isEqualTo("#dc143c");
        verify(productVariantRepository, never()).findUsedOptionValueIds(any());
    }

    @Test
    void update_ShouldRejectRemovingUsedValue() {
        ProductVariantOption option = optionWithValues(
                value(10L, "red", "Red"),
                value(11L, "blue", "Blue"));
        when(optionRepository.findByIdWithValues(1L)).thenReturn(Optional.of(option));
        when(productVariantRepository.findUsedOptionValueIds(anyCollection())).thenReturn(List.of(11L));

        UpdateProductVariantOptionRequest request = new UpdateProductVariantOptionRequest(
                null,
                null,
                null,
                null,
                List.of(new ProductVariantOptionValueRequest(10L, "Red", null, 0, true)));

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("product.variant.option.value.in.use");

        verify(optionRepository, never()).save(any(ProductVariantOption.class));
    }

    @Test
    void delete_ShouldRejectUsedOption() {
        ProductVariantOption option = optionWithValues(value(10L, "red", "Red"));
        when(optionRepository.findByIdWithValues(1L)).thenReturn(Optional.of(option));
        when(productVariantRepository.existsByOptionId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("product.variant.option.in.use");

        verify(optionRepository, never()).delete(any(ProductVariantOption.class));
    }

    private ProductVariantOption optionWithValues(ProductVariantOptionValue... values) {
        ProductVariantOption option = new ProductVariantOption();
        option.setId(1L);
        option.setCode("color");
        option.setName("Color");
        option.setDisplayType(ProductVariantOptionDisplayType.COLOR);
        for (ProductVariantOptionValue value : values) {
            option.addValue(value);
        }
        return option;
    }

    private ProductVariantOptionValue value(Long id, String code, String label) {
        ProductVariantOptionValue value = new ProductVariantOptionValue();
        value.setId(id);
        value.setCode(code);
        value.setLabel(label);
        value.setSortOrder(0);
        value.setActive(true);
        return value;
    }
}
