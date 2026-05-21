package com.ecommerce.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogNotificationChannel implements NotificationChannel {

    @Override
    public void send(String recipient, String subject, String body) {
        log.info("[NOTIFICATION] To: {} | Subject: {} | Body: {}", recipient, subject, body);
    }

    @Override
    public String getChannelName() {
        return "LOG";
    }
}