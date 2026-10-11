package com.houseease.homeservice.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String role; // "CUSTOMER" or "PROVIDER"

    private String addressLine;
    private String city;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
}
