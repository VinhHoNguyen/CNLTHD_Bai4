package com.hdbank.orderservice.config;

import com.hdbank.orderservice.dto.OrderResponse;
import com.hdbank.orderservice.exception.OutOfStockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(OutOfStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<OrderResponse> handleOutOfStockException(OutOfStockException e) {
        log.error("Out of stock exception: {}", e.getMessage());
        OrderResponse response = new OrderResponse(
                null,
                null,
                e.getMessage(),
                "FAILED"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<OrderResponse> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception: {}", e.getMessage());
        OrderResponse response = new OrderResponse(
                null,
                null,
                "Internal server error: " + e.getMessage(),
                "ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<OrderResponse> handleGeneralException(Exception e) {
        log.error("General exception: {}", e.getMessage());
        OrderResponse response = new OrderResponse(
                null,
                null,
                "An unexpected error occurred: " + e.getMessage(),
                "ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
