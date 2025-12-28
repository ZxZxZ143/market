package org.example.session.mappers;

import org.example.session.data.dtos.response.cart.CartItemResDto;
import org.example.session.data.mappers.cart.CartItemMapper;
import org.example.session.db.entity.CartItem;
import org.example.session.db.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CartItemMapperTest {

    @Autowired
    private CartItemMapper mapper;

    @Test
    void toResDto_shouldMapProductFields_andSubtotal() {
        Product product = new Product();
        product.setId(10);
        product.setTitle("Phone");

        CartItem ci = new CartItem();
        ci.setId(1);
        ci.setProduct(product);
        ci.setPriceSnapshot(200L);
        ci.setQuantity(3);

        CartItemResDto dto = mapper.toResDto(ci);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(10, dto.getProductId());
        Assertions.assertEquals("Phone", dto.getProductTitle());
        Assertions.assertEquals(200L, dto.getPriceSnapshot());
        Assertions.assertEquals(3, dto.getQuantity());
        Assertions.assertEquals(600L, dto.getSubtotal());
    }

    @Test
    void toResDto_subtotal_shouldBeZero_whenPriceOrQtyNull() {
        Product product = new Product();
        product.setId(11);
        product.setTitle("TV");

        CartItem ci1 = new CartItem();
        ci1.setProduct(product);
        ci1.setPriceSnapshot(null);
        ci1.setQuantity(2);

        CartItemResDto dto1 = mapper.toResDto(ci1);
        Assertions.assertEquals(0L, dto1.getSubtotal());

        CartItem ci2 = new CartItem();
        ci2.setProduct(product);
        ci2.setPriceSnapshot(100L);
        ci2.setQuantity(null);

        CartItemResDto dto2 = mapper.toResDto(ci2);
        Assertions.assertEquals(0L, dto2.getSubtotal());
    }
}