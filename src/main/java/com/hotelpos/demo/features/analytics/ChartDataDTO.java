package com.hotelpos.demo.features.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartDataDTO {
    private String timeLabel;  // Holds Month name ("December") or Specific Day Date ("Dec 12")
    private int monthNum;      // Numeric month helper identifier (e.g., 12 for December)
    private double salesTotal; // Gross sales volume amount calculated for that period
}