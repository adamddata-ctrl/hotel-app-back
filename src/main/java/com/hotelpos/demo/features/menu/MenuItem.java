package com.hotelpos.demo.features.menu;

import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Menu Item Inventory Entity. Houses product specifics, pricing bounds,
 * and classifications mapped to an explicit enterprise tenant.
 */
@Entity
@Table(name = "menu_items")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Auto-incrementing relational key

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName; // Operational name of food/drink (e.g., "Fried Chicken")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Category category; // Classification separator: FOOD or DRINK

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // Standard billing denomination price matrix

    /**
     * Enumerator breaking down menu sorting types for cashier quick tabs.
     */
    public enum Category {
        FOOD,
        DRINK
    }
}