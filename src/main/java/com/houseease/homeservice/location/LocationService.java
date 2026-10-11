package com.houseease.homeservice.location;

import com.houseease.homeservice.location.dto.AddressDto;
import com.houseease.homeservice.location.dto.ProviderServiceAreaDto;
import com.houseease.homeservice.provider.ProviderProfile;
import com.houseease.homeservice.provider.ProviderProfileRepository;
import com.houseease.homeservice.user.User;
import com.houseease.homeservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final AddressRepository addressRepository;
    private final ProviderServiceAreaRepository serviceAreaRepository;
    private final UserRepository userRepository;
    private final ProviderProfileRepository providerRepository;

    public List<AddressDto> getMyAddresses(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AddressDto addAddress(UUID userId, AddressDto request) {
        User user = userRepository.findById(userId).orElseThrow();
        Address address = new Address();
        address.setUser(user);
        address.setLabel(request.getLabel());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        
        return mapToDto(addressRepository.save(address));
    }

    public AddressDto updateAddress(UUID userId, UUID addressId, AddressDto request) {
        Address address = addressRepository.findById(addressId).orElseThrow();
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        address.setLabel(request.getLabel());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        
        return mapToDto(addressRepository.save(address));
    }

    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow();
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        addressRepository.delete(address);
    }

    public List<ProviderServiceAreaDto> getMyServiceAreas(UUID providerId) {
        return serviceAreaRepository.findByProviderProfileId(providerId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public ProviderServiceAreaDto addServiceArea(UUID providerId, ProviderServiceAreaDto request) {
        ProviderProfile profile = providerRepository.findById(providerId).orElseThrow();
        ProviderServiceArea area = new ProviderServiceArea();
        area.setProviderProfile(profile);
        area.setLatitude(request.getLatitude());
        area.setLongitude(request.getLongitude());
        area.setRadiusKm(request.getRadiusKm());

        return mapToDto(serviceAreaRepository.save(area));
    }

    public void deleteServiceArea(UUID providerId, UUID areaId) {
        ProviderServiceArea area = serviceAreaRepository.findById(areaId).orElseThrow();
        if (!area.getProviderProfile().getId().equals(providerId)) {
            throw new RuntimeException("Unauthorized");
        }
        serviceAreaRepository.delete(area);
    }

    private AddressDto mapToDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .label(address.getLabel())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }

    private ProviderServiceAreaDto mapToDto(ProviderServiceArea area) {
        return ProviderServiceAreaDto.builder()
                .id(area.getId())
                .latitude(area.getLatitude())
                .longitude(area.getLongitude())
                .radiusKm(area.getRadiusKm())
                .build();
    }
}
