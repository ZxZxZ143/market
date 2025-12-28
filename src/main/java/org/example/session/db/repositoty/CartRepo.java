package org.example.session.db.repositoty;

import org.example.session.db.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart, Integer> {
    @Query("select c from Cart c where c.buyer.id = :buyerId")
    Optional<Cart> findByBuyer_Id(Integer buyerId);
}
