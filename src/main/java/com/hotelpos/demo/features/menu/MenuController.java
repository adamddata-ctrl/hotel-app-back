package com.hotelpos.demo.features.menu;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap; // 🔥 FIXES LINE 57: Imports the HashMap collection builder class
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/menu")
//@CrossOrigin(origins = "http://localhost:4200") // Connects smoothly with your Angular dev server
public class MenuController {

    @Autowired
    private MenuItemRepository menuItemRepository;
    /**
     * Pulls the full localized food and beverage product array matrix.
     * Automatically scopes lookups using the active ThreadLocal multi-tenant identity.
     */
    @GetMapping("/all")
    public ResponseEntity<List<MenuItem>> getTenantMenuCatalog() {

        // 1. Safely extract the tenant ID token handled by our background WebConfig interceptor firewall
        String activeTenantId = TenantContext.getCurrentTenant();

        // 2. Query only the inventory records belonging to this specific tenant workspace space
        List<MenuItem> catalog = menuItemRepository.findByTenantId(activeTenantId);

        // 3. Stream data parameters back upstream to populate the cashier touchscreen product grid layout
        return ResponseEntity.ok(catalog);
    }

    /**
     * Registers a new custom product menu option into the database.
     * Automatically captures the caller workspace identity from the thread context firewall.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addMenuItemToCatalog(@RequestBody MenuItem newItem) {

        // 1. Defend against malformed form inputs or missing item descriptions
        if (newItem.getItemName() == null || newItem.getItemName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product description name label cannot be empty."));
        }
        if (newItem.getPrice() == null || newItem.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unit price marker must be a positive value calculation."));
        }

        try {
            // 2. Extricate the active restaurant workspace ID from our background ThreadLocal tracker
            String activeTenantId = TenantContext.getCurrentTenant();
            newItem.setTenantId(activeTenantId);

            // 3. Persist the inventory catalog row down into your MySQL engine matrix
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