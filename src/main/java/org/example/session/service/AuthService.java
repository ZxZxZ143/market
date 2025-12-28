package org.example.session.service;

import jakarta.transaction.Transactional;
import org.example.session.data.dtos.request.auth.ChangePassDto;
import org.example.session.data.dtos.request.auth.RegisterDto;
import org.example.session.data.dtos.request.auth.UpdateProfileDto;
import org.example.session.data.dtos.response.UserResDto;
import org.example.session.data.mappers.UserMapper;
import org.example.session.db.entity.Role;
import org.example.session.db.entity.User;
import org.example.session.db.repositoty.RoleRepo;
import org.example.session.db.repositoty.UserRepo;
import org.example.session.security.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepo userRepository;
    private final RoleRepo roleRepository;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;

    @Autowired
    public AuthService(UserRepo userRepository, RoleRepo roleRepository, PasswordEncoder encoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResDto register(RegisterDto req, String roleName) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found in DB"));

        User user = userMapper.toEntity(req);
        user.setPasswordHash(encoder.encode(req.getPassword()));

        user.setStatus("ACTIVE");
        user.setRole(role);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional()
    public UserResDto me(UserServiceImpl principal) {
        return userMapper.toResponse(principal.getUser());
    }

    @Transactional
    public UserResDto updateProfile(UserServiceImpl principal, UpdateProfileDto req) {
        User user = userRepository.findById(principal.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.setFullName(req.getFullName());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(UserServiceImpl principal, ChangePassDto req) {
        User user = userRepository.findById(principal.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!encoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPasswordHash(encoder.encode(req.getNewPassword()));
    }
}
