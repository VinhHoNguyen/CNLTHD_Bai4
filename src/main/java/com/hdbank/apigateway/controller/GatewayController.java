package com.hdbank.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
@Slf4j
public class GatewayController {

    private final DiscoveryClient discoveryClient;

    public GatewayController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Gateway health check");
        return ResponseEntity.ok("API Gateway is running");
    }

    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getServices() {
        log.info("Fetching registered services");
        
        Map<String, Object> response = new HashMap<>();
        List<String> services = discoveryClient.getServices();
        
        Map<String, List<ServiceInstance>> serviceDetails = new HashMap<>();
        for (String service : services) {
            serviceDetails.put(service, discoveryClient.getInstances(service));
        }
        
        response.put("total_services", services.size());
        response.put("services", serviceDetails);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        log.info("Gateway status check");
        
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("component", "API-Gateway");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(status);
    }
}
