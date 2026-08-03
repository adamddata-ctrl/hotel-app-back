package com.hotelpos.demo.features.inventory;

import com.hotelpos.demo.features.menu.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    // =========================================================================
    // 🔥 NEW SIMPLIFIED DEDUCTION LOGIC: Directly finds the inventory linked by menu_item_id
    // =========================================================================
    @Transactional
    public void deductStockForOrder(MenuItem menuItem, int orderQuantity) {
        if (menuItem == null) return;

        // Look for the Inventory Item linked to this Menu Item ID
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findByMenuItemId(menuItem.getId());

        if (optionalItem.isPresent()) {
            InventoryItem ingredient = optionalItem.get();
            double newStock = ingredient.getQuantityOnHand() - orderQuantity;

            // Prevent negative stock
            if (newStock < 0) newStock = 0;

            ingredient.setQuantityOnHand(newStock);
            inventoryItemRepository.save(ingredient);
            System.out.println("INVENTORY ENGINE: Deducted " + orderQuantity + " from " + ingredient.getItemName());
        } else {
            System.out.println("INVENTORY ENGINE: No inventory item linked to menu item ID " + menuItem.getId());
        }
    }

    // --- Your existing methods for adjustments, counts, and purchase orders ---
    @Transactional
    public InventoryItem executeStockAdjustment(Long itemId, double quantityChange) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item with specified ID does not exist."));
        item.setQuantityOnHand(item.getQuantityOnHand() + quantityChange);
        return inventoryItemRepository.save(item);
    }

    @Transactional
    public InventoryItem executeInventoryCount(Long itemId, double manualCountedQuantity) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item with specified ID does not exist."));
        item.setQuantityOnHand(manualCountedQuantity);
        return inventoryItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public ShiftSummaryData generateShiftReportData(String cashierId) {
        ShiftSummaryData summary = new ShiftSummaryData();
        summary.setTotalCheckouts(14);
        summary.setGrossSalesVolume(420.50);
        summary.setCashInflow(310.00);
        summary.setCardInflow(110.50);
        return summary;
    }

    @Transactional
    public InventoryItem executeReceivePurchaseOrder(Long itemId, double receivedQuantity) {
        InventoryItem item = inventoryItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item with specified ID does not exist."));
        item.setQuantityOnHand(item.getQuantityOnHand() + receivedQuantity);
        return inventoryItemRepository.save(item);
    }
}