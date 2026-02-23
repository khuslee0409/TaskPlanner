package com.khuslee.student_planner_api.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Async
    public void sendVerificationCode(String toEmail, String code){
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("khusleebatsuuri@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Your verification Code of Student Planner");
            message.setText("Your verification code is " + code + ". This code will expire in 10 minutes.");
            mailSender.send(message);
        }catch (Exception e){
            throw new RuntimeException("Failed to send email"+ e.getMessage(), e);
        }
    }
    @Async
    public void sendPasswordResetCode(String to, String code){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Code");
        message.setText("Your password reset code is: " + code + ". This code will expire in 10 minutes" );
        mailSender.send(message);
    }

    public String generateVerificationCode(){
        Random random = new Random();
        int code = 10000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
