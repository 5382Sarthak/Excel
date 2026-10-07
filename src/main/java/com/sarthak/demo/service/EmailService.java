package com.sarthak.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import this
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // This pulls the value of 'MAIL' from your .env -> application.properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtp(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail); // Use the variable here
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);

        mailSender.send(message);
    }
}