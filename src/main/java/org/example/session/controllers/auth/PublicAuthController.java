package org.example.session.controllers.auth;

import org.example.session.data.dtos.request.auth.RegisterDto;
import org.example.session.data.dtos.response.UserResDto;
import org.example.session.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicAuthController {

    private final AuthService authService;

    @Autowired
    public PublicAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/buyer/register")
    public ResponseEntity<UserResDto> registerBuyer(@RequestBody RegisterDto req) {
        return ResponseEntity.ok(authService.register(req, "BUYER"));
    }

    @PostMapping("/seller/register")
    public ResponseEntity<UserResDto> registerSeller(@RequestBody RegisterDto req) {
        return ResponseEntity.ok(authService.register(req, "SELLER"));
    }
}

