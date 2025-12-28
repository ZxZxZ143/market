package org.example.session.mappers;

import org.example.session.data.dtos.response.order.OrderItemResDto;
import org.example.session.data.mappers.order.OrderItemMapper;
import org.example.session.db.entity.OrderItem;
import org.example.session.db.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderItemMapperTest {

    @Autowired
    private OrderItemMapper mapper;

    @Test
    void toResDto_shouldMapProductId_andIgnoreFields() {
        Product product = new Product();
        product.setId(77);
        product.setTitle("ShouldNotMap");

        OrderItem oi = new OrderItem();
        oi.setId(1);
        oi.setProduct(product);
        oi.setQuantity(4);
        oi.setPriceSnapshot(100L);

        OrderItemResDto dto = mapper.toResDto(oi);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(77, dto.getProductId());
        Assertions.assertEquals(4, dto.getQuantity());
        Assertions.assertEquals(100L, dto.getPriceSnapshot());

        Assertions.assertNull(dto.getProductTitleSnapshot());
        Assertions.assertNull(dto.getSubtotal());
    }
}
