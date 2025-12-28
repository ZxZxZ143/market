package org.example.session.mappers;

import org.example.session.data.dtos.response.cart.CartResDto;
import org.example.session.data.mappers.cart.CartMapper;
import org.example.session.db.entity.Cart;
import org.example.session.db.entity.CartItem;
import org.example.session.db.entity.Product;
import org.example.session.db.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class CartMapperTest {

    @Autowired
    private CartMapper mapper;

    @Test
    void toResDto_shouldMapBuyerId_items_andFillTotal() {
        User buyer = new User();
        buyer.setId(5);

        Product p1 = new Product();
        p1.setId(10);
        p1.setTitle("A");

        Product p2 = new Product();
        p2.setId(20);
        p2.setTitle("B");

        CartItem i1 = new CartItem();
        i1.setProduct(p1);
        i1.setPriceSnapshot(100L);
        i1.setQuantity(2);

        CartItem i2 = new CartItem();
        i2.setProduct(p2);
        i2.setPriceSnapshot(50L);
        i2.setQuantity(3);

        Cart cart = new Cart();
        cart.setId(1);
        cart.setBuyer(buyer);

        cart.setItems(new ArrayList<>(List.of(i1, i2)));

        CartResDto dto = mapper.toResDto(cart);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(5, dto.getBuyerId());

        Assertions.assertNotNull(dto.getItems());
        Assertions.assertEquals(2, dto.getItems().size());

        Assertions.assertEquals(350L, dto.getTotalAmount());

        Assertions.assertEquals(10, dto.getItems().get(0).getProductId());
        Assertions.assertEquals("A", dto.getItems().get(0).getProductTitle());
        Assertions.assertEquals(200L, dto.getItems().get(0).getSubtotal());
    }

    @Test
    void toResDto_totalAmountShouldBeZero_whenItemsNull() {
        User buyer = new User();
        buyer.setId(6);

        Cart cart = new Cart();
        cart.setBuyer(buyer);
        cart.setItems(null);

        CartResDto dto = mapper.toResDto(cart);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(6, dto.getBuyerId());
        Assertions.assertEquals(0L, dto.getTotalAmount());
    }

    @Test
    void toResDto_totalAmountShouldIgnoreInvalidLines() {
        User buyer = new User();
        buyer.setId(7);

        Product p = new Product();
        p.setId(1);
        p.setTitle("X");

        CartItem ok = new CartItem();
        ok.setProduct(p);
        ok.setPriceSnapshot(10L);
        ok.setQuantity(2);

        CartItem bad1 = new CartItem();
        bad1.setProduct(p);
        bad1.setPriceSnapshot(null);
        bad1.setQuantity(10);

        CartItem bad2 = new CartItem();
        bad2.setProduct(p);
        bad2.setPriceSnapshot(5L);
        bad2.setQuantity(null);

        Cart cart = new Cart();
        cart.setBuyer(buyer);
        cart.setItems(List.of(ok, bad1, bad2));

        CartResDto dto = mapper.toResDto(cart);

        Assertions.assertEquals(20L, dto.getTotalAmount());
    }
}
