package com.khuslee.student_planner_api.controller;

import com.khuslee.student_planner_api.auth.AuthResponse;
import com.khuslee.student_planner_api.auth.LoginRequest;
import com.khuslee.student_planner_api.auth.RegisterRequest;
import com.khuslee.student_planner_api.UserService.UserService;
import com.khuslee.student_planner_api.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private JwtService jwtService;

    private final UserService userService;

    public AuthController(UserService userService, JwtService theJwtService) {
        this.userService = userService;
        this.jwtService = theJwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        userService.register(req);
        return ResponseEntity.ok("Registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        userService.login(req);
        String token = jwtService.generateToken(req.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));

    }

    @GetMapping("/api/me")
    public String me(Authentication auth) {
        return auth.getName();
    }


}
