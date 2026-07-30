package com.hotelpos.demo.features.auth;

import lombok.Data;

@Data
public class TenantRegistrationDto {
    private String username;
    private String password;
    private String pinCode;
    private String fullName;
}