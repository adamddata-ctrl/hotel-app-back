package com.hotelpos.demo.features.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    // This interface automatically gives us save(), findAll(), findById(), and delete() functions.
}