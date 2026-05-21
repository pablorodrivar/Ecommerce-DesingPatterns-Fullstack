package com.ecommerce.notification.consumer;

import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = "order.created",
        groupId = "notification-service"
    )
    public void handleOrderCreated(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String orderNumber) {

        log.info("Notification service received order.created: {}", orderNumber);

        Long customerId = ((Number) event.get("customerId")).longValue();
        BigDecimal total = new BigDecimal(event.get("totalAmount").toString());

        String recipient = "customer-" + customerId + "@ecommerce.com";
        String subject = "Order confirmed: " + orderNumber;
        String body = String.format(
            "Hello! Your order %s has been confirmed.\nTotal: $%s\nThank you for your purchase!",
            orderNumber, total
        );

        notificationService.send("LOG", recipient, subject, body);
    }
}