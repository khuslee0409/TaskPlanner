package com.khuslee.student_planner_api.controller;

import com.khuslee.student_planner_api.auth.*;
import com.khuslee.student_planner_api.UserService.UserService;
import com.khuslee.student_planner_api.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req){
        try {
            userService.requestPasswordReset(req.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password reset code sent to your email"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody VerifyResetCodeRequest req){
        try {
            String token = userService.verifyResetCodeAndGenerateToken(req.getEmail(), req.getCode());
            return ResponseEntity.ok(new VerifyResetCodeResponse(token, req.getEmail()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            userService.resetPasswordWithToken(req.getEmail(), req.getToken(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }








}
