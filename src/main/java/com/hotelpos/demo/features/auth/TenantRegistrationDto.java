package com.hotelpos.demo.features.auth;

import lombok.Data;

@Data
public class TenantRegistrationDto {
    private String username;
    private String pinCode;
    private String fullName;
}