package org.example.session.service;


import org.example.session.db.entity.*;
import org.example.session.db.repositoty.CartItemRepo;
import org.example.session.db.repositoty.CartRepo;
import org.example.session.db.repositoty.OrderRepo;
import org.example.session.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrderService {

    private final OrderRepo orderRepository;
    private final CartRepo cartRepository;
    private final CartItemRepo cartItemRepo;

    @Autowired
    public OrderService(OrderRepo orderRepository, CartRepo cartRepository, CartItemRepo cartItemRepo) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepo = cartItemRepo;
    }

    @Transactional
    public Order checkout(UserServiceImpl principal) {
        Cart cart = cartRepository.findByBuyer_Id(principal.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cart is empty"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Instant now = Instant.now();

        Order order = new Order();
        order.setBuyer(principal.getUser());
        order.setStatus("CREATED");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setTotalAmount(0L);

        long total = 0L;

        for (CartItem ci : cart.getItems()) {
            long price = ci.getPriceSnapshot() == null ? 0L : ci.getPriceSnapshot();
            int qty = ci.getQuantity() == null ? 0 : ci.getQuantity();
            long subtotal = price * ((long) qty);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setPriceSnapshot(price);
            oi.setQuantity(qty);
            oi.setSeller(ci.getProduct().getSeller());
            oi.setCreatedAt(now);

            order.getItems().add(oi);
            total = total + subtotal;
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        cartItemRepo.deleteAllByCart_Id(cart.getId());
        cart.setUpdatedAt(now);

        return saved;
    }

    @Transactional
    public Page<Order> myOrders(UserServiceImpl principal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findAllByBuyer_Id(principal.getUser().getId(), pageable);
    }

    @Transactional
    public Order getMyOrder(UserServiceImpl principal, Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.getBuyer().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedException("Not your order");
        }
        return order;
    }

    @Transactional
    public Order getAnyOrder(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    @Transactional
    public Order updateStatusByAdmin(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        String st = status == null ? order.getStatus() : status.toUpperCase();
        if (!st.equals("CREATED") && !st.equals("PAID") && !st.equals("SHIPPED")
                && !st.equals("CANCELLED") && !st.equals("COMPLETED")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        order.setStatus(st);
        order.setUpdatedAt(Instant.now());
        return order;
    }
}

