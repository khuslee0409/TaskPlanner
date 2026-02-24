package com.khuslee.student_planner_api.UserService;

import com.khuslee.student_planner_api.auth.LoginRequest;
import com.khuslee.student_planner_api.auth.RegisterRequest;
import com.khuslee.student_planner_api.user.UserEntity;
import com.khuslee.student_planner_api.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Autowired
    private EmailService emailService;

    public UserService(UserRepository users, PasswordEncoder encoder, EmailService theEmailService) {
        this.users = users;
        this.encoder = encoder;
        this.emailService = theEmailService;
    }



    @Transactional
    public void register(RegisterRequest req) {
        String username = req.getUsername().trim();
        String theEmail = req.getEmail().trim().toLowerCase();


        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (users.existsByEmailIgnoreCase(theEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }



        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setEmail(theEmail);
        u.setEmailVerified(false);

        String code = emailService.generateVerificationCode();
        u.setVerificationCode(code);
        u.setCodeExpiry(LocalDateTime.now().plusMinutes(10));

        users.save(u);

        // Send email only AFTER the DB transaction commits (so no rollback)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    emailService.sendVerificationCode(theEmail, code);
                } catch (Exception ex) {
                    ex.printStackTrace(); // log error; user remains saved
                }
            }
        });
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        String theEmail = (email == null) ? "" : email.trim().toLowerCase();
        String theCode  = (code == null)  ? "" : code.trim();

        Optional<UserEntity> userOpt = users.findByEmailIgnoreCase(theEmail);

        if (userOpt.isEmpty()) {
            return false;
        }

        UserEntity user = userOpt.get();

        if (user.getVerificationCode() == null) {
            return false;
        }

        if (!theCode.equals(user.getVerificationCode().trim())) {
            return false;
        }

        if (user.getCodeExpiry() == null || user.getCodeExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setCodeExpiry(null);
        users.save(user);

        return true;
    }

    @Transactional
    public void requestPasswordReset(String email) {

        Optional<UserEntity> userOpt = users.findByEmail(email.trim().toLowerCase());

        // SECURITY: don't reveal whether email exists
        if (userOpt.isEmpty()) {
            return;
        }

        UserEntity user = userOpt.get();

        String resetCode = emailService.generateVerificationCode();
        user.setPasswordResetCode(resetCode);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(10));

        users.save(user);

        // send email AFTER commit (prevents rollback if email fails)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    emailService.sendPasswordResetCode(user.getEmail(), resetCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Transactional
    public String verifyResetCodeAndGenerateToken(String email, String code) {

        email = email.trim().toLowerCase();
        code  = code.trim();

        UserEntity user = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPasswordResetCode() == null) {
            throw new IllegalArgumentException("No reset request found");
        }

        if (!code.equals(user.getPasswordResetCode())) {
            throw new IllegalArgumentException("Invalid reset code");
        }

        if (user.getResetCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset code has expired");
        }

        // Generate a one-time reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5)); // 5 min to enter new password

        // Clear the code since it's been used
        user.setPasswordResetCode(null);
        user.setResetCodeExpiry(null);

        users.save(user);

        return resetToken;
    }

    @Transactional
    public void resetPasswordWithToken(String email, String token, String newPassword) {
        email = email.trim().toLowerCase();
        token = token.trim();

        UserEntity user = users.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getPasswordResetToken() == null) {
            throw new IllegalArgumentException("Invalid or expired reset session");
        }

        if (!token.equals(user.getPasswordResetToken())) {
            throw new IllegalArgumentException("Invalid reset token");
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset session has expired");
        }

        // Update password
        user.setPasswordHash(encoder.encode(newPassword));

        // Clear all reset-related fields
        user.setPasswordResetToken(null);
        user.setResetTokenExpiry(null);

        users.save(user);
    }

    public void login(LoginRequest req) {
        UserEntity user = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        System.out.println("User found: " + user.getUsername());
        System.out.println("Email verified: " + user.isEmailVerified());
        System.out.println("Verification code: " + user.getVerificationCode());
        System.out.println("Password matches: " + encoder.matches(req.getPassword(), user.getPasswordHash()));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Only block login if user is NEWLY registered and hasn't verified yet
        // Existing users (those without verification code data) can login freely
        if (!user.isEmailVerified() && user.getVerificationCode() != null) {
            throw new IllegalArgumentException("Please verify your email before logging in");
        }
    }
}