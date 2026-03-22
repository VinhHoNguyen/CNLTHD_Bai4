package com.hdbank.notificationservice.service;

import com.hdbank.notificationservice.dto.NotificationEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationProcessorService {

    private final List<NotificationEvent> processedNotifications = new CopyOnWriteArrayList<>();

    public void process(NotificationEvent event) {
        log.info("Received notification for order {}: {}", event.getOrderNumber(), event.getMessage());
        processedNotifications.add(event);

        // Simulate sending confirmation email for the order.
        log.info("Simulate sending confirmation email for order {}", event.getOrderNumber());
    }

    public List<NotificationEvent> getProcessedNotifications() {
        return List.copyOf(processedNotifications);
    }

    public void clear() {
        processedNotifications.clear();
    }
}
