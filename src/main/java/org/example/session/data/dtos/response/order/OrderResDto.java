package org.example.session.data.dtos.response.order;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResDto {
    private Long id;
    private Long buyerId;

    private String status;
    private Long totalAmount;

    private Instant createdAt;
    private Instant updatedAt;

    private List<OrderItemResDto> items;
}
