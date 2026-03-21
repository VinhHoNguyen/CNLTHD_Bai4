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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;

    public OrderService(OrderRepository orderRepository,
                        InventoryClient inventoryClient,
                        OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.orderProducer = orderProducer;
    }

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        log.info("Placing order with {} line items", orderRequest.getOrderLineItemsDtoList().size());

        // Extract skuCodes for inventory check
        List<String> skuCodes = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(OrderLineItemsDto::getSkuCode)
                .collect(Collectors.toList());

        // Check inventory
        try {
            boolean[] inventoryResult = inventoryClient.checkInventory(skuCodes)
                    .get(); // Block and wait for result

            if (!inventoryResult[0]) {
                log.warn("Order failed - items out of stock for skuCodes: {}", skuCodes);
                throw new OutOfStockException("One or more items are out of stock");
            }
        } catch (Exception e) {
            log.error("Error checking inventory: {}", e.getMessage());
            throw new OutOfStockException("Failed to verify inventory: " + e.getMessage(), e);
        }

        // Create Order
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus("CONFIRMED");

        // Create OrderLineItems
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(dto -> mapDtoToOrderLineItems(dto, order))
                .collect(Collectors.toList());

        order.setOrderLineItemsList(orderLineItems);

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with orderNumber: {}", savedOrder.getOrderNumber());

        // Send event to Kafka
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                "Order Placed"
        );
        orderProducer.sendOrderPlacedEvent(orderPlacedEvent);

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                "Order Placed",
                savedOrder.getStatus()
        );
    }

    private OrderLineItems mapDtoToOrderLineItems(OrderLineItemsDto dto, Order order) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setOrder(order);
        orderLineItems.setSkuCode(dto.getSkuCode());
        orderLineItems.setPrice(dto.getPrice());
        orderLineItems.setQuantity(dto.getQuantity());
        return orderLineItems;
    }

    @Transactional(readOnly = true)
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with number: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }
}
