package com.hotelpos.demo.features.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // 🔥 CRITICAL: This allows the InventoryService to look up an inventory
    // item directly by the menu item ID it is linked to.
    Optional<InventoryItem> findByMenuItemId(Long menuItemId);
}