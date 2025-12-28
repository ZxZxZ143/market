package org.example.session.service;

import jakarta.transaction.Transactional;
import org.example.session.data.dtos.request.InventoryDto;
import org.example.session.db.entity.Inventory;
import org.example.session.db.entity.Product;
import org.example.session.db.repositoty.InventoryRepo;
import org.example.session.db.repositoty.ProductRepo;
import org.example.session.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;

@Service
public class InventoryService {

    private final InventoryRepo inventoryRepository;
    private final ProductRepo productRepository;

    @Autowired
    public InventoryService(InventoryRepo inventoryRepository,
                            ProductRepo productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Inventory getByProductId(Integer productId) {
        return inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));
    }

    @Transactional
    public Inventory setQuantityBySeller(UserServiceImpl principal, Integer productId, InventoryDto dto) throws AccessDeniedException {
        Product product = productRepository.findByIdAndSeller_Id(productId, principal.getUser().getId())
                .orElseThrow(() -> new AccessDeniedException("No access to product or not found"));

        return upsert(product, dto);
    }

    @Transactional
    public Inventory setQuantityByAdmin(Integer productId, InventoryDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        return upsert(product, dto);
    }

    private Inventory upsert(Product product, InventoryDto dto) {
        Inventory inv = inventoryRepository.findByProduct_Id(product.getId())
                .orElseGet(() -> {
                    Inventory i = new Inventory();
                    i.setProduct(product);
                    i.setQuantity(0);
                    i.setReserved(0);
                    Instant now = Instant.now();
                    i.setUpdatedAt(now);
                    return i;
                });

        int qty = dto.getQuantity() == null ? 0 : dto.getQuantity();
        if (qty < 0) throw new IllegalArgumentException("Quantity cannot be negative");

        inv.setQuantity(qty);
        inv.setUpdatedAt(Instant.now());
        return inventoryRepository.save(inv);
    }
}
