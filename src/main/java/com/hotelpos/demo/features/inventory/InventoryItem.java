package com.hotelpos.demo.features.inventory;

import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "inventory_items")
public class InventoryItem extends BaseEntity {

    // --- Getters and Setters ---
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name; // e.g., "Coca Cola Bottle", "Burger Bun", "Cheese slice"

    @Column(nullable = false)
    private double quantityOnHand; // Current physical stock level

    @Column(nullable = false)
    private double minStockLevel; // Trigger threshold for low-stock alerts

    @Column(nullable = false)
    private String unitOfMeasure; // e.g., "pcs", "kg", "ml"

    // --- Constructors ---
    public InventoryItem() {}

    public InventoryItem(String name, double quantityOnHand, double minStockLevel, String unitOfMeasure) {
        this.name = name;
        this.quantityOnHand = quantityOnHand;
        this.minStockLevel = minStockLevel;
        this.unitOfMeasure = unitOfMeasure;
    }

}