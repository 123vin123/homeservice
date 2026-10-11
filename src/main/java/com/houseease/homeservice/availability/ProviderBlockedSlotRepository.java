package com.houseease.homeservice.availability;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ProviderBlockedSlotRepository extends JpaRepository<ProviderBlockedSlot, UUID> {
    List<ProviderBlockedSlot> findByProviderProfileIdAndEndDatetimeAfter(UUID providerId, ZonedDateTime now);
}
