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

    // 🔥 PERMANENT FIX: Renamed 'name' to 'itemName' to match your frontend JSON.
    // Pro-tip: '@Column(name = "name")' ensures the database column stays named 'name' even though the Java field is 'itemName'.
    @Column(name = "name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private double quantityOnHand;

    @Column(nullable = false)
    private double minStockLevel;

    @Column(nullable = false)
    private String unitOfMeasure;

    // 🔥 PERMANENT FIX: Added 'category' so the database can receive the frontend value.
    @Column(nullable = false)
    private String category;

    // --- Constructors ---
    public InventoryItem() {}

    public InventoryItem(String itemName, double quantityOnHand, double minStockLevel, String unitOfMeasure, String category) {
        this.itemName = itemName;
        this.quantityOnHand = quantityOnHand;
        this.minStockLevel = minStockLevel;
        this.unitOfMeasure = unitOfMeasure;
        this.category = category;
    }
}