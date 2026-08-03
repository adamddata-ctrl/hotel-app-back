package com.hotelpos.demo.features.inventory;

import com.hotelpos.demo.core.tenant.TenantContext;
import com.hotelpos.demo.features.menu.MenuItem;
import com.hotelpos.demo.features.menu.MenuItemRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Fetches all inventory items strictly filtered by the active tenant context workspace.
     */
    @GetMapping("/items/all")
    public ResponseEntity<List<InventoryItem>> fetchAllInventoryItems() {
        String activeTenantId = TenantContext.getCurrentTenant();

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", activeTenantId);

        List<InventoryItem> items = inventoryItemRepository.findAll();

        session.disableFilter("tenantFilter");

        return ResponseEntity.ok(items);
    }

    /**
     * Processes inventory ingredient manual stock adjustments.
     */
    @PostMapping("/adjust")
    public ResponseEntity<?> processStockAdjustment(@RequestBody InventoryActionRequest request) {
        try {
            InventoryItem updatedItem = inventoryService.executeStockAdjustment(
                    request.getItemId(),
                    request.getQuantityValue()
            );
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Processes inventory stock-takes and manual item counting overrides.
     */
    @PostMapping("/count")
    public ResponseEntity<?> processInventoryCount(@RequestBody InventoryActionRequest request) {
        try {
            InventoryItem updatedItem = inventoryService.executeInventoryCount(
                    request.getItemId(),
                    request.getQuantityValue()
            );
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Registers a brand-new raw ingredient item directly into the database.
     * Handles multi-selected menu IDs and creates recipe links automatically.
     */
    @PostMapping("/items/create")
    public ResponseEntity<?> createNewInventoryItem(@RequestBody Map<String, Object> payload) {
        try {
            String itemName = (String) payload.get("itemName");
            double quantityOnHand = Double.parseDouble(payload.get("quantityOnHand").toString());
            double minStockLevel = Double.parseDouble(payload.get("minStockLevel").toString());
            String unitOfMeasure = (String) payload.get("unitOfMeasure");
            String category = (String) payload.get("category");

            List<Integer> linkedMenuIds = (List<Integer>) payload.get("linkedMenuIds");
            if (linkedMenuIds == null) {
                linkedMenuIds = new ArrayList<>();
            }

            InventoryItem newItem = new InventoryItem(itemName, quantityOnHand, minStockLevel, unitOfMeasure, category);
            InventoryItem savedItem = inventoryItemRepository.save(newItem);

            for (Integer menuId : linkedMenuIds) {
                menuItemRepository.findById(menuId.longValue()).ifPresent(menuItem -> {
                    RecipeIngredient recipe = new RecipeIngredient();
                    recipe.setMenuItem(menuItem);
                    recipe.setInventoryItem(savedItem);
                    recipe.setQuantityRequired(1.0);
                    recipeIngredientRepository.save(recipe);
                });
            }

            return ResponseEntity.ok(savedItem);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Cryptographic Boundary Check: Fetches shift analytical balance reports for a cashier safely.
     */
    @GetMapping("/shift/summary/{cashierId}")
    public ResponseEntity<?> fetchActiveShiftInflows(@PathVariable("cashierId") String cashierId) {
        String activeTenantId = TenantContext.getCurrentTenant();
        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Security Violation: Tenant identification context missing."));
        }

        try {
            var shiftReport = inventoryService.generateShiftReportData(cashierId);
            return ResponseEntity.ok(shiftReport);
        } catch (Exception e) {
            // 🔥 FIXED: This returns a JSON error object instead of a raw null-unsafe string, clearing the red squiggle!
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "An unknown error occurred while fetching shift report."
            ));
        }
    }

    /**
     * Processes goods receiving tasks from supplier purchase order tickets.
     */
    @PostMapping("/purchase-order/receive")
    public ResponseEntity<?> processReceivePurchaseOrder(@RequestBody InventoryActionRequest request) {
        try {
            InventoryItem updatedItem = inventoryService.executeReceivePurchaseOrder(
                    request.getItemId(),
                    request.getQuantityValue()
            );
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

/**
 * Unified Request Data Transfer Object container layer
 */
@lombok.Data
class InventoryActionRequest {
    private Long itemId;
    private double quantityValue;
}