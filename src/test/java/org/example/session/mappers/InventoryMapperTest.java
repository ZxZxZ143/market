package org.example.session.mappers;

import org.example.session.data.dtos.response.InventoryResDto;
import org.example.session.data.mappers.InventoryMapper;
import org.example.session.db.entity.Inventory;
import org.example.session.db.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
class InventoryMapperTest {

    @Autowired
    private InventoryMapper mapper;

    @Test
    void toResDto_shouldMapProductId_andIgnoreCreatedAt() {
        Product product = new Product();
        product.setId(100);

        Inventory inventory = new Inventory();
        inventory.setId(1);
        inventory.setProduct(product);
        inventory.setQuantity(5);
        inventory.setReserved(2);
        inventory.setUpdatedAt(Instant.now());

        InventoryResDto dto = mapper.toResDto(inventory);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1, dto.getId());
        Assertions.assertEquals(100, dto.getProductId());
        Assertions.assertEquals(5, dto.getQuantity());

        Assertions.assertNull(dto.getCreatedAt());
    }
}
