package org.example.session.data.dtos.response.cart;

import lombok.Data;

@Data
public class CartItemResDto {
    private Long id;

    private Long productId;
    private String productTitle;

    private Integer quantity;
    private Long priceSnapshot;

    private Long subtotal;
}
