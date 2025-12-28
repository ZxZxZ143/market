package org.example.session.controllers.order;

import org.example.session.data.dtos.request.OrderStatusDto;
import org.example.session.data.dtos.response.order.OrderResDto;
import org.example.session.data.mappers.order.OrderMapper;
import org.example.session.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderAdminController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResDto> updateStatus(@PathVariable Integer id, @RequestBody OrderStatusDto dto) {
        var updated = orderService.updateStatusByAdmin(id, dto.getStatus());
        return ResponseEntity.ok(orderMapper.toResDto(updated));
    }
}
