package org.example.session.db.repositoty;

import org.example.session.db.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepo extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProduct_Id(Integer productId);
}
