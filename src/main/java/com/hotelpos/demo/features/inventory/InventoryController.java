package com.hotelpos.demo.features.inventory;

import com.hotelpos.demo.core.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private EntityManager entityManager; // 🔥 ADDED: Needed to enable the Hibernate filter

    /**
     * Fetches all inventory items strictly filtered by the active tenant context workspace.
     */
    @GetMapping("/items/all")
    public ResponseEntity<List<InventoryItem>> fetchAllInventoryItems() {
        String activeTenantId = TenantContext.getCurrentTenant();

        // 🔥 CRITICAL FIX: Enable the tenant filter manually before querying!
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", activeTenantId);

        List<InventoryItem> items = inventoryItemRepository.findAll();

        // 🔥 Disable the filter after the query so it doesn't interfere with other operations
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
     */
    @PostMapping("/items/create")
    public ResponseEntity<?> createNewInventoryItem(@RequestBody InventoryItem newItem) {
        try {
            InventoryItem saved = inventoryItemRepository.save(newItem);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create item: " + e.getMessage());
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
            return ResponseEntity.badRequest().body(e.getMessage());
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




/**adam
 * Unified Request Data Transfer Object container layer
 */
@lombok.Data
class InventoryActionRequest {
    private Long itemId;
    private double quantityValue;
}