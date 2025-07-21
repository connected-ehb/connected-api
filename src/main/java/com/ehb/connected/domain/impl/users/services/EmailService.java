package com.ehb.connected.domain.impl.users.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String email, String url) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);

        // also send to list of emails
        message.setTo(email);
        message.setCc("mertensgill@outlook.com", "gilldyr@outlook.com", "mcgaming.gm@gmail.com", "m.gill.mertens@gmail.com");

        message.setSubject("Verify your email");
        message.setText(url);
        javaMailSender.send(message);
    }
}
