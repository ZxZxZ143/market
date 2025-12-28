package org.example.session.services;

import org.example.session.data.dtos.request.auth.ChangePassDto;
import org.example.session.data.dtos.request.auth.RegisterDto;
import org.example.session.data.dtos.request.auth.UpdateProfileDto;
import org.example.session.data.dtos.response.UserResDto;
import org.example.session.db.entity.Role;
import org.example.session.db.entity.User;
import org.example.session.db.repositoty.RoleRepo;
import org.example.session.db.repositoty.UserRepo;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        userRepo.deleteAll();
        roleRepo.deleteAll();

        Role role = new Role();
        role.setName("BUYER");

        Role role2 = new Role();
        role2.setName("SELLER");

        roleRepo.save(role2);
        roleRepo.save(role);
    }

    @Test
    public void registerBuyer_shouldCreateUser_withEncodedPassword_andRole() {
        RegisterDto req = new RegisterDto();
        req.setEmail(uniqueEmail());
        req.setPassword("pass0");
        req.setFullName("Test User");

        UserResDto res = authService.register(req, "BUYER");

        Assertions.assertNotNull(res);
        Assertions.assertNotNull(res.getId());
        Assertions.assertNotNull(res.getEmail());
        Assertions.assertEquals(req.getEmail(), res.getEmail());

        User fromDb = userRepo.findById(res.getId())
                .orElseThrow(() -> new AssertionError("User not found in DB"));

        Assertions.assertEquals("ACTIVE", fromDb.getStatus());
        Assertions.assertNotNull(fromDb.getRole());
        Assertions.assertEquals("BUYER", fromDb.getRole().getName());

        Assertions.assertNotNull(fromDb.getPasswordHash());
        Assertions.assertNotEquals(req.getPassword(), fromDb.getPasswordHash());
        Assertions.assertTrue(encoder.matches(req.getPassword(), fromDb.getPasswordHash()));
    }

    @Test
    public void register_shouldThrow_whenEmailAlreadyExists() {
        String email = uniqueEmail();

        RegisterDto req1 = new RegisterDto();
        req1.setEmail(email);
        req1.setPassword("pass1");
        req1.setFullName("First");

        authService.register(req1, "SELLER");

        RegisterDto req2 = new RegisterDto();
        req2.setEmail(email);
        req2.setPassword("pass2");
        req2.setFullName("Second");

        Assertions.assertThrows(IllegalArgumentException.class, () -> authService.register(req2, "SELLER"));
    }

    @Test
    public void registerBuyer_shouldThrow_whenRoleMissing() {
        roleRepo.deleteAll();

        RegisterDto req = new RegisterDto();
        req.setEmail(uniqueEmail());
        req.setPassword("pass3");
        req.setFullName("No Role");

        Assertions.assertThrows(IllegalStateException.class, () -> authService.register(req, "no role"));
    }

    @Test
    public void me_shouldReturnCurrentUser() {
        User user = createUserInDb("Me User", "me@mail.com", "secret");

        UserServiceImpl principal = buildPrincipal(user);

        UserResDto me = authService.me(principal);

        Assertions.assertNotNull(me);
        Assertions.assertEquals(user.getId(), me.getId());
        Assertions.assertEquals(user.getEmail(), me.getEmail());
    }

    @Test
    public void updateProfile_shouldUpdateFullName() {
        User user = createUserInDb("Old Name", "profile@mail.com", "secret");
        UserServiceImpl principal = buildPrincipal(user);

        UpdateProfileDto dto = new UpdateProfileDto();
        dto.setFullName("New Name");

        UserResDto updated = authService.updateProfile(principal, dto);

        Assertions.assertNotNull(updated);
        Assertions.assertEquals(user.getId(), updated.getId());
        Assertions.assertEquals("New Name", updated.getFullName());

        User fromDb = userRepo.findById(user.getId())
                .orElseThrow(() -> new AssertionError("User not found in DB"));

        Assertions.assertEquals("New Name", fromDb.getFullName());
    }

    @Test
    public void changePassword_shouldUpdateHash_whenOldPasswordCorrect() {
        User user = createUserInDb("Pass User", "pass@mail.com", "oldPass");
        UserServiceImpl principal = buildPrincipal(user);

        ChangePassDto dto = new ChangePassDto();
        dto.setOldPassword("oldPass");
        dto.setNewPassword("newPass");

        authService.changePassword(principal, dto);

        User fromDb = userRepo.findById(user.getId())
                .orElseThrow(() -> new AssertionError("User not found in DB"));

        Assertions.assertTrue(encoder.matches("newPass", fromDb.getPasswordHash()));
        Assertions.assertFalse(encoder.matches("oldPass", fromDb.getPasswordHash()));
    }

    @Test
    public void changePassword_shouldThrow_whenOldPasswordWrong() {
        User user = createUserInDb("Pass User", "pass2@mail.com", "oldPass");
        UserServiceImpl principal = buildPrincipal(user);

        ChangePassDto dto = new ChangePassDto();
        dto.setOldPassword("WRONG");
        dto.setNewPassword("newPass");

        Assertions.assertThrows(IllegalArgumentException.class, () -> authService.changePassword(principal, dto));
    }

    private String uniqueEmail() {
        return "user_" + UUID.randomUUID() + "@mail.com";
    }

    private User createUserInDb(String fullName, String email, String rawPassword) {
        Role buyer = roleRepo.findByName("BUYER")
                .orElseThrow(() -> new AssertionError("BUYER role missing in DB"));

        User u = new User();
        u.setEmail(email);
        u.setFullName(fullName);
        u.setStatus("ACTIVE");
        u.setRole(buyer);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        u.setPasswordHash(encoder.encode(rawPassword));

        return userRepo.save(u);
    }

    private UserServiceImpl buildPrincipal(User user) {
        return new UserServiceImpl(user);
    }
}
