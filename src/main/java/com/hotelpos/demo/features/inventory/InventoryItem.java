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

    // 🔥 CRITICAL NEW FIELD: Links this inventory item directly to a Menu Item ID!
    @Column(name = "menu_item_id")
    private Long menuItemId;

    // --- Constructors ---
    public InventoryItem() {}

    public InventoryItem(String itemName, double quantityOnHand, double minStockLevel, String unitOfMeasure, String category, Long menuItemId) {
        this.itemName = itemName;
        this.quantityOnHand = quantityOnHand;
        this.minStockLevel = minStockLevel;
        this.unitOfMeasure = unitOfMeasure;
        this.category = category;
        this.menuItemId = menuItemId;
    }
}