package com.ferragem.avila.pdv.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ferragem.avila.pdv.dto.auth.SendEmailDto;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    
    @Async
    public void sendEmailAsync(SendEmailDto dto) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(from);
        helper.setTo(dto.to());
        helper.setSubject(dto.subject());
        helper.setText(dto.body(), true);

        javaMailSender.send(message);
    }
    
}
