package org.example.session.controllers.auth;

import org.example.session.data.dtos.request.auth.ChangePassDto;
import org.example.session.data.dtos.request.auth.LoginDto;
import org.example.session.data.dtos.request.auth.UpdateProfileDto;
import org.example.session.data.dtos.response.UserResDto;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResDto> me(@AuthenticationPrincipal UserServiceImpl principal) {
        return ResponseEntity.ok(authService.me(principal));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResDto> updateProfile(
            @AuthenticationPrincipal UserServiceImpl principal,
            @RequestBody UpdateProfileDto req
    ) {
        return ResponseEntity.ok(authService.updateProfile(principal, req));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserServiceImpl principal,
            @RequestBody ChangePassDto req
    ) {
        authService.changePassword(principal, req);
        return ResponseEntity.noContent().build();
    }
}
