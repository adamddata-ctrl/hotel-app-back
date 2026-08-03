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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private double quantityOnHand;

    @Column(nullable = false)
    private double minStockLevel;

    @Column(nullable = false)
    private String unitOfMeasure;

    @Column(nullable = false)
    private String category;

    public InventoryItem() {}

    public InventoryItem(String itemName, double quantityOnHand, double minStockLevel, String unitOfMeasure, String category) {
        this.itemName = itemName;
        this.quantityOnHand = quantityOnHand;
        this.minStockLevel = minStockLevel;
        this.unitOfMeasure = unitOfMeasure;
        this.category = category;
    }
}