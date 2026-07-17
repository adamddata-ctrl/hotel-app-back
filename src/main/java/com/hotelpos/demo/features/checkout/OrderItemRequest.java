package com.hotelpos.demo.features.checkout;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private Integer itemId;
    private Integer quantity;
    private BigDecimal unitPrice;
}