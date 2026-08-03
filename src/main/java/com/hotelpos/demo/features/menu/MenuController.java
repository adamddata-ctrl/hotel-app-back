package com.hotelpos.demo.features.menu;

import com.hotelpos.demo.core.tenant.TenantContext;
import com.hotelpos.demo.features.inventory.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/menu-items")
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    /**
     * Pulls the full localized food and beverage product array matrix.
     * Handles `/api/menu-items`, `/api/menu-items/`, AND `/api/menu-items/active`!
     */
    @GetMapping({"", "/", "/active"})
    public ResponseEntity<List<MenuItem>> getTenantMenuCatalog() {
        String activeTenantId = TenantContext.getCurrentTenant();
        List<MenuItem> catalog = menuItemRepository.findByTenantId(activeTenantId);
        return ResponseEntity.ok(catalog);
    }

    /**
     * Registers a new custom product menu option into the database.
     */
    @PostMapping("/create")
    public ResponseEntity<?> addMenuItemToCatalog(@RequestBody MenuItem newItem) {

        if (newItem.getItemName() == null || newItem.getItemName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product description name label cannot be empty."));
        }
        if (newItem.getPrice() == null || newItem.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unit price marker must be a positive value calculation."));
        }

        try {
            String activeTenantId = TenantContext.getCurrentTenant();
            newItem.setTenantId(activeTenantId);

            MenuItem savedItem = menuItemRepository.save(newItem);

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("id", savedItem.getId());
            successResponse.put("message", "New menu item successfully synchronized to multi-tenant inventory tables.");

            return ResponseEntity.ok(successResponse);

        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Inventory registry transaction isolation mapping failure.",
                    "details", ex.getMessage()
            ));
        }
    }

    /**
     * 🔥 DELETES MENU ITEM: Clears the menu_item_id link in inventory before deleting the item!
     * No more type mismatch errors because everything now uses Long!
     */
    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long itemId) {
        try {
            // 1. Find the inventory item linked to this menu item
            inventoryItemRepository.findByMenuItemId(itemId).ifPresent(inventoryItem -> {
                // 2. Clear the link so MySQL allows the deletion
                inventoryItem.setMenuItemId(null);
                inventoryItemRepository.save(inventoryItem);
                System.out.println("INVENTORY ENGINE: Unlinked inventory item " + inventoryItem.getItemName() + " from menu item " + itemId);
            });

            // 3. Now safely delete the menu item (deleteById now accepts Long perfectly!)
            menuItemRepository.deleteById(itemId);

            return ResponseEntity.ok(Map.of("success", true, "message", "Menu item deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete menu item: " + e.getMessage()));
        }
    }
}