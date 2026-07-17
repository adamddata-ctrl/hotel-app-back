package com.hotelpos.demo.features.inventory; // Make sure this matches the package folder you placed it in!

import lombok.Data;

@Data
public class ShiftSummaryData {
    private int totalCheckouts;
    private double grossSalesVolume;
    private double cashInflow;
    private double cardInflow;
}