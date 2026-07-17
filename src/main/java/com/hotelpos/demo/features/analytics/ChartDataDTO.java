package com.hotelpos.demo.features.analytics;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor  // 👇 ADDED: Required by Hibernate to instantiate the object
@AllArgsConstructor
public class ChartDataDTO {
    private String timeLabel;
    private int monthNum;
    private Double salesTotal; // 👇 CHANGED: Primitive double changed to Double object wrapper
}