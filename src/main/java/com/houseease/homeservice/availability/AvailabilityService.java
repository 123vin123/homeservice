package com.houseease.homeservice.availability;

import com.houseease.homeservice.availability.dto.AvailabilityDto;
import com.houseease.homeservice.availability.dto.AvailabilityResponse;
import com.houseease.homeservice.availability.dto.BlockedSlotDto;
import com.houseease.homeservice.provider.ProviderProfile;
import com.houseease.homeservice.provider.ProviderProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final ProviderAvailabilityRepository availabilityRepository;
    private final ProviderBlockedSlotRepository blockedSlotRepository;
    private final ProviderProfileRepository providerRepository;

    public AvailabilityResponse getProviderAvailability(UUID providerId) {
        List<AvailabilityDto> weekly = availabilityRepository.findByProviderProfileIdAndIsActiveTrue(providerId)
                .stream().map(a -> AvailabilityDto.builder()
                        .id(a.getId())
                        .dayOfWeek(a.getDayOfWeek())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .isActive(a.getIsActive())
                        .build())
                .collect(Collectors.toList());

        List<BlockedSlotDto> blocked = blockedSlotRepository.findByProviderProfileIdAndEndDatetimeAfter(providerId, ZonedDateTime.now())
                .stream().map(b -> BlockedSlotDto.builder()
                        .id(b.getId())
                        .startDatetime(b.getStartDatetime())
                        .endDatetime(b.getEndDatetime())
                        .reason(b.getReason())
                        .build())
                .collect(Collectors.toList());

        return AvailabilityResponse.builder()
                .providerId(providerId)
                .weeklyAvailability(weekly)
                .blockedSlots(blocked)
                .build();
    }

    @Transactional
    public AvailabilityDto addAvailability(UUID providerId, AvailabilityDto request) {
        ProviderProfile profile = providerRepository.findById(providerId).orElseThrow();
        
        ProviderAvailability availability = new ProviderAvailability();
        availability.setProviderProfile(profile);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setIsActive(true);
        
        availability = availabilityRepository.save(availability);
        
        request.setId(availability.getId());
        return request;
    }

    @Transactional
    public void removeAvailability(UUID providerId, UUID availabilityId) {
        ProviderAvailability availability = availabilityRepository.findById(availabilityId).orElseThrow();
        if (!availability.getProviderProfile().getId().equals(providerId)) {
            throw new RuntimeException("Unauthorized");
        }
        availability.setIsActive(false);
        availabilityRepository.save(availability);
    }

    @Transactional
    public BlockedSlotDto addBlockedSlot(UUID providerId, BlockedSlotDto request) {
        ProviderProfile profile = providerRepository.findById(providerId).orElseThrow();
        
        ProviderBlockedSlot slot = new ProviderBlockedSlot();
        slot.setProviderProfile(profile);
        slot.setStartDatetime(request.getStartDatetime());
        slot.setEndDatetime(request.getEndDatetime());
        slot.setReason(request.getReason());
        
        slot = blockedSlotRepository.save(slot);
        
        request.setId(slot.getId());
        return request;
    }

    @Transactional
    public void removeBlockedSlot(UUID providerId, UUID slotId) {
        ProviderBlockedSlot slot = blockedSlotRepository.findById(slotId).orElseThrow();
        if (!slot.getProviderProfile().getId().equals(providerId)) {
            throw new RuntimeException("Unauthorized");
        }
        blockedSlotRepository.delete(slot);
    }
}
