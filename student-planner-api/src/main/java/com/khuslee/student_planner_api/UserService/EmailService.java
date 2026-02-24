package com.khuslee.student_planner_api.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY}")
    private String apiKey;

    @Value("${MAIL_FROM}")
    private String mailFrom;

    public void sendVerificationCode(String toEmail, String code) {

        try {

            URL url = new URL("https://api.resend.com/emails");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");

            conn.setDoOutput(true);

            String body = """
            {
              "from": "%s",
              "to": ["%s"],
              "subject": "Student Planner Verification Code",
              "html": "<h2>Your verification code is: %s</h2><p>Expires in 10 minutes.</p>"
            }
            """.formatted(mailFrom, toEmail, code);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            int responseCode = conn.getResponseCode();

            if (responseCode >= 400) {
                throw new RuntimeException("Resend failed with code: " + responseCode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            URL url = new URL("https://api.resend.com/emails");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String body = """
        {
          "from": "%s",
          "to": ["%s"],
          "subject": "Student Planner Password Reset",
          "html": "<h2>Your password reset code is: %s</h2><p>Expires in 10 minutes.</p>"
        }
        """.formatted(mailFrom, toEmail, code);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes());
            }

            int responseCode = conn.getResponseCode();

            if (responseCode >= 400) {
                throw new RuntimeException("Resend failed with code: " + responseCode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage(), e);
        }
    }

    public String generateVerificationCode() {
        int code = 100000 + (int)(Math.random() * 900000);
        return String.valueOf(code);
    }
}