package com.houseease.homeservice.availability;

import com.houseease.homeservice.provider.ProviderProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider_blocked_slots")
@Getter
@Setter
public class ProviderBlockedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderProfile providerProfile;

    @Column(name = "start_datetime", nullable = false)
    private ZonedDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private ZonedDateTime endDatetime;

    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
