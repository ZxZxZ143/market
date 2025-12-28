package org.example.session.controllers.order;

import org.example.session.data.dtos.response.order.OrderResDto;
import org.example.session.data.mappers.order.OrderMapper;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer/orders")
public class OrderBuyerController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderBuyerController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResDto> checkout(@AuthenticationPrincipal UserServiceImpl principal) {
        var created = orderService.checkout(principal);
        var res = orderMapper.toResDto(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResDto>> myOrders(
            @AuthenticationPrincipal UserServiceImpl principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<OrderResDto> res = orderService.myOrders(principal, page, size).map(orderMapper::toResDto);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResDto> getMyOrder(
            @AuthenticationPrincipal UserServiceImpl principal,
            @PathVariable Integer id
    ) {
        OrderResDto res = orderMapper.toResDto(orderService.getMyOrder(principal, id));
        return ResponseEntity.ok(res);
    }
}