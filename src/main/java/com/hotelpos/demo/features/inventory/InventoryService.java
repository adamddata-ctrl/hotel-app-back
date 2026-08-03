package com.hotelpos.demo.features.inventory;

import com.hotelpos.demo.features.menu.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Transactional
    public void deductStockForOrder(MenuItem menuItem, int orderQuantity) {
        if (menuItem == null) return;

        List<RecipeIngredient> recipeList = recipeIngredientRepository.findByMenuItemId(menuItem.getId());

        if (recipeList.isEmpty()) {
            System.out.println("INVENTORY ENGINE: No recipe ingredients found for menu item " + menuItem.getId());
            return;
        }

        for (RecipeIngredient recipe : recipeList) {
            InventoryItem ingredient = recipe.getInventoryItem();
            if (ingredient != null) {
                double totalDeduction = recipe.getQuantityRequired() * orderQuantity;
                double newStock = ingredient.getQuantityOnHand() - totalDeduction;

                if (newStock < 0) newStock = 0;

                ingredient.setQuantityOnHand(newStock);
                inventoryItemRepository.save(ingredient);
                System.out.println("INVENTORY ENGINE: Deducted " + totalDeduction + " from " + ingredient.getItemName());
            }
        }
    }

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