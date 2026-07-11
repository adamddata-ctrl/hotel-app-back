package com.hotelpos.demo.features.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WaiterPerformanceReport {
    private String waiterName;
    private Long totalOrders;
    private BigDecimal totalRevenue;
}