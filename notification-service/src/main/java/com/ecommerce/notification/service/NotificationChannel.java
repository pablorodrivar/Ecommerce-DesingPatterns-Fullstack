package com.ecommerce.notification.service;

public interface NotificationChannel {
    void send(String recipient, String subject, String body);
    String getChannelName();
}