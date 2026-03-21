package vn.hdbank.intern.inventoryservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.hdbank.intern.inventoryservice.model.Inventory;
import vn.hdbank.intern.inventoryservice.repository.InventoryRepo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceUnitTest {

    @Mock
    private InventoryRepo inventoryRepo;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldReturnStockStatusForRequestedSkuCodes() {
        Inventory iphone13 = Inventory.builder()
                .id(1L)
                .skuCode("iphone_13")
                .quantity(5)
                .build();

        Inventory iphone13Red = Inventory.builder()
                .id(2L)
                .skuCode("iphone_13_red")
                .quantity(0)
                .build();

        when(inventoryRepo.findBySkuCodeIn(List.of("iphone_13", "iphone_13_red")))
                .thenReturn(List.of(iphone13, iphone13Red));

        var result = inventoryService.isInStock(List.of("iphone_13", "iphone_13_red"));

        assertEquals(2, result.size());
        assertEquals("iphone_13", result.get(0).getSkuCode());
        assertTrue(result.get(0).isInStock());
        assertEquals("iphone_13_red", result.get(1).getSkuCode());
        assertFalse(result.get(1).isInStock());
    }

    @Test
    void shouldReturnFalseWhenSkuCodeDoesNotExist() {
        when(inventoryRepo.findBySkuCodeIn(List.of("iphone_18")))
                .thenReturn(List.of());

        var result = inventoryService.isInStock(List.of("iphone_18"));

        assertEquals(1, result.size());
        assertEquals("iphone_18", result.get(0).getSkuCode());
        assertFalse(result.get(0).isInStock());
    }
}
