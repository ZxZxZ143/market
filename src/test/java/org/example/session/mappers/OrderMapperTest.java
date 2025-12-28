package org.example.session.mappers;

import org.example.session.data.dtos.response.order.OrderResDto;
import org.example.session.data.mappers.order.OrderMapper;
import org.example.session.db.entity.Order;
import org.example.session.db.entity.OrderItem;
import org.example.session.db.entity.Product;
import org.example.session.db.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

@SpringBootTest
class OrderMapperTest {

    @Autowired
    private OrderMapper mapper;

    @Test
    void toResDto_shouldMapBuyerId_andItems() {
        User buyer = new User();
        buyer.setId(9);

        Product p = new Product();
        p.setId(100);

        OrderItem oi = new OrderItem();
        oi.setProduct(p);
        oi.setQuantity(2);
        oi.setPriceSnapshot(50L);
        oi.setCreatedAt(Instant.now());

        Order order = new Order();
        order.setId(1);
        order.setBuyer(buyer);
        order.setStatus("CREATED");
        order.setTotalAmount(100L);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        order.setItems(List.of(oi));

        OrderResDto dto = mapper.toResDto(order);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(9, dto.getBuyerId());
        Assertions.assertEquals("CREATED", dto.getStatus());
        Assertions.assertEquals(100L, dto.getTotalAmount());

        Assertions.assertNotNull(dto.getItems());
        Assertions.assertEquals(1, dto.getItems().size());
        Assertions.assertEquals(100, dto.getItems().get(0).getProductId());
    }
}
