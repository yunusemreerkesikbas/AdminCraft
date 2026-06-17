package com.backend.application.dto.delivery;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.domain.entity.Product;
import com.backend.domain.entity.ProductI18n;
import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;

class ProductListDeliveryResponseTest {

    @Test
    @DisplayName("Should include render-ready price while keeping basePrice")
    void from_IncludesRenderReadyPrice() {
        Product product = new Product();
        product.setUid("product-001");
        product.setSku("SKU-001");
        product.setBasePrice(BigDecimal.valueOf(149.90));

        ProductI18n translation = new ProductI18n();
        translation.setLanguage(Language.TR);
        translation.setName("Test Urun");
        translation.setShortDescription("Kisa aciklama");
        product.addI18n(translation);

        ProductListDeliveryResponse response = ProductListDeliveryResponse.from(product, Language.TR, Currency.TRY);

        assertThat(response.getBasePrice()).isEqualByComparingTo("149.90");
        assertThat(response.getPrice()).isNotNull();
        assertThat(response.getPrice().currencyIso()).isEqualTo("TRY");
        assertThat(response.getPrice().priceType()).isEqualTo("BUY");
        assertThat(response.getPrice().value()).isEqualByComparingTo("149.90");
        assertThat(response.getPrice().formattedValue()).isNotBlank();
    }
}
