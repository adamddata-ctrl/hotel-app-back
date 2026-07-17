package com.hotelpos.demo.features.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    // 🔥 ADD THIS LINE: Used by the Angular terminal on boot to pull the whole catalog
    List<MenuItem> findByTenantId(String tenantId);

    // Fetch menu options for the cashier touch matrix grouped by category and restaurant
    List<MenuItem> findByTenantIdAndCategory(String tenantId, MenuItem.Category category);
}