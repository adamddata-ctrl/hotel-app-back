package com.hotelpos.demo.features.analytics;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.math.BigDecimal; // 👇 ADDED THIS IMPORT

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSalesVolumeReport {
    private String itemName;
    private String category;
    private Long totalQuantitySold;
    private BigDecimal totalRevenueGenerated; // 👇 CHANGED FROM Long TO BigDecimal
}