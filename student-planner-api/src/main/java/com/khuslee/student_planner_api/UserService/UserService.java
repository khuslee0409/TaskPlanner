package com.khuslee.student_planner_api.UserService;

import com.khuslee.student_planner_api.auth.LoginRequest;
import com.khuslee.student_planner_api.auth.RegisterRequest;
import com.khuslee.student_planner_api.user.UserEntity;
import com.khuslee.student_planner_api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public void register(RegisterRequest req) {
        String username = req.getUsername().trim();

        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(req.getPassword()));

        users.save(u);
    }

    public void login(LoginRequest req) {

        UserEntity user = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }
}
