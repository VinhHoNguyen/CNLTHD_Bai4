package vn.hdbank.intern.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hdbank.intern.inventoryservice.dto.InventoryResponse;
import vn.hdbank.intern.inventoryservice.model.Inventory;
import vn.hdbank.intern.inventoryservice.repository.InventoryRepo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepo inventoryRepo;

    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        Map<String, Inventory> inventoryBySku = inventoryRepo.findBySkuCodeIn(skuCodes)
                .stream()
                .collect(Collectors.toMap(Inventory::getSkuCode, Function.identity()));

        return skuCodes.stream()
                .map(sku -> {
                    Inventory inventory = inventoryBySku.get(sku);
                    return InventoryResponse.builder()
                            .skuCode(sku)
                            .inStock(inventory != null && inventory.getQuantity() > 0)
                            .build();
                })
                .toList();
    }
}
