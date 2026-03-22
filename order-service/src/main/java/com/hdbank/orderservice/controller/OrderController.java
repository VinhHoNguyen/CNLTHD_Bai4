package com.hdbank.orderservice.controller;

import com.hdbank.orderservice.dto.OrderRequest;
import com.hdbank.orderservice.dto.OrderResponse;
import com.hdbank.orderservice.model.Order;
import com.hdbank.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        log.info("Received order placement request with {} items", 
                 orderRequest.getOrderLineItemsDtoList().size());
        
        try {
            OrderResponse response = orderService.placeOrder(orderRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error placing order: {}", e.getMessage());
            OrderResponse errorResponse = new OrderResponse(
                    null,
                    null,
                    "Error: " + e.getMessage(),
                    "FAILED"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("Fetching order with orderNumber: {}", orderNumber);
        
        try {
            Order order = orderService.getOrderByOrderNumber(orderNumber);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("Fetching order with id: {}", id);
        
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Order not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running");
    }
}
