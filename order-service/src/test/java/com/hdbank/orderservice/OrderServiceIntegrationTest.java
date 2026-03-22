package com.hdbank.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdbank.orderservice.dto.OrderLineItemsDto;
import com.hdbank.orderservice.dto.OrderRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("order_test_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testPlaceOrderIntegration() throws Exception {
        // Arrange
        OrderLineItemsDto lineItem = new OrderLineItemsDto(
                null, "iphone_13", BigDecimal.valueOf(1200), 1
        );
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(lineItem));

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println("Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testMultipleOrderPlacement() throws Exception {
        // Arrange
        OrderLineItemsDto item1 = new OrderLineItemsDto(
                null, "iphone_13", BigDecimal.valueOf(1200), 1
        );
        OrderLineItemsDto item2 = new OrderLineItemsDto(
                null, "ipad_pro", BigDecimal.valueOf(1500), 2
        );
        OrderRequest orderRequest = new OrderRequest(Arrays.asList(item1, item2));

        // Act & Assert
        mockMvc.perform(post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isBadRequest());
    }
}
