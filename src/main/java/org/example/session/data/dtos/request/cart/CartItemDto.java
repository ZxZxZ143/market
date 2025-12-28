package org.example.session.data.dtos.request.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDto {
    private Integer productId;
    private Integer quantity;
}
