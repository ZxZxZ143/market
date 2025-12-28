package org.example.session.services;

import jakarta.transaction.Transactional;
import org.example.session.data.dtos.request.product.ProductDto;
import org.example.session.data.dtos.request.product.ProductUpdateDto;
import org.example.session.db.entity.*;
import org.example.session.db.repositoty.*;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private InventoryRepo inventoryRepo;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        inventoryRepo.deleteAll();
        productRepo.deleteAll();
        categoryRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();

        Role sellerRole = new Role();
        sellerRole.setName("SELLER");
        roleRepo.save(sellerRole);

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        roleRepo.save(adminRole);
    }

    @Test
    @Transactional
    public void create_shouldCreateProductAndInventory_withDraftStatus() {
        User seller = createUser("seller1@mail.com", "Seller1", "SELLER");
        UserServiceImpl principal = buildPrincipal(seller);

        Category cat = createCategory("Electronics");

        ProductDto dto = new ProductDto();
        dto.setCategoryId(cat.getId());
        dto.setTitle("iPhone");
        dto.setDescription("desc");
        dto.setPrice(1200L);

        Product created = productService.create(principal, dto);

        Assertions.assertNotNull(created);
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals(seller.getId(), created.getSeller().getId());
        Assertions.assertEquals(cat.getId(), created.getCategory().getId());
        Assertions.assertEquals("iPhone", created.getTitle());
        Assertions.assertEquals("desc", created.getDescription());
        Assertions.assertEquals(1200L, created.getPrice());
        Assertions.assertEquals("DRAFT", created.getStatus());
        Assertions.assertNotNull(created.getCreatedAt());
        Assertions.assertNotNull(created.getUpdatedAt());

        Inventory inv = inventoryRepo.findByProduct_Id(created.getId())
                .orElseThrow(() -> new AssertionError("Inventory not created"));

        Assertions.assertNotNull(inv.getId());
        Assertions.assertEquals(created.getId(), inv.getProduct().getId());
        Assertions.assertEquals(0, inv.getQuantity());
        Assertions.assertNotNull(inv.getUpdatedAt());

        Assertions.assertNotNull(inv.getReserved());
        Assertions.assertEquals(0, inv.getReserved());
    }

    @Test
    @Transactional
    public void create_shouldThrow_whenCategoryNotFound() {
        User seller = createUser("seller2@mail.com", "Seller2", "SELLER");
        UserServiceImpl principal = buildPrincipal(seller);

        ProductDto dto = new ProductDto();
        dto.setCategoryId(999999);
        dto.setTitle("X");
        dto.setDescription("Y");
        dto.setPrice(10L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> productService.create(principal, dto));
    }

    @Test
    @Transactional
    public void getById_shouldReturnProduct() {
        User seller = createUser("seller3@mail.com", "Seller3", "SELLER");
        Category cat = createCategory("Cat");

        Product p = createProductInDb(seller, cat, "T", 100L, "ACTIVE");

        Product found = productService.getById(p.getId());

        Assertions.assertNotNull(found);
        Assertions.assertEquals(p.getId(), found.getId());
    }

    @Test
    @Transactional
    public void getById_shouldThrow_whenNotFound() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> productService.getById(123456));
    }

    @Test
    @Transactional
    public void listPublic_shouldReturnOnlyActive() {
        User seller = createUser("seller4@mail.com", "Seller4", "SELLER");
        Category cat = createCategory("Cat2");

        createProductInDb(seller, cat, "A1", 10L, "ACTIVE");
        createProductInDb(seller, cat, "D1", 10L, "DRAFT");
        createProductInDb(seller, cat, "AR1", 10L, "ARCHIVED");

        Page<Product> page = productService.listPublic(0, 50);

        Assertions.assertNotNull(page);
        Assertions.assertTrue(page.getTotalElements() >= 1);

        for (Product p : page.getContent()) {
            Assertions.assertEquals("ACTIVE", p.getStatus());
        }
    }

    @Test
    @Transactional
    public void listSellerProducts_shouldReturnOnlySellerProducts() {
        User seller1 = createUser("seller5@mail.com", "Seller5", "SELLER");
        User seller2 = createUser("seller6@mail.com", "Seller6", "SELLER");
        Category cat = createCategory("Cat3");

        createProductInDb(seller1, cat, "S1-P1", 10L, "ACTIVE");
        createProductInDb(seller1, cat, "S1-P2", 20L, "DRAFT");
        createProductInDb(seller2, cat, "S2-P1", 30L, "ACTIVE");

        Page<Product> page = productService.listSellerProducts(buildPrincipal(seller1), 0, 50);

        Assertions.assertNotNull(page);
        Assertions.assertEquals(2, page.getTotalElements());

        for (Product p : page.getContent()) {
            Assertions.assertEquals(seller1.getId(), p.getSeller().getId());
        }
    }

    @Test
    @Transactional
    public void updateBySeller_shouldUpdate_whenOwner() throws Exception {
        User seller = createUser("seller7@mail.com", "Seller7", "SELLER");
        UserServiceImpl principal = buildPrincipal(seller);

        Category cat1 = createCategory("CatA");
        Category cat2 = createCategory("CatB");

        Product p = createProductInDb(seller, cat1, "Old", 10L, "DRAFT");
        Instant oldUpdatedAt = p.getUpdatedAt();

        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setCategoryId(cat2.getId());
        dto.setTitle("New");
        dto.setDescription("New desc");
        dto.setPrice(777L);
        dto.setStatus("active");

        Product updated = productService.updateBySeller(principal, p.getId(), dto);

        Assertions.assertEquals(p.getId(), updated.getId());
        Assertions.assertEquals(cat2.getId(), updated.getCategory().getId());
        Assertions.assertEquals("New", updated.getTitle());
        Assertions.assertEquals("New desc", updated.getDescription());
        Assertions.assertEquals(777L, updated.getPrice());
        Assertions.assertEquals("ACTIVE", updated.getStatus());
        Assertions.assertNotNull(updated.getUpdatedAt());
        Assertions.assertTrue(updated.getUpdatedAt().isAfter(oldUpdatedAt) || updated.getUpdatedAt().equals(oldUpdatedAt));
    }

    @Test
    @Transactional
    public void updateBySeller_shouldThrowAccessDenied_whenNotOwner() {
        User owner = createUser("seller8@mail.com", "Seller8", "SELLER");
        User attacker = createUser("seller9@mail.com", "Seller9", "SELLER");

        Category cat1 = createCategory("CatC");
        Category cat2 = createCategory("CatD");

        Product p = createProductInDb(owner, cat1, "Old", 10L, "DRAFT");

        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setCategoryId(cat2.getId());
        dto.setTitle("Hack");
        dto.setDescription("Hack");
        dto.setPrice(1L);
        dto.setStatus("ACTIVE");

        Assertions.assertThrows(AccessDeniedException.class,
                () -> productService.updateBySeller(buildPrincipal(attacker), p.getId(), dto));
    }

    @Test
    @Transactional
    public void updateByAdmin_shouldUpdate_anyProduct() {
        User seller = createUser("seller10@mail.com", "Seller10", "SELLER");
        Category cat1 = createCategory("CatE");
        Category cat2 = createCategory("CatF");

        Product p = createProductInDb(seller, cat1, "Old", 10L, "DRAFT");

        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setCategoryId(cat2.getId());
        dto.setTitle("AdminNew");
        dto.setDescription("Admin desc");
        dto.setPrice(999L);
        dto.setStatus("ARCHIVED");

        Product updated = productService.updateByAdmin(p.getId(), dto);

        Assertions.assertEquals(cat2.getId(), updated.getCategory().getId());
        Assertions.assertEquals("AdminNew", updated.getTitle());
        Assertions.assertEquals("ARCHIVED", updated.getStatus());
        Assertions.assertEquals(999L, updated.getPrice());
    }

    @Test
    @Transactional
    public void update_shouldThrow_whenInvalidStatus() {
        User seller = createUser("seller11@mail.com", "Seller11", "SELLER");
        Category cat1 = createCategory("CatG");
        Category cat2 = createCategory("CatH");

        Product p = createProductInDb(seller, cat1, "Old", 10L, "DRAFT");

        ProductUpdateDto dto = new ProductUpdateDto();
        dto.setCategoryId(cat2.getId());
        dto.setTitle("X");
        dto.setDescription("Y");
        dto.setPrice(1L);
        dto.setStatus("WRONG");

        Assertions.assertThrows(IllegalArgumentException.class, () -> productService.updateByAdmin(p.getId(), dto));
    }

    @Test
    @Transactional
    public void archiveBySeller_shouldArchive_whenOwner() throws Exception {
        User seller = createUser("seller12@mail.com", "Seller12", "SELLER");
        Product p = createProductInDb(seller, createCategory("CatI"), "T", 10L, "ACTIVE");

        productService.archiveBySeller(buildPrincipal(seller), p.getId());

        Product fromDb = productRepo.findById(p.getId()).orElseThrow();
        Assertions.assertEquals("ARCHIVED", fromDb.getStatus());
    }

    @Test
    @Transactional
    public void archiveBySeller_shouldThrowAccessDenied_whenNotOwner() {
        User owner = createUser("seller13@mail.com", "Seller13", "SELLER");
        User other = createUser("seller14@mail.com", "Seller14", "SELLER");

        Product p = createProductInDb(owner, createCategory("CatJ"), "T", 10L, "ACTIVE");

        Assertions.assertThrows(AccessDeniedException.class,
                () -> productService.archiveBySeller(buildPrincipal(other), p.getId()));
    }

    @Test
    @Transactional
    public void archiveByAdmin_shouldArchive_anyProduct() {
        User seller = createUser("seller15@mail.com", "Seller15", "SELLER");
        Product p = createProductInDb(seller, createCategory("CatK"), "T", 10L, "ACTIVE");

        productService.archiveByAdmin(p.getId());

        Product fromDb = productRepo.findById(p.getId()).orElseThrow();
        Assertions.assertEquals("ARCHIVED", fromDb.getStatus());
    }


    private User createUser(String email, String fullName, String roleName) {
        Role role = roleRepo.findByName(roleName)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(roleName);
                    return roleRepo.save(r);
                });

        User u = new User();
        u.setEmail(email);
        u.setFullName(fullName);
        u.setPasswordHash(encoder.encode("pass"));
        u.setStatus("ACTIVE");
        u.setRole(role);

        Instant now = Instant.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);

        return userRepo.save(u);
    }

    private Category createCategory(String name) {
        Category c = new Category();
        c.setName(name + " " + UUID.randomUUID());
        c.setCreatedAt(Instant.now());
        return categoryRepo.save(c);
    }

    private Product createProductInDb(User seller, Category category, String title, Long price, String status) {
        Product p = new Product();
        p.setSeller(seller);
        p.setCategory(category);
        p.setTitle(title);
        p.setDescription("desc");
        p.setPrice(price);
        p.setStatus(status);

        Instant now = Instant.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        return productRepo.save(p);
    }

    private UserServiceImpl buildPrincipal(User user) {
        return new UserServiceImpl(user);
    }
}
