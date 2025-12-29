package org.example.session.service;

import org.example.session.data.dtos.request.cart.CartItemDto;
import org.example.session.db.entity.*;
import org.example.session.db.repositoty.CartItemRepo;
import org.example.session.db.repositoty.CartRepo;
import org.example.session.db.repositoty.ProductRepo;
import org.example.session.security.user.UserServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

@Service
public class CartService {

    private final CartRepo cartRepository;
    private final CartItemRepo cartItemRepository;
    private final ProductRepo productRepository;

    public CartService(CartRepo cartRepository,
                       CartItemRepo cartItemRepository,
                       ProductRepo productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Cart getOrCreateMyCart(UserServiceImpl principal) {
        return cartRepository.findByBuyer_Id(principal.getUser().getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setBuyer(principal.getUser());
                    Instant now = Instant.now();
                    c.setCreatedAt(now);
                    c.setUpdatedAt(now);
                    return cartRepository.save(c);
                });
    }

    @Transactional()
    public Cart getMyCart(UserServiceImpl principal) {
        return cartRepository.findByBuyer_Id(principal.getUser().getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setBuyer(principal.getUser());
                    c.setCreatedAt(Instant.now());
                    c.setUpdatedAt(Instant.now());
                    return cartRepository.save(c);
                });
    }

    @Transactional
    public Cart addItem(UserServiceImpl principal, CartItemDto dto) {
        Cart cart = getOrCreateMyCart(principal);

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + dto.getProductId()));

        if ("ARCHIVED".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalArgumentException("Product is archived");
        }

        int addQty = dto.getQuantity() == null ? 1 : dto.getQuantity();
        if (addQty <= 0) addQty = 1;

        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .orElseGet(() -> {
                    CartItem ci = new CartItem();
                    ci.setCart(cart);
                    ci.setProduct(product);
                    ci.setQuantity(0);
                    ci.setPriceSnapshot(product.getPrice() == null ? 0 : product.getPrice());
                    Instant now = Instant.now();
                    ci.setCreatedAt(now);
                    return ci;
                });

        item.setQuantity(item.getQuantity() + addQty);

        cartItemRepository.save(item);

        cart.getItems().add(item);

        cartRepository.save(cart);
        cart.setUpdatedAt(Instant.now());
        return cart;
    }

    @Transactional
    public Cart setQuantity(UserServiceImpl principal, CartItemDto dto) {
        Cart cart = getOrCreateMyCart(principal);

        int qty = dto.getQuantity() == null ? 1 : dto.getQuantity();

        if (qty <= 0) {
            cartItemRepository.deleteByCart_IdAndProduct_Id(cart.getId(), dto.getProductId());
            cart.setUpdatedAt(Instant.now());
            return cart;
        }

        CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        item.setQuantity(qty);

        cart.setUpdatedAt(Instant.now());
        return cart;
    }

    @Transactional
    public Cart removeItem(UserServiceImpl principal, Integer productId) {
        Cart cart = getOrCreateMyCart(principal);
        cartItemRepository.deleteByCart_IdAndProduct_Id(cart.getId(), productId);
        cart.setUpdatedAt(Instant.now());
        return cart;
    }

    @Transactional
    public Cart clear(UserServiceImpl principal) {
        Cart cart = getOrCreateMyCart(principal);

        cartItemRepository.deleteAllByCart_Id(cart.getId());

        cart.setUpdatedAt(Instant.now());

        cartRepository.save(cart);
        return cart;
    }
}
