package org.example.session.db.repositoty;

import org.example.session.db.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Integer> {
    Page<Product> findAllByStatus(String status, Pageable pageable);
    Page<Product> findAllBySeller_Id(Integer sellerId, Pageable pageable);
    Optional<Product> findByIdAndSeller_Id(Integer id, Integer sellerId);
}
