package com.hotelpos.demo.features.menu;

import com.hotelpos.demo.core.tenant.TenantContext;
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

    /**
     * Pulls the full localized food and beverage product array matrix.
     * 🔥 ULTIMATE FIX: Handles `/api/menu-items`, `/api/menu-items/`, AND `/api/menu-items/active`!
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
    @PostMapping
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
}