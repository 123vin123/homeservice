package com.houseease.homeservice.matching;

import com.houseease.homeservice.availability.ProviderAvailability;
import com.houseease.homeservice.availability.ProviderAvailabilityRepository;
import com.houseease.homeservice.availability.ProviderBlockedSlotRepository;
import com.houseease.homeservice.location.Address;
import com.houseease.homeservice.location.AddressRepository;
import com.houseease.homeservice.location.ProviderServiceArea;
import com.houseease.homeservice.location.ProviderServiceAreaRepository;
import com.houseease.homeservice.provider.ProviderProfile;
import com.houseease.homeservice.provider.ProviderServiceLink;
import com.houseease.homeservice.provider.ProviderServiceLinkRepository;
import com.houseease.homeservice.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final AddressRepository addressRepository;
    private final ProviderServiceAreaRepository serviceAreaRepository;
    private final ProviderServiceLinkRepository providerServiceRepository;
    private final ProviderAvailabilityRepository availabilityRepository;
    private final ProviderBlockedSlotRepository blockedSlotRepository;
    private final BookingRepository bookingRepository;

    public List<ProviderMatchingResponse> findEligibleProviders(UUID customerAddressId, UUID serviceId, ZonedDateTime desiredTime) {
        // 1. Get Customer Location
        Address address = addressRepository.findById(customerAddressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        
        double custLat = address.getLatitude().doubleValue();
        double custLon = address.getLongitude().doubleValue();

        // 2. Find Providers who offer this service
        List<ProviderServiceLink> offeringProviders = providerServiceRepository.findAll().stream()
                .filter(link -> link.getServiceId().equals(serviceId))
                .collect(Collectors.toList());

        List<ProviderMatchingResponse> eligibleProviders = new ArrayList<>();

        for (ProviderServiceLink link : offeringProviders) {
            ProviderProfile profile = link.getProviderProfile();

            // Skip if provider is not APPROVED
            if (!"APPROVED".equals(profile.getStatus())) {
                continue;
            }

            // 3. Check Location Coverage (Haversine formula in Java memory for Phase 1 simplicity)
            List<ProviderServiceArea> areas = serviceAreaRepository.findByProviderProfileId(profile.getId());
            boolean inServiceArea = false;
            double shortestDistance = Double.MAX_VALUE;

            for (ProviderServiceArea area : areas) {
                double distance = calculateHaversineDistance(custLat, custLon, area.getLatitude().doubleValue(), area.getLongitude().doubleValue());
                if (distance <= area.getRadiusKm().doubleValue()) {
                    inServiceArea = true;
                    shortestDistance = Math.min(shortestDistance, distance);
                }
            }

            if (!inServiceArea) continue;

            // 4. Calculate Duration and End Time
            Integer duration = link.getEstimatedDurationMinutes() != null ? 
                               link.getEstimatedDurationMinutes() : 
                               link.getService().getDurationMinutes();
            ZonedDateTime desiredEnd = desiredTime.plusMinutes(duration);

            // Convert the absolute UTC time to Asia/Kolkata timezone (or system default)
            // so we can properly compare against the provider's local working hours.
            java.time.ZoneId targetZone = java.time.ZoneId.of("Asia/Kolkata");
            java.time.ZonedDateTime localDesiredTime = desiredTime.withZoneSameInstant(targetZone);
            java.time.ZonedDateTime localDesiredEnd = desiredEnd.withZoneSameInstant(targetZone);
            int dayOfWeek = localDesiredTime.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
            
            // 5. Check Availability
            boolean hasStandardAvailability = availabilityRepository.findByProviderProfileIdAndIsActiveTrue(profile.getId())
                    .stream().anyMatch(a -> a.getDayOfWeek() == dayOfWeek 
                        && !localDesiredTime.toLocalTime().isBefore(a.getStartTime()) 
                        && !localDesiredEnd.toLocalTime().isAfter(a.getEndTime())
                        && localDesiredEnd.toLocalDate().equals(localDesiredTime.toLocalDate()));

            if (!hasStandardAvailability) continue;

            // 6. Check Blocked Slots
            boolean isBlocked = blockedSlotRepository.findAll().stream()
                    .anyMatch(b -> b.getProviderProfile().getId().equals(profile.getId())
                        && b.getStartDatetime().isBefore(desiredEnd) 
                        && b.getEndDatetime().isAfter(desiredTime));

            if (isBlocked) continue;

            // 7. Check Active Bookings
            boolean isAlreadyBooked = bookingRepository.hasConflictingBookings(
                    profile.getId(), desiredTime, desiredEnd);

            if (isAlreadyBooked) continue;

            // Passed all filters! Add to results.
            BigDecimal price = link.getCustomPrice() != null ? link.getCustomPrice() : BigDecimal.ZERO;

            eligibleProviders.add(ProviderMatchingResponse.builder()
                    .providerId(profile.getId())
                    .firstName(profile.getUser().getFirstName())
                    .lastName(profile.getUser().getLastName())
                    .averageRating(profile.getAverageRating())
                    .totalReviews(profile.getTotalReviews())
                    .price(price)
                    .distanceKm(Math.round(shortestDistance * 10.0) / 10.0) // Round to 1 decimal place
                    .build());
        }

        // Sort by distance (closest first)
        eligibleProviders.sort((p1, p2) -> Double.compare(p1.getDistanceKm(), p2.getDistanceKm()));

        return eligibleProviders;
    }

    // Haversine formula to calculate distance between two coordinates in kilometers
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
