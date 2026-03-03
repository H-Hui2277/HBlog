package com.hblog.controller;

import com.hblog.dto.LoginRequest;
import com.hblog.dto.LoginResponse;
import com.hblog.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (authService.validateCredentials(request.getUsername(), request.getPassword())) {
            String token = authService.generateToken(request.getUsername());
            return ResponseEntity.ok(LoginResponse.success("Login successful", token));
        }
        return ResponseEntity.ok(LoginResponse.failure("Invalid credentials"));
    }
}
