package com.khuslee.student_planner_api.UserService;

import com.khuslee.student_planner_api.auth.LoginRequest;
import com.khuslee.student_planner_api.auth.RegisterRequest;
import com.khuslee.student_planner_api.user.UserEntity;
import com.khuslee.student_planner_api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public void register(RegisterRequest req) {
        String username = req.getUsername().trim();

        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }



        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setEmail(req.getEmail());
        u.setEmailVerified(false);

        String code = emailService.generateVerificationCode();
        u.setVerificationCode(code);
        u.setCodeExpiry(LocalDateTime.now().plusMinutes(10));

        users.save(u);

        emailService.sendVerificationCode(req.getEmail(), code);
    }

    public boolean verifyCode(String email, String code){
        Optional<UserEntity> userOpt = users.findByEmail(email);

        if(userOpt.isEmpty()){
            return false;
        }

        UserEntity user = userOpt.get();

        if(!code.equals(user.getVerificationCode())){
            return false;
        }

        if(user.getCodeExpiry().isBefore(LocalDateTime.now())){
            return false;
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setCodeExpiry(null);
        users.save(user);

        return true;

    }




    public void login(LoginRequest req) {

        UserEntity user = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified");
        }

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }
}
