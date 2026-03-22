package com.hdbank.notificationservice.controller;

import com.hdbank.notificationservice.dto.NotificationEvent;
import com.hdbank.notificationservice.service.NotificationProcessorService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationProcessorService notificationProcessorService;

    @GetMapping
    public List<NotificationEvent> getProcessedNotifications() {
        return notificationProcessorService.getProcessedNotifications();
    }
}
