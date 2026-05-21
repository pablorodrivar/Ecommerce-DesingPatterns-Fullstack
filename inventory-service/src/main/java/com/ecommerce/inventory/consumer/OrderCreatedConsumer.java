package com.ecommerce.inventory.consumer;

import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(
        topics = "order.created",
        groupId = "inventory-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreated(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_KEY) String orderNumber) {

        log.info("Received order.created event for order: {}", orderNumber);

        try {
            List<Map<String, Object>> items =
                (List<Map<String, Object>>) event.get("items");

            if (items == null || items.isEmpty()) {
                log.warn("Order {} has no items, skipping stock update", orderNumber);
                return;
            }

            items.forEach(item -> {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();
                inventoryService.decreaseStock(productId, quantity);
            });

            log.info("Stock updated successfully for order: {}", orderNumber);

        } catch (Exception e) {
            log.error("Error processing order.created for {}: {}", orderNumber, e.getMessage(), e);
        }
    }
}