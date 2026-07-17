package com.hotelpos.demo.features.inventory;

import com.hotelpos.demo.features.menu.MenuItem;
import com.hotelpos.demo.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "recipe_ingredients")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem; // The menu item that uses this ingredient (e.g., Burger)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem; // The raw ingredient being used (e.g., Beef Patty)

    @Column(nullable = false)
    private double quantityRequired; // Amount consumed per order (e.g., 1.0, 0.150 for kg)
}