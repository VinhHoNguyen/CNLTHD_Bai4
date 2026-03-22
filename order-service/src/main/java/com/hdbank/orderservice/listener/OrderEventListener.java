package com.hdbank.orderservice.listener;

import com.hdbank.orderservice.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderEventListener {

    @KafkaListener(
            topics = "notificationTopic",
            groupId = "order-service-group"
    )
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order placed event: orderNumber={}, message={}", 
                 event.getOrderNumber(), event.getMessage());
        
        // Here you can implement notification logic
        // For example: send email, SMS, push notification, etc.
        
        log.info("Order notification processed for order: {}", event.getOrderNumber());
    }
}
