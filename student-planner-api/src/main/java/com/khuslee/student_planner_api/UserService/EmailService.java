package com.khuslee.student_planner_api.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Value("${RESEND_API_KEY:}")
    private String apiKey;

    @Value("${MAIL_FROM:}")
    private String mailFrom;

    private static final String RESEND_ENDPOINT = "https://api.resend.com/emails";

    public void sendVerificationCode(String toEmail, String code) {
        String body = """
        {
          "from": "%s",
          "to": ["%s"],
          "subject": "Student Planner Verification Code",
          "html": "<h2>Your verification code is: %s</h2><p>Expires in 10 minutes.</p>"
        }
        """.formatted(mailFrom, toEmail, code);

        sendEmail(body);
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        String body = """
        {
          "from": "%s",
          "to": ["%s"],
          "subject": "Student Planner Password Reset",
          "html": "<h2>Your password reset code is: %s</h2><p>Expires in 10 minutes.</p>"
        }
        """.formatted(mailFrom, toEmail, code);

        sendEmail(body);
    }

    private void sendEmail(String jsonBody) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY is missing");
        }
        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException("MAIL_FROM is missing (example: Student Planner <noreply@yourdomain.xyz>)");
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(RESEND_ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Accept", "application/json");

            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);

            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();

            if (status >= 400) {
                String err = readAll(conn.getErrorStream());
                throw new RuntimeException("Resend failed: HTTP " + status + " - " + err);
            } else {
                // Optional: read response for logging/debugging
                // String ok = readAll(conn.getInputStream());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    public String generateVerificationCode() {
        int code = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(code);
    }
}