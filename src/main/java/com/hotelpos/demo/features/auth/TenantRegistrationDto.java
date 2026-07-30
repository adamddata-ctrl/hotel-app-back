package com.hotelpos.demo.features.auth;

import lombok.Data;

@Data
public class TenantRegistrationDto {
    private String restaurantName;
    private String ownerUsername;
    private String ownerPassword;
    private String defaultCashierPin;
}
