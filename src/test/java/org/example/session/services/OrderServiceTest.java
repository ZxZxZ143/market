package org.example.session.services;

import jakarta.transaction.Transactional;
import org.example.session.db.entity.*;
import org.example.session.db.repositoty.*;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.OrderService;
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
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private CartItemRepo cartItemRepo;

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
        orderRepo.deleteAll();
        cartItemRepo.deleteAll();
        cartRepo.deleteAll();
        productRepo.deleteAll();
        categoryRepo.deleteAll();
        userRepo.deleteAll();
        roleRepo.deleteAll();

        Role buyerRole = new Role();
        buyerRole.setName("BUYER");
        roleRepo.save(buyerRole);

        Role sellerRole = new Role();
        sellerRole.setName("SELLER");
        roleRepo.save(sellerRole);

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        roleRepo.save(adminRole);
    }

    @Test
    @Transactional
    public void checkout_shouldThrow_whenCartNotFound() {
        User buyer = createUser("buyer1@mail.com", "Buyer1", "BUYER");
        UserServiceImpl principal = buildPrincipal(buyer);

        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.checkout(principal));
    }

    @Test
    @Transactional
    public void checkout_shouldThrow_whenCartEmpty() {
        User buyer = createUser("buyer2@mail.com", "Buyer2", "BUYER");
        UserServiceImpl principal = buildPrincipal(buyer);

        Cart cart = new Cart();
        cart.setBuyer(buyer);
        Instant now = Instant.now();
        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);
        cartRepo.save(cart);

        Assertions.assertThrows(IllegalArgumentException.class, () -> orderService.checkout(principal));
    }

    @Test
    @Transactional
    public void checkout_shouldCreateOrder_withItems_andTotal_andClearCart() {
        User buyer = createUser("buyer3@mail.com", "Buyer3", "BUYER");
        User seller = createUser("seller3@mail.com", "Seller3", "SELLER");

        Category cat = createCategory("Cat");
        categoryRepo.save(cat);

        Product p1 = createProduct(seller, cat, "P1", 100L);
        Product p2 = createProduct(seller, cat, "P2", 250L);

        Cart cart = createCartWithItem(buyer, p1, 2, 100L);
        addCartItem(cart, p2, 3, 250L);

        UserServiceImpl principal = buildPrincipal(buyer);

        Order order = orderService.checkout(principal);

        Assertions.assertNotNull(order);
        Assertions.assertNotNull(order.getId());
        Assertions.assertEquals(buyer.getId(), order.getBuyer().getId());
        Assertions.assertEquals("CREATED", order.getStatus());
        Assertions.assertNotNull(order.getCreatedAt());
        Assertions.assertNotNull(order.getUpdatedAt());
        Assertions.assertEquals(950L, order.getTotalAmount());

        Order fromDb = orderRepo.findById(order.getId())
                .orElseThrow(() -> new AssertionError("Order not found"));
        Assertions.assertNotNull(fromDb.getItems());
        Assertions.assertEquals(2, fromDb.getItems().size());

        Assertions.assertTrue(cartRepo.findById(cart.getId()).isPresent());
    }

    @Test
    @Transactional
    public void myOrders_shouldReturnPaged_sortedByCreatedAtDesc() {
        User buyer = createUser("buyer4@mail.com", "Buyer4", "BUYER");
        UserServiceImpl principal = buildPrincipal(buyer);

        Order o1 = new Order();
        o1.setBuyer(buyer);
        o1.setStatus("CREATED");
        o1.setTotalAmount(10L);
        o1.setCreatedAt(Instant.now().minusSeconds(30));
        o1.setUpdatedAt(Instant.now().minusSeconds(30));
        orderRepo.save(o1);

        Order o2 = new Order();
        o2.setBuyer(buyer);
        o2.setStatus("PAID");
        o2.setTotalAmount(20L);
        o2.setCreatedAt(Instant.now().minusSeconds(10));
        o2.setUpdatedAt(Instant.now().minusSeconds(10));
        orderRepo.save(o2);

        Order o3 = new Order();
        o3.setBuyer(buyer);
        o3.setStatus("SHIPPED");
        o3.setTotalAmount(30L);
        o3.setCreatedAt(Instant.now().minusSeconds(20));
        o3.setUpdatedAt(Instant.now().minusSeconds(20));
        orderRepo.save(o3);

        Page<Order> page = orderService.myOrders(principal, 0, 10);

        Assertions.assertNotNull(page);
        Assertions.assertEquals(3, page.getTotalElements());

        Assertions.assertEquals(o2.getId(), page.getContent().get(0).getId());
    }

    @Test
    @Transactional
    public void getMyOrder_shouldReturn_whenOwner() {
        User buyer = createUser("buyer5@mail.com", "Buyer5", "BUYER");
        UserServiceImpl principal = buildPrincipal(buyer);

        Order o = new Order();
        o.setBuyer(buyer);
        o.setStatus("CREATED");
        o.setTotalAmount(10L);
        Instant now = Instant.now();
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        o = orderRepo.save(o);

        Order found = orderService.getMyOrder(principal, o.getId());

        Assertions.assertNotNull(found);
        Assertions.assertEquals(o.getId(), found.getId());
    }

    @Test
    @Transactional
    public void getMyOrder_shouldThrowAccessDenied_whenNotOwner() {
        User buyer1 = createUser("buyer6@mail.com", "Buyer6", "BUYER");
        User buyer2 = createUser("buyer7@mail.com", "Buyer7", "BUYER");

        Order o = new Order();
        o.setBuyer(buyer1);
        o.setStatus("CREATED");
        o.setTotalAmount(10L);
        Instant now = Instant.now();
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        o = orderRepo.save(o);

        UserServiceImpl principalBuyer2 = buildPrincipal(buyer2);

        Order finalO = o;
        Assertions.assertThrows(AccessDeniedException.class, () -> orderService.getMyOrder(principalBuyer2, finalO.getId()));
    }

    @Test
    @Transactional
    public void getAnyOrder_shouldReturn() {
        User buyer = createUser("buyer8@mail.com", "Buyer8", "BUYER");

        Order o = new Order();
        o.setBuyer(buyer);
        o.setStatus("CREATED");
        o.setTotalAmount(10L);
        Instant now = Instant.now();
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        o = orderRepo.save(o);

        Order found = orderService.getAnyOrder(o.getId());

        Assertions.assertEquals(o.getId(), found.getId());
    }

    @Test
    @Transactional
    public void updateStatusByAdmin_shouldUpdateValidStatus() {
        User buyer = createUser("buyer9@mail.com", "Buyer9", "BUYER");

        Order o = new Order();
        o.setBuyer(buyer);
        o.setStatus("CREATED");
        o.setTotalAmount(10L);
        Instant now = Instant.now();
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        o = orderRepo.save(o);

        Order updated = orderService.updateStatusByAdmin(o.getId(), "paid");

        Assertions.assertEquals("PAID", updated.getStatus());
        Assertions.assertNotNull(updated.getUpdatedAt());
    }

    @Test
    @Transactional
    public void updateStatusByAdmin_shouldThrow_whenInvalidStatus() {
        User buyer = createUser("buyer10@mail.com", "Buyer10", "BUYER");

        Order o = new Order();
        o.setBuyer(buyer);
        o.setStatus("CREATED");
        o.setTotalAmount(10L);
        Instant now = Instant.now();
        o.setCreatedAt(now);
        o.setUpdatedAt(now);
        o = orderRepo.save(o);

        Order finalO = o;
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> orderService.updateStatusByAdmin(finalO.getId(), "WRONG_STATUS"));
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

    private Product createProduct(User seller, Category category, String title, Long price) {
        Product p = new Product();
        p.setSeller(seller);
        p.setCategory(category);
        p.setTitle(title);
        p.setDescription("desc");
        p.setPrice(price);
        p.setStatus("ACTIVE");
        Instant now = Instant.now();
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        return productRepo.save(p);
    }

    private Cart createCartWithItem(User buyer, Product product, int qty, long priceSnapshot) {
        Instant now = Instant.now();
        Cart cart = new Cart();
        cart.setBuyer(buyer);
        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);
        cart = cartRepo.save(cart);

        addCartItem(cart, product, qty, priceSnapshot);
        return cart;
    }

    private void addCartItem(Cart cart, Product product, int qty, long priceSnapshot) {
        CartItem ci = new CartItem();
        ci.setCart(cart);
        ci.setProduct(product);
        ci.setQuantity(qty);
        ci.setPriceSnapshot(priceSnapshot);
        ci.setCreatedAt(Instant.now());

        cart.getItems().add(ci);
        cartItemRepo.save(ci);
    }

    private UserServiceImpl buildPrincipal(User user) {
        return new UserServiceImpl(user);
    }
}
