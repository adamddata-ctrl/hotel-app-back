package com.hotelpos.demo.features.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ItemSalesVolumeReport {
    private String itemName;
    private String category;
    private Long totalQuantitySold;
    private BigDecimal totalRevenueGenerated;
}