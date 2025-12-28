package org.example.session.mappers;

import org.example.session.data.dtos.response.product.ProductResDto;
import org.example.session.data.mappers.ProductMapper;
import org.example.session.db.entity.Category;
import org.example.session.db.entity.Product;
import org.example.session.db.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Optional;

@SpringBootTest
class ProductMapperTest {

    @Autowired
    private ProductMapper mapper;

    @Test
    void toResDto_shouldMapNestedSellerAndCategory() {
        User seller = new User();
        seller.setId(7);
        seller.setEmail("seller@mail.com");

        Category category = new Category();
        category.setId(3);
        category.setName("Phones");

        Product product = new Product();
        product.setId(1);
        product.setSeller(seller);
        product.setCategory(category);
        product.setTitle("iPhone");
        product.setPrice(1000L);
        product.setStatus("ACTIVE");
        product.setCreatedAt(Instant.now());

        ProductResDto dto = mapper.toResDto(product);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1, dto.getId());
        Assertions.assertEquals("iPhone", dto.getTitle());
        Assertions.assertNotNull(dto.getPrice());
        Assertions.assertEquals(1000L, dto.getPrice());
        Assertions.assertEquals("ACTIVE", dto.getStatus());

        Assertions.assertEquals(7, dto.getSellerId());
        Assertions.assertEquals("seller@mail.com", dto.getSellerEmail());

        Assertions.assertEquals(3, dto.getCategoryId());
        Assertions.assertEquals("Phones", dto.getCategoryName());
    }
}