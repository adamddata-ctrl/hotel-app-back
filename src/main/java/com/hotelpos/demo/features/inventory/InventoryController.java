package com.hotelpos.demo.features.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller exposing Loyverse-style advanced inventory management features [3.1].
 */
@RestController
@RequestMapping("/api/inventory-management")
@CrossOrigin(origins = "http://localhost:4200")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    /**
     * Downloads all active raw ingredient stocks configured under the current tenant [3.1].
     */
    @GetMapping("/items/all")
    public ResponseEntity<List<InventoryItem>> fetchAllInventoryItems() {
        // Your TenantInterceptor filters this repository call by tenant automatically [3.1]!
        return ResponseEntity.ok(inventoryItemRepository.findAll());
    }

    /**
     * Dispatches stock adjustment commands (Damages, Losses, or Manual Additions) [3.1].
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
     * Submits definitive manual stocktake audit counts to overwrite old numbers [3.1].
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
     * 🔥 ADDED: Registers a brand-new raw ingredient item directly into the database [3.1].
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
     * 🔥 ADDED: Downloads the active shift summary dataset for the current employee [3.1].
     */
    @GetMapping("/shift/summary/{cashierId}")
    public ResponseEntity<?> fetchActiveShiftInflows(@PathVariable String cashierId) {
        try {
            return ResponseEntity.ok(inventoryService.generateShiftReportData(cashierId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Receives new external stock supplies from vendors and logs them into current stock totals [3.1].
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