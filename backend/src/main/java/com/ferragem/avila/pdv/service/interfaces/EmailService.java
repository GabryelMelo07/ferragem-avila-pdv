package com.ferragem.avila.pdv.service.interfaces;

import com.ferragem.avila.pdv.dto.SendEmailDto;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmailAsync(SendEmailDto dto) throws MessagingException;
}
