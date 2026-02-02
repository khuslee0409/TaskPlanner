package com.khuslee.student_planner_api.auth;

public class AuthResponse {
    private String token;

    public AuthResponse(String token) { this.token = token; }
    public String getToken() { return token; }
}
