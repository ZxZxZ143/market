package org.example.session.services;

import jakarta.transaction.Transactional;
import org.example.session.data.dtos.request.InventoryDto;
import org.example.session.db.entity.*;
import org.example.session.db.repositoty.*;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepo inventoryRepo;
    @Autowired
    private ProductRepo productRepo;
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
    public void getByProductId_shouldReturnInventory() {
        User seller = createUser("seller@mail.com", "Seller", "SELLER");
        Category cat = createCategory("Electronics");
        Product product = createProduct(seller, cat);

        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setQuantity(10);
        inv.setReserved(0);
        inv.setUpdatedAt(Instant.now());
        inv = inventoryRepo.save(inv);

        Inventory found = inventoryService.getByProductId(product.getId());

        Assertions.assertNotNull(found);
        Assertions.assertEquals(inv.getId(), found.getId());
        Assertions.assertEquals(product.getId(), found.getProduct().getId());
        Assertions.assertEquals(10, found.getQuantity());
    }

    @Test
    @Transactional
    public void getByProductId_shouldThrow_whenInventoryMissing() {
        User seller = createUser("seller2@mail.com", "Seller2", "SELLER");
        Category cat = createCategory("Books");
        Product product = createProduct(seller, cat);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> inventoryService.getByProductId(product.getId()));
    }

    @Test
    @Transactional
    public void setQuantityByAdmin_shouldCreateInventory_ifNotExists() {
        User seller = createUser("seller3@mail.com", "Seller3", "SELLER");
        Category cat = createCategory("Cat");
        Product product = createProduct(seller, cat);

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(25);

        Inventory saved = inventoryService.setQuantityByAdmin(product.getId(), dto);

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals(product.getId(), saved.getProduct().getId());
        Assertions.assertEquals(25, saved.getQuantity());
        Assertions.assertNotNull(saved.getUpdatedAt());

        Inventory fromDb = inventoryRepo.findById(saved.getId())
                .orElseThrow(() -> new AssertionError("Inventory not found"));
        Assertions.assertNotNull(fromDb.getReserved());
        Assertions.assertEquals(0, fromDb.getReserved());
    }

    @Test
    @Transactional
    public void setQuantityByAdmin_shouldUpdateExistingInventory() {
        User seller = createUser("seller4@mail.com", "Seller4", "SELLER");
        Category cat = createCategory("Cat2");
        Product product = createProduct(seller, cat);

        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setQuantity(5);
        inv.setReserved(0);
        inv.setUpdatedAt(Instant.now());
        inv = inventoryRepo.save(inv);

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(99);

        Inventory updated = inventoryService.setQuantityByAdmin(product.getId(), dto);

        Assertions.assertEquals(inv.getId(), updated.getId());
        Assertions.assertEquals(99, updated.getQuantity());
    }

    @Test
    @Transactional
    public void setQuantityByAdmin_shouldThrow_whenQtyNegative() {
        User seller = createUser("seller5@mail.com", "Seller5", "SELLER");
        Category cat = createCategory("Cat3");
        Product product = createProduct(seller, cat);

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(-1);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> inventoryService.setQuantityByAdmin(product.getId(), dto));
    }

    @Test
    @Transactional
    public void setQuantityBySeller_shouldAllowOwnProduct() throws Exception {
        User seller = createUser("seller6@mail.com", "Seller6", "SELLER");
        Category cat = createCategory("Cat4");
        Product product = createProduct(seller, cat);

        UserServiceImpl principal = buildPrincipal(seller);

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(7);

        Inventory inv = inventoryService.setQuantityBySeller(principal, product.getId(), dto);

        Assertions.assertNotNull(inv.getId());
        Assertions.assertEquals(7, inv.getQuantity());
        Assertions.assertEquals(product.getId(), inv.getProduct().getId());
    }

    @Test
    @Transactional
    public void setQuantityBySeller_shouldThrowAccessDenied_whenNotOwner() {
        User seller1 = createUser("seller7@mail.com", "Seller7", "SELLER");
        User seller2 = createUser("seller8@mail.com", "Seller8", "SELLER");
        Category cat = createCategory("Cat5");

        Product productOfSeller1 = createProduct(seller1, cat);

        UserServiceImpl principalSeller2 = buildPrincipal(seller2);

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(10);

        Assertions.assertThrows(AccessDeniedException.class,
                () -> inventoryService.setQuantityBySeller(principalSeller2, productOfSeller1.getId(), dto));
    }

    @Test
    @Transactional
    public void setQuantityBySeller_shouldThrowAccessDenied_whenProductNotFound() {
        User seller = createUser("seller9@mail.com", "Seller9", "SELLER");
        UserServiceImpl principal = buildPrincipal(seller);

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(10);

        Assertions.assertThrows(AccessDeniedException.class,
                () -> inventoryService.setQuantityBySeller(principal, 999999, dto));
    }

    @Test
    @Transactional
    public void setQuantity_null_shouldSetZero() {
        User seller = createUser("seller10@mail.com", "Seller10", "SELLER");
        Category cat = createCategory("Cat6");
        Product product = createProduct(seller, cat);

        InventoryDto dto = new InventoryDto();
        dto.setQuantity(null);

        Inventory inv = inventoryService.setQuantityByAdmin(product.getId(), dto);

        Assertions.assertEquals(0, inv.getQuantity());
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

    private Product createProduct(User seller, Category category) {
        Product p = new Product();
        p.setSeller(seller);
        p.setCategory(category);
        p.setTitle("Title " + UUID.randomUUID());
        p.setDescription("desc");
        p.setPrice(1000L);
        p.setStatus("ACTIVE");

        Instant now = Instant.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        return productRepo.save(p);
    }

    private UserServiceImpl buildPrincipal(User user) {
        return new UserServiceImpl(user);
    }
}
