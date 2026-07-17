package com.hotelpos.demo.features.checkout;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateRequest {
    private String cashierId;
    private Integer waiterId;
    private BigDecimal totalAmount;
    private List<OrderItemRequest> items;
}