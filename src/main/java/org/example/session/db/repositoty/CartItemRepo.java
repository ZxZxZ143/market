package org.example.session.db.repositoty;

import org.example.session.db.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepo extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCart_IdAndProduct_Id(Integer cartId, Integer productId);
    void deleteByCart_IdAndProduct_Id(Integer cartId, Integer productId);
    void deleteAllByCart_Id(Integer cartId);
}
