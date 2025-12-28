package org.example.session.data.dtos.response.order;


import lombok.Data;

@Data
public class OrderItemResDto {
    private Long id;

    private Long productId;
    private String productTitleSnapshot;

    private Long priceSnapshot;
    private Integer quantity;
    private Long subtotal;
}
