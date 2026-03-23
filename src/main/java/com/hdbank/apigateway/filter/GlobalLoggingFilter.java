package com.hdbank.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class GlobalLoggingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Generate trace ID
        String traceId = UUID.randomUUID().toString();
        
        // Add trace ID to request headers
        exchange.getRequest().mutate()
                .header("X-Trace-Id", traceId)
                .build();
        
        // Log incoming request
        log.info("Incoming Request: {} {} | Trace ID: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                traceId);
        
        // Continue with the chain
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    log.info("Response Status: {} | Trace ID: {}",
                            exchange.getResponse().getStatusCode(),
                            traceId);
                });
    }
}
