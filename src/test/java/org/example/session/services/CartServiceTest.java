package org.example.session.services;

import jakarta.transaction.Transactional;
import org.example.session.data.dtos.request.cart.CartItemDto;
import org.example.session.db.entity.*;
import org.example.session.db.repositoty.*;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.CartService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartItemRepo cartItemRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        cartItemRepo.deleteAll();
        cartRepo.deleteAll();
        cartRepo.deleteAll();
        productRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();

        Category category = new Category();
        category.setName("test");
        category.setCreatedAt(Instant.now());

        categoryRepo.save(category);

        Role buyer = new Role();
        buyer.setName("BUYER");
        roleRepo.save(buyer);

        Role seller = new Role();
        seller.setName("SELLER");
        roleRepo.save(seller);
    }

    @Test
    @Transactional
    public void getOrCreateMyCart_shouldCreateIfMissing() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Cart cart = cartService.getOrCreateMyCart(principal);

        Assertions.assertNotNull(cart);
        Assertions.assertNotNull(cart.getId());
        Assertions.assertNotNull(cart.getBuyer());
        Assertions.assertEquals(buyer.getId(), cart.getBuyer().getId());
        Assertions.assertNotNull(cart.getCreatedAt());
        Assertions.assertNotNull(cart.getUpdatedAt());

        Optional<Cart> fromDb = cartRepo.findByBuyer_Id(buyer.getId());
        Assertions.assertTrue(fromDb.isPresent());
        Assertions.assertEquals(cart.getId(), fromDb.get().getId());
    }

    @Test
    @Transactional
    public void addItem_shouldAddNewCartItem() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Product product = createProductInDb("ACTIVE", 100L);

        CartItemDto dto = new CartItemDto();
        dto.setProductId(product.getId());
        dto.setQuantity(2);

        Cart cart = cartService.addItem(principal, dto);

        Assertions.assertNotNull(cart);
        Assertions.assertNotNull(cart.getId());

        CartItem item = cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .orElseThrow(() -> new AssertionError("CartItem not found"));

        Assertions.assertEquals(2, item.getQuantity());
        Assertions.assertEquals(product.getId(), item.getProduct().getId());
        Assertions.assertEquals(cart.getId(), item.getCart().getId());
        Assertions.assertEquals(100, item.getPriceSnapshot());
        Assertions.assertNotNull(item.getCreatedAt());
    }

    @Test
    @Transactional
    public void addItem_shouldIncreaseQuantity_ifItemAlreadyExists() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Product product = createProductInDb("ACTIVE", 200L);

        CartItemDto dto1 = new CartItemDto();
        dto1.setProductId(product.getId());
        dto1.setQuantity(2);
        Cart cart = cartService.addItem(principal, dto1);

        CartItemDto dto2 = new CartItemDto();
        dto2.setProductId(product.getId());
        dto2.setQuantity(3);
        cartService.addItem(principal, dto2);

        CartItem item = cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .orElseThrow(() -> new AssertionError("CartItem not found"));

        Assertions.assertEquals(5, item.getQuantity());
    }

    @Test
    @Transactional
    public void addItem_shouldThrow_whenProductArchived() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Product archived = createProductInDb("ARCHIVED", 100L);

        CartItemDto dto = new CartItemDto();
        dto.setProductId(archived.getId());
        dto.setQuantity(1);

        Assertions.assertThrows(IllegalArgumentException.class, () -> cartService.addItem(principal, dto));
    }

    @Test
    @Transactional
    public void setQuantity_shouldUpdateQuantity_whenQtyPositive() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Product product = createProductInDb("ACTIVE", 100L);

        CartItemDto add = new CartItemDto();
        add.setProductId(product.getId());
        add.setQuantity(2);
        Cart cart = cartService.addItem(principal, add);

        CartItemDto set = new CartItemDto();
        set.setProductId(product.getId());
        set.setQuantity(10);

        cartService.setQuantity(principal, set);

        CartItem item = cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), product.getId())
                .orElseThrow(() -> new AssertionError("CartItem not found"));

        Assertions.assertEquals(10, item.getQuantity());
    }

    @Test
    @Transactional
    public void setQuantity_shouldDeleteItem_whenQtyZeroOrLess() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Product product = createProductInDb("ACTIVE", 100L);

        CartItemDto add = new CartItemDto();
        add.setProductId(product.getId());
        add.setQuantity(2);
        Cart cart = cartService.addItem(principal, add);

        CartItemDto set = new CartItemDto();
        set.setProductId(product.getId());
        set.setQuantity(0);

        cartService.setQuantity(principal, set);

        Optional<CartItem> item = cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), product.getId());
        Assertions.assertTrue(item.isEmpty());
    }

    @Test
    @Transactional
    public void removeItem_shouldDeleteItem() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Product product = createProductInDb("ACTIVE", 100L);

        CartItemDto add = new CartItemDto();
        add.setProductId(product.getId());
        add.setQuantity(2);
        Cart cart = cartService.addItem(principal, add);

        cartService.removeItem(principal, product.getId());

        Optional<CartItem> item = cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), product.getId());
        Assertions.assertTrue(item.isEmpty());
    }

    @Test
    @Transactional
    public void clear_shouldRemoveAllItems() {
        User buyer = createBuyerInDb();
        UserServiceImpl principal = buildPrincipal(buyer);

        Product p1 = createProductInDb("ACTIVE", 10L);
        Product p2 = createProductInDb("ACTIVE", 20L);

        CartItemDto d1 = new CartItemDto();
        d1.setProductId(p1.getId());
        d1.setQuantity(2);
        Cart cart = cartService.addItem(principal, d1);

        CartItemDto d2 = new CartItemDto();
        d2.setProductId(p2.getId());
        d2.setQuantity(3);
        cartService.addItem(principal, d2);

        Assertions.assertTrue(cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), p1.getId()).isPresent());
        Assertions.assertTrue(cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), p2.getId()).isPresent());

        cartService.clear(principal);

        Assertions.assertTrue(cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), p1.getId()).isEmpty());
        Assertions.assertTrue(cartItemRepo.findByCart_IdAndProduct_Id(cart.getId(), p2.getId()).isEmpty());
    }


    private User createBuyerInDb() {
        Role buyer = roleRepo.findByName("BUYER")
                .orElseThrow(() -> new AssertionError("BUYER role missing"));

        User u = new User();
        u.setEmail("buyer_" + UUID.randomUUID() + "@mail.com");
        u.setFullName("Buyer");
        u.setStatus("ACTIVE");
        u.setRole(buyer);

        u.setPasswordHash(encoder.encode("pass"));

        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());

        return userRepo.save(u);
    }

    private User createSellerInDb() {
        Role role = roleRepo.findByName("SELLER")
                .orElseThrow(() -> new AssertionError("SELLER role missing"));

        User u = new User();
        u.setEmail("seller_" + UUID.randomUUID() + "@mail.com");
        u.setFullName("Seller");
        u.setStatus("ACTIVE");
        u.setRole(role);

        u.setPasswordHash(encoder.encode("pass"));

        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());

        return userRepo.save(u);
    }

    private Product createProductInDb(String status, Long price) {
        Category c = categoryRepo.findAll().get(0);
        User seller = createSellerInDb();

        Product p = new Product();
        p.setTitle("Product " + UUID.randomUUID());
        p.setStatus(status);
        p.setPrice(price);
        p.setCategory(c);
        p.setSeller(seller);

        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());

        return productRepo.save(p);
    }

    private UserServiceImpl buildPrincipal(User user) {
        return new UserServiceImpl(user);
    }

    private boolean hasField(Object obj, String fieldName) {
        try {
            obj.getClass().getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
