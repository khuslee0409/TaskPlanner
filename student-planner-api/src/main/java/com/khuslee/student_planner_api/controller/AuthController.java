package com.khuslee.student_planner_api.controller;

import com.khuslee.student_planner_api.auth.AuthResponse;
import com.khuslee.student_planner_api.auth.LoginRequest;
import com.khuslee.student_planner_api.auth.RegisterRequest;
import com.khuslee.student_planner_api.UserService.UserService;
import com.khuslee.student_planner_api.auth.VerifyCodeRequest;
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
        System.out.println("REGISTER HIT: " + req.getUsername());
        try{
            userService.register(req);

        } catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok("Registered");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request){
        boolean verified = userService.verifyCode(request.getEmail(), request.getCode());

        if (verified) {
            return ResponseEntity.ok((
                    "Email verified successfully! You can now login."
            ));
        } else {
            return ResponseEntity.badRequest()
                    .body(("Invalid or expired code"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        userService.login(req);
        String token = jwtService.generateToken(req.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));

    }




}
