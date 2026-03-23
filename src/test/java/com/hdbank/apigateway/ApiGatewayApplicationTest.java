package com.hdbank.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void contextLoads() {
        assertNotNull(testRestTemplate);
    }

    @Test
    void testGatewayHealth() {
        ResponseEntity<String> response = testRestTemplate
                .getForEntity("/api/gateway/health", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("API Gateway is running", response.getBody());
    }

    @Test
    void testActuatorHealth() {
        ResponseEntity<String> response = testRestTemplate
                .getForEntity("/actuator/health", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testActuatorMetrics() {
        ResponseEntity<String> response = testRestTemplate
                .getForEntity("/actuator/prometheus", String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
