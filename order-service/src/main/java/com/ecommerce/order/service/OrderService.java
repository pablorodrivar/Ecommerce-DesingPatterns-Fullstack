package com.ecommerce.order.service;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(java.math.BigDecimal.ZERO)
                .build();

        request.getItems().forEach(itemRequest -> {
            OrderItem item = OrderItem.builder()
                    .productId(itemRequest.getProductId())
                    .productName(itemRequest.getProductName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .build();
            order.addItem(item);
        });

        order.recalculateTotal();
        Order saved = orderRepository.save(order);

        publishOrderCreatedEvent(saved);

        log.info("Order created: {}", saved.getOrderNumber());
        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> BusinessException.notFound("Order", id));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Order", id));

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw BusinessException.badRequest(
                "Cannot transition from " + current + " to " + next);
        }
    }

    private void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(orderMapper::toItemResponse)
                        .toList())
                .occurredAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send("order.created", order.getOrderNumber(), event);
        log.info("Event published: order.created for {}", order.getOrderNumber());
    }
}