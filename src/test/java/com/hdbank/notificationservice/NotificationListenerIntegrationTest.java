package com.hdbank.notificationservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.hdbank.notificationservice.dto.NotificationEvent;
import com.hdbank.notificationservice.service.NotificationProcessorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"notificationTopic"})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer"
})
class NotificationListenerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Autowired
    private NotificationProcessorService notificationProcessorService;

    @BeforeEach
    void setup() {
        notificationProcessorService.clear();
    }

    @Test
    void shouldConsumeMessageFromKafkaSuccessfully() {
        NotificationEvent event = NotificationEvent.builder()
                .orderNumber("ORD123")
                .message("Order Placed Successfully")
                .build();

        kafkaTemplate.send("notificationTopic", event);

        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(notificationProcessorService.getProcessedNotifications()).hasSize(1));

        NotificationEvent processed = notificationProcessorService.getProcessedNotifications().get(0);
        assertThat(processed.getOrderNumber()).isEqualTo("ORD123");
        assertThat(processed.getMessage()).isEqualTo("Order Placed Successfully");
    }
}
