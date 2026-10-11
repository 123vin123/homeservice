package com.houseease.homeservice.user.dto;

import com.houseease.homeservice.user.Role;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
}
