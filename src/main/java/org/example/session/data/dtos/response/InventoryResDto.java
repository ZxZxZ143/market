package org.example.session.data.dtos.response;

import lombok.Data;

import java.time.Instant;

@Data
public class InventoryResDto {
    private Long id;
    private Long productId;
    private Integer quantity;
    private Instant createdAt;
    private Instant updatedAt;
}
