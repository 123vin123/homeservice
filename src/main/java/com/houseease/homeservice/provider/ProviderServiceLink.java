package com.houseease.homeservice.provider;

import com.houseease.homeservice.servicecatalog.ServiceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "provider_services")
@IdClass(ProviderServiceId.class)
@Getter
@Setter
public class ProviderServiceLink {

    @Id
    @Column(name = "provider_id")
    private UUID providerId;

    @Id
    @Column(name = "service_id")
    private UUID serviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", insertable = false, updatable = false)
    private ProviderProfile providerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    private ServiceEntity service;

    @Column(name = "custom_price")
    private BigDecimal customPrice;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
}
