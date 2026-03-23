package com.hdbank.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Product Service Route
                .route("product-service", r -> r
                        .path("/api/product/**")
                        .uri("lb://product-service"))
                
                // Order Service Route
                .route("order-service", r -> r
                        .path("/api/order/**")
                        .uri("lb://order-service"))
                
                // Inventory Service Route
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .uri("lb://inventory-service"))
                
                // Gateway Health Route
                .route("gateway-health", r -> r
                        .path("/api/gateway/health")
                        .uri("no://op"))
                
                .build();
    }
}
