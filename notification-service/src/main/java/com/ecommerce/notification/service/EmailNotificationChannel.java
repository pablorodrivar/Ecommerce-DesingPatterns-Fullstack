package com.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Override
    public void send(String recipient, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {}: {}", recipient, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", recipient, e.getMessage());
        }
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }
}