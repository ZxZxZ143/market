package org.example.session.data.dtos.response.cart;


import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class CartResDto {
    private Long id;
    private Long buyerId;

    private Instant createdAt;
    private Instant updatedAt;

    private List<CartItemResDto> items;
    private Long totalAmount;
}
