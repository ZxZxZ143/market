package org.example.session.service;

import jakarta.transaction.Transactional;
import org.example.session.data.dtos.request.product.ProductDto;
import org.example.session.data.dtos.request.product.ProductUpdateDto;
import org.example.session.db.entity.Category;
import org.example.session.db.entity.Inventory;
import org.example.session.db.entity.Product;
import org.example.session.db.entity.User;
import org.example.session.db.repositoty.CategoryRepo;
import org.example.session.db.repositoty.InventoryRepo;
import org.example.session.db.repositoty.ProductRepo;
import org.example.session.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProductService {

    private final ProductRepo productRepository;
    private final InventoryRepo inventoryRepository;
    private final CategoryRepo categoryRepository;

    @Autowired
    public ProductService(ProductRepo productRepository, CategoryRepo categoryRepository, InventoryRepo inventoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public Product create(UserServiceImpl principal, ProductDto dto) {
        User seller = principal.getUser();

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + dto.getCategoryId()));

        Product p = new Product();
        p.setSeller(seller);
        p.setCategory(category);
        p.setTitle(dto.getTitle());
        p.setDescription(dto.getDescription());
        p.setPrice(Long.valueOf(dto.getPrice()));

        p.setStatus("DRAFT");

        Instant now = Instant.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        Product savedProduct = productRepository.save(p);

        Inventory inv = new Inventory();
        inv.setProduct(savedProduct);
        inv.setQuantity(0);
        inv.setReserved(0);
        inv.setUpdatedAt(Instant.now());
        inventoryRepository.save(inv);

        return savedProduct;
    }

    @Transactional()
    public Product getById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Transactional()
    public Page<Product> listPublic(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findAllByStatus("ACTIVE", pageable);
    }

    @Transactional()
    public Page<Product> listSellerProducts(UserServiceImpl principal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findAllBySeller_Id(principal.getUser().getId(), pageable);
    }

    @Transactional
    public Product updateBySeller(UserServiceImpl principal, Integer productId, ProductUpdateDto dto) throws AccessDeniedException {
        Product product = productRepository.findByIdAndSeller_Id(productId, principal.getUser().getId())
                .orElseThrow(() -> new AccessDeniedException("No access to product or not found"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + dto.getCategoryId()));

        applyUpdate(product, category, dto);
        product.setUpdatedAt(Instant.now());
        return product;
    }

    @Transactional
    public Product updateByAdmin(Integer productId, ProductUpdateDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + dto.getCategoryId()));

        applyUpdate(product, category, dto);
        product.setUpdatedAt(Instant.now());
        return product;
    }

    @Transactional
    public void archiveBySeller(UserServiceImpl principal, Integer productId) throws AccessDeniedException {
        Product product = productRepository.findByIdAndSeller_Id(productId, principal.getUser().getId())
                .orElseThrow(() -> new AccessDeniedException("No access to product or not found"));

        product.setStatus("ARCHIVED");
        product.setUpdatedAt(Instant.now());
    }

    @Transactional
    public void archiveByAdmin(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        product.setStatus("ARCHIVED");
        product.setUpdatedAt(Instant.now());
    }

    private void applyUpdate(Product product, Category category, ProductUpdateDto dto) {
        product.setCategory(category);
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        String st = dto.getStatus() == null ? "DRAFT" : dto.getStatus().toUpperCase();
        if (!st.equals("DRAFT") && !st.equals("ACTIVE") && !st.equals("ARCHIVED")) {
            throw new IllegalArgumentException("Invalid status: " + dto.getStatus());
        }
        product.setStatus(st);
    }
}
