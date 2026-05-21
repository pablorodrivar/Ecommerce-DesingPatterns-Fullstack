package com.ecommerce.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final Map<String, NotificationChannel> channels;

    public NotificationService(List<NotificationChannel> channelList) {
        this.channels = channelList.stream()
                .collect(Collectors.toMap(
                        NotificationChannel::getChannelName,
                        Function.identity()
                ));
        log.info("Notification channels registered: {}", this.channels.keySet());
    }

    public void send(String channelName, String recipient, String subject, String body) {
        NotificationChannel channel = channels.get(channelName);
        if (channel == null) {
            log.warn("Channel not found: {}, falling back to LOG", channelName);
            channels.get("LOG").send(recipient, subject, body);
            return;
        }
        channel.send(recipient, subject, body);
    }

    public void sendToAll(String recipient, String subject, String body) {
        channels.values().forEach(channel ->
                channel.send(recipient, subject, body));
    }
}