package com.houseease.homeservice.provider;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProviderServiceLinkRepository extends JpaRepository<ProviderServiceLink, ProviderServiceId> {
    List<ProviderServiceLink> findByProviderId(UUID providerId);
    void deleteByProviderIdAndServiceId(UUID providerId, UUID serviceId);
}
