package com.hotelpos.demo.features.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesSummaryResponse {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Double averageOrderValue;
}