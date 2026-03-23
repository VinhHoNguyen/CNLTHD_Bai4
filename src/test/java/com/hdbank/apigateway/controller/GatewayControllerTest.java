package com.hdbank.apigateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGatewayHealth() throws Exception {
        mockMvc.perform(get("/api/gateway/health"))
                .andExpect(status().isOk());
    }

    @Test
    void testGatewayStatus() throws Exception {
        mockMvc.perform(get("/api/gateway/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.component").value("API-Gateway"));
    }

    @Test
    void testGetServices() throws Exception {
        mockMvc.perform(get("/api/gateway/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_services").exists())
                .andExpect(jsonPath("$.services").exists());
    }
}
