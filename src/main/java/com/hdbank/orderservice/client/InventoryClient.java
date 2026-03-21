package com.hdbank.orderservice.client;

import com.hdbank.orderservice.dto.InventoryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class InventoryClient {

    private final RestTemplate restTemplate;

    public InventoryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackCheckInventory")
    @Retry(name = "inventoryService")
    @TimeLimiter(name = "inventoryService")
    public CompletableFuture<boolean[]> checkInventory(List<String> skuCodes) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Checking inventory for skuCodes: {}", skuCodes);
            try {
                InventoryResponse[] response = restTemplate.getForObject(
                        "http://localhost:8081/api/inventory?skuCode=" + String.join(",", skuCodes),
                        InventoryResponse[].class
                );

                if (response != null) {
                    boolean[] allInStock = new boolean[1];
                    allInStock[0] = Arrays.stream(response)
                            .allMatch(InventoryResponse::isInStock);
                    return allInStock;
                }
                return new boolean[]{false};
            } catch (Exception e) {
                log.error("Error checking inventory: {}", e.getMessage());
                throw new RuntimeException("Failed to check inventory", e);
            }
        });
    }

    public CompletableFuture<boolean[]> fallbackCheckInventory(List<String> skuCodes, Exception e) {
        log.warn("Fallback triggered for inventory check due to: {}", e.getMessage());
        // Return true assuming all items are in stock (optimistic approach)
        // or false to reject all orders (pessimistic approach)
        return CompletableFuture.completedFuture(new boolean[]{true});
    }
}
