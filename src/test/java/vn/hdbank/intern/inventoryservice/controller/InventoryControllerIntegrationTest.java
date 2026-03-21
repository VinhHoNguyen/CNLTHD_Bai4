package vn.hdbank.intern.inventoryservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCheckInventorySuccessfully() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_13", "iphone_13_red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuCode").value("iphone_13"))
                .andExpect(jsonPath("$[0].isInStock").value(true))
                .andExpect(jsonPath("$[1].skuCode").value("iphone_13_red"))
                .andExpect(jsonPath("$[1].isInStock").value(false));
    }

    @Test
    void shouldReturnOutOfStockWhenProductDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/inventory")
                        .param("skuCode", "iphone_18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].skuCode").value("iphone_18"))
                .andExpect(jsonPath("$[0].isInStock").value(false));
    }
}
