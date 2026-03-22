package com.hdbank.notificationservice.listener;

import com.hdbank.notificationservice.dto.NotificationEvent;
import com.hdbank.notificationservice.service.NotificationProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationProcessorService notificationProcessorService;

    @KafkaListener(topics = "${app.kafka.notification-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(NotificationEvent event) {
        log.debug("Kafka message arrived: {}", event);
        notificationProcessorService.process(event);
    }
}
