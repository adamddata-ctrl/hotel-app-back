package com.hotelpos.demo.features.menu;

import com.hotelpos.demo.core.tenant.TenantContext;
import com.hotelpos.demo.features.inventory.RecipeIngredient;
import com.hotelpos.demo.features.inventory.RecipeIngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu-items")
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    /**
     * Pulls the full localized food and beverage product array matrix.
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
     * 🔥 UPDATED DELETE METHOD: Removes recipe links BEFORE deleting the menu item.
     */
    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long itemId) {
        try {
            // 1. Delete all RecipeIngredient records associated with this menu item first
            List<RecipeIngredient> recipeLinks = recipeIngredientRepository.findByMenuItemId(itemId);
            if (!recipeLinks.isEmpty()) {
                recipeIngredientRepository.deleteAll(recipeLinks);
                System.out.println("RECIPE ENGINE: Deleted " + recipeLinks.size() + " recipe links for menu item " + itemId);
            }

            // 2. Now safely delete the menu item
            menuItemRepository.deleteById(itemId);

            return ResponseEntity.ok(Map.of("success", true, "message", "Menu item deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete menu item: " + e.getMessage()));
        }
    }
}