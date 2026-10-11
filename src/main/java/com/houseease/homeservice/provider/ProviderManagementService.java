package com.houseease.homeservice.provider;

import com.houseease.homeservice.provider.dto.ProviderProfileResponse;
import com.houseease.homeservice.provider.dto.ProviderProfileUpdateRequest;
import com.houseease.homeservice.provider.dto.ProviderServiceDto;
import com.houseease.homeservice.servicecatalog.ServiceEntity;
import com.houseease.homeservice.servicecatalog.ServiceRepository;
import com.houseease.homeservice.user.User;
import com.houseease.homeservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderManagementService {

    private final ProviderProfileRepository providerRepository;
    private final ProviderServiceLinkRepository linkRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProviderProfileResponse getProviderProfile(UUID providerId) {
        ProviderProfile profile = providerRepository.findById(providerId)
                .orElseGet(() -> createDefaultProfile(providerId));
        return mapToResponse(profile);
    }

    @Transactional
    public ProviderProfileResponse updateMyProfile(UUID providerId, ProviderProfileUpdateRequest request) {
        ProviderProfile profile = providerRepository.findById(providerId)
                .orElseGet(() -> createDefaultProfile(providerId));

        profile.setBio(request.getBio());
        profile = providerRepository.save(profile);
        return mapToResponse(profile);
    }

    @Transactional
    public ProviderProfileResponse addServiceToProvider(UUID providerId, UUID serviceId, BigDecimal customPrice, Integer estimatedDurationMinutes) {
        ProviderProfile profile = providerRepository.findById(providerId)
                .orElseGet(() -> createDefaultProfile(providerId));

        if (!"APPROVED".equals(profile.getStatus())) {
            throw new RuntimeException("Your profile must be APPROVED by an administrator before you can add services.");
        }
        ServiceEntity svc = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        ProviderServiceLink link = new ProviderServiceLink();
        link.setProviderId(providerId);
        link.setServiceId(serviceId);
        link.setService(svc);
        link.setCustomPrice(customPrice);
        link.setEstimatedDurationMinutes(estimatedDurationMinutes);

        linkRepository.save(link);
        return getProviderProfile(providerId);
    }

    private ProviderProfile createDefaultProfile(UUID providerId) {
        User user = userRepository.findById(providerId).orElseThrow(() -> new RuntimeException("User not found"));
        ProviderProfile newProfile = ProviderProfile.builder()
                .user(user)
                .id(providerId)
                .bio("New Provider")
                .status("PENDING")
                .build();
        return providerRepository.save(newProfile);
    }

    @Transactional
    public ProviderProfileResponse removeServiceFromProvider(UUID providerId, UUID serviceId) {
        linkRepository.deleteByProviderIdAndServiceId(providerId, serviceId);
        return getProviderProfile(providerId);
    }

    public ProviderProfileResponse updateProviderStatus(UUID providerId, String status) {
        ProviderProfile profile = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        profile.setStatus(status);
        profile = providerRepository.save(profile);
        return mapToResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<ProviderProfileResponse> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ProviderProfileResponse mapToResponse(ProviderProfile profile) {
        List<ProviderServiceDto> services = linkRepository.findByProviderId(profile.getId())
                .stream().map(link -> ProviderServiceDto.builder()
                        .serviceId(link.getService().getId())
                        .serviceName(link.getService().getName())
                        .customPrice(link.getCustomPrice())
                        .estimatedDurationMinutes(link.getEstimatedDurationMinutes())
                        .build())
                .collect(Collectors.toList());

        return ProviderProfileResponse.builder()
                .id(profile.getId())
                .firstName(profile.getUser().getFirstName())
                .lastName(profile.getUser().getLastName())
                .bio(profile.getBio())
                .status(profile.getStatus())
                .averageRating(profile.getAverageRating())
                .totalReviews(profile.getTotalReviews())
                .services(services)
                .build();
    }
}
