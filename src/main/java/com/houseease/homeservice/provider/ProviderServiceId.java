package com.houseease.homeservice.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderServiceId implements Serializable {
    private UUID providerId;
    private UUID serviceId;
}
