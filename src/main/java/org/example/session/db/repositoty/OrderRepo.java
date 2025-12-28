package org.example.session.db.repositoty;

import org.example.session.db.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Integer> {
    Page<Order> findAllByBuyer_Id(Integer buyerId, Pageable pageable);
}
