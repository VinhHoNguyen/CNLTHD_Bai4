package vn.hdbank.intern.inventoryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="inventory")
//@RedisHash
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name ="sku_code", nullable = false, unique = true)
    private String skuCode;
    @Column(name="quantity", nullable = false)
    private Integer quantity;
}
