package org.example.session.controllers;

import org.example.session.data.dtos.request.cart.CartItemDto;
import org.example.session.data.dtos.response.cart.CartResDto;
import org.example.session.data.mappers.cart.CartMapper;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer/cart")
public class CartBuyerController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @Autowired
    public CartBuyerController(CartService cartService, CartMapper cartMapper) {
        this.cartService = cartService;
        this.cartMapper = cartMapper;
    }

    @GetMapping
    public ResponseEntity<CartResDto> getMyCart(@AuthenticationPrincipal UserServiceImpl principal) {
        CartResDto res = cartMapper.toResDto(cartService.getMyCart(principal));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResDto> addItem(@AuthenticationPrincipal UserServiceImpl principal,
                                              @RequestBody CartItemDto dto) {
        CartResDto res = cartMapper.toResDto(cartService.addItem(principal, dto));
        return ResponseEntity.ok(res);
    }

    @PutMapping("/items")
    public ResponseEntity<CartResDto> setQuantity(@AuthenticationPrincipal UserServiceImpl principal,
                                                  @RequestBody CartItemDto dto) {
        CartResDto res = cartMapper.toResDto(cartService.setQuantity(principal, dto));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal UserServiceImpl principal,
                                           @PathVariable Integer productId) {
        cartService.removeItem(principal, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal UserServiceImpl principal) {
        cartService.clear(principal);
        return ResponseEntity.noContent().build();
    }
}
