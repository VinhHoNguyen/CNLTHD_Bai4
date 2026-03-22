package com.hdbank.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdbank.orderservice.dto.OrderLineItemsDto;
import com.hdbank.orderservice.dto.OrderRequest;
import com.hdbank.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/order/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order Service is running"));
    }

    @Test
    void testPlaceOrderEndpoint_BadRequest() throws Exception {
        // Arrange
        OrderLineItemsDto lineItem = new OrderLineItemsDto(
                null, "iphone_13", BigDecimal.valueOf(1200), 1
        );
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(lineItem));

        // Act & Assert
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetOrderByOrderNumber() throws Exception {
        mockMvc.perform(get("/api/order/test-order-123"))
                .andExpect(status().isNotFound());
    }
}
