package com.hotelpos.demo.features.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    // 🔥 ADD THIS LINE: Allows the service to find an inventory item directly by its linked menu item ID
    Optional<InventoryItem> findByMenuItemId(Long menuItemId);
}