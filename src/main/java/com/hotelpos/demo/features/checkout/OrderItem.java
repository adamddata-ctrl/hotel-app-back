package com.hotelpos.demo.features.checkout;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Unique line-item ID

    @Column(name = "order_id", nullable = false)
    private Integer orderId; // Foreign key mapping back to master order

    @Column(name = "item_id", nullable = false)
    private Integer itemId; // Foreign key tracking food/drink option ordered (e.g., Fried Chicken)

    @Column(nullable = false)
    private Integer quantity; // How many units of this item were ordered

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // Dynamic price snapshot at time of checkout execution
}