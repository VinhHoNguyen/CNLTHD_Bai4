package com.hdbank.orderservice.service;

import com.hdbank.orderservice.client.InventoryClient;
import com.hdbank.orderservice.dto.OrderLineItemsDto;
import com.hdbank.orderservice.dto.OrderRequest;
import com.hdbank.orderservice.dto.OrderResponse;
import com.hdbank.orderservice.event.OrderPlacedEvent;
import com.hdbank.orderservice.exception.OutOfStockException;
import com.hdbank.orderservice.model.Order;
import com.hdbank.orderservice.model.OrderLineItems;
import com.hdbank.orderservice.producer.OrderProducer;
import com.hdbank.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private OrderProducer orderProducer;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, inventoryClient, orderProducer);
    }

    @Test
    void testPlaceOrderSuccessfully() throws Exception {
        // Arrange
        OrderLineItemsDto lineItem = new OrderLineItemsDto(
                null, "iphone_13", BigDecimal.valueOf(1200), 1
        );
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(lineItem));

        // Mock inventory check returns true
        when(inventoryClient.checkInventory(any()))
                .thenReturn(CompletableFuture.completedFuture(new boolean[]{true}));

        // Mock order save
        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("test-order-123");
        savedOrder.setStatus("CONFIRMED");
        savedOrder.setOrderLineItemsList(new ArrayList<>());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.placeOrder(orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Order Placed", response.getMessage());
        assertEquals("CONFIRMED", response.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProducer, times(1)).sendOrderPlacedEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void testPlaceOrderFailsWhenOutOfStock() throws Exception {
        // Arrange
        OrderLineItemsDto lineItem = new OrderLineItemsDto(
                null, "iphone_13", BigDecimal.valueOf(1200), 1
        );
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(lineItem));

        // Mock inventory check returns false
        when(inventoryClient.checkInventory(any()))
                .thenReturn(CompletableFuture.completedFuture(new boolean[]{false}));

        // Act & Assert
        assertThrows(OutOfStockException.class, () -> {
            orderService.placeOrder(orderRequest);
        });

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderProducer, never()).sendOrderPlacedEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void testPlaceOrderWithMultipleItems() throws Exception {
        // Arrange
        List<OrderLineItemsDto> lineItems = Arrays.asList(
                new OrderLineItemsDto(null, "iphone_13", BigDecimal.valueOf(1200), 1),
                new OrderLineItemsDto(null, "ipad_pro", BigDecimal.valueOf(1500), 2)
        );
        OrderRequest orderRequest = new OrderRequest(lineItems);

        when(inventoryClient.checkInventory(any()))
                .thenReturn(CompletableFuture.completedFuture(new boolean[]{true}));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setOrderNumber("test-order-456");
        savedOrder.setStatus("CONFIRMED");
        savedOrder.setOrderLineItemsList(new ArrayList<>());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        OrderResponse response = orderService.placeOrder(orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Order Placed", response.getMessage());
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
