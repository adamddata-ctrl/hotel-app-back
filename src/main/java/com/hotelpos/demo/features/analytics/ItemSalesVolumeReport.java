package com.hotelpos.demo.features.analytics;

import com.hotelpos.demo.features.menu.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSalesVolumeReport {
    private String itemName;
    private MenuItem.Category category; // Matches your enum type layout
    private Long totalQuantitySold;      // 🔥 Fixed: Changed to Long to match JPQL SUM()
    private Long totalRevenueGenerated;  // 🔥 Fixed: Changed to Long to match JPQL SUM()
}