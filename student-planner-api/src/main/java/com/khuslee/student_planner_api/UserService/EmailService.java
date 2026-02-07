package com.khuslee.student_planner_api.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("khusleebatsuuri@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Your verification Code of Student Planner");
            message.setText("Your verification code is " + code + "  This code will expire in 10 minutes.");
            mailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Failed to send email"+ e.getMessage(), e);
        }
    }

    public String generateVerificationCode(){
        Random random = new Random();
        int code = 10000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
