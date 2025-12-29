package org.example.session.data.dtos.request.cart;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class CartItemDto {
    private Integer productId;
    private Integer quantity;
}
