package com.hotelpos.demo.features.auth;

public class LoginRequest {
    private String username;
    private String pinCode;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }
}