package com.hdbank.notificationservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.hdbank.notificationservice.dto.NotificationEvent;
import com.hdbank.notificationservice.service.NotificationProcessorService;
import org.junit.jupiter.api.Test;

class NotificationProcessorServiceTest {

    private final NotificationProcessorService notificationProcessorService = new NotificationProcessorService();

    @Test
    void shouldProcessMessageSuccessfully() {
        NotificationEvent event = NotificationEvent.builder()
                .orderNumber("ORD123")
                .message("Order Placed Successfully")
                .build();

        notificationProcessorService.process(event);

        assertThat(notificationProcessorService.getProcessedNotifications())
                .hasSize(1)
                .first()
                .extracting(NotificationEvent::getOrderNumber, NotificationEvent::getMessage)
                .containsExactly("ORD123", "Order Placed Successfully");
    }
}
