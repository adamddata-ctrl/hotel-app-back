package com.hotelpos.demo.features.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Integer> {

    // Fetch menu options for the cashier touch matrix grouped by category and restaurant
    List<MenuItem> findByTenantIdAndCategory(String tenantId, MenuItem.Category category);
}