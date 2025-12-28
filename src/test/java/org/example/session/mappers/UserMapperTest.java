package org.example.session.mappers;

import org.example.session.data.dtos.request.auth.LoginDto;
import org.example.session.data.dtos.request.auth.RegisterDto;
import org.example.session.data.dtos.response.UserResDto;
import org.example.session.data.mappers.UserMapper;
import org.example.session.db.entity.Role;
import org.example.session.db.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponse_shouldMapRoleName() {
        Role role = new Role();
        role.setName("BUYER");

        User user = new User();
        user.setId(1);
        user.setEmail("user@mail.com");
        user.setFullName("John Doe");
        user.setStatus("ACTIVE");
        user.setRole(role);
        user.setCreatedAt(Instant.now());

        UserResDto dto = mapper.toResponse(user);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1, dto.getId());
        Assertions.assertEquals("user@mail.com", dto.getEmail());
        Assertions.assertEquals("John Doe", dto.getFullName());
        Assertions.assertEquals("ACTIVE", dto.getStatus());
        Assertions.assertEquals("BUYER", dto.getRole());
    }

    @Test
    void toEntity_fromRegisterDto_shouldIgnoreRestrictedFields() {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("new@mail.com");
        dto.setFullName("New User");
        dto.setPassword("123456");

        User user = mapper.toEntity(dto);

        Assertions.assertNotNull(user);
        Assertions.assertEquals("new@mail.com", user.getEmail());
        Assertions.assertEquals("New User", user.getFullName());

        Assertions.assertNull(user.getId());
        Assertions.assertNull(user.getPasswordHash());
        Assertions.assertNull(user.getStatus());
        Assertions.assertNull(user.getRole());
        Assertions.assertNull(user.getCreatedAt());
        Assertions.assertNull(user.getUpdatedAt());
    }

    @Test
    void toEntity_fromLoginDto_shouldMapOnlyEmail() {
        LoginDto dto = new LoginDto();
        dto.setEmail("login@mail.com");
        dto.setPassword("pass");

        User user = mapper.toEntity(dto);

        Assertions.assertNotNull(user);
        Assertions.assertEquals("login@mail.com", user.getEmail());

        Assertions.assertNull(user.getId());
        Assertions.assertNull(user.getFullName());
        Assertions.assertNull(user.getPasswordHash());
        Assertions.assertNull(user.getRole());
    }
}
