package com.khuslee.student_planner_api.auth;

public class VerifyResetCodeResponse {
    private String resetToken;
    private String email;

    public VerifyResetCodeResponse(String resetToken, String email) {
        this.resetToken = resetToken;
        this.email = email;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
