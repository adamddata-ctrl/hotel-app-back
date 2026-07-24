package com.hotelpos.demo.features.inventory;

import com.hotelpos.demo.core.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory") // FIX: Standardized clean top-level path matching our project pattern
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    /**
     * Fetches all inventory items for the active tenant context workspace.
     */
    @GetMapping("/items/all")
    public ResponseEntity<List<InventoryItem>> fetchAllInventoryItems() {
        // Your TenantInterceptor filters this repository call by tenant automatically [3.1]!
        return ResponseEntity.ok(inventoryItemRepository.findAll());
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
     * Registers a brand-new raw ingredient item directly into the database [3.1].
     */
    @PostMapping("/items/create")
    public ResponseEntity<?> createNewInventoryItem(@RequestBody InventoryItem newItem) {
        try {
            // Your multi-tenant TenantInterceptor binds the active tenant_id automatically! [3.1]
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
        // SECURE FAIL CLOSED: Intercept requests to ensure a valid tenant context is bound to the thread
        String activeTenantId = TenantContext.getCurrentTenant();
        if (activeTenantId == null || activeTenantId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Security Violation: Tenant identification context missing."));
        }

        try {
            // Your service logic aggregates transactions safely under the current active workspace parameters
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

/**
 * Unified Request Data Transfer Object container layer
 */
@lombok.Data
class InventoryActionRequest {
    private Long itemId;
    private double quantityValue; // Set to 'double' to cleanly support fractional weights [3.1]
}