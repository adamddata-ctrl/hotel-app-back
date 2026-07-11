package com.hotelpos.demo.features.checkout;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private String cashierId;
    private Integer waiterId;
    private BigDecimal totalAmount;
    private List<ItemDetails> items;

    @Data
    public static class ItemDetails {
        private Integer itemId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}