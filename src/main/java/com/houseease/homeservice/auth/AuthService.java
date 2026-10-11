package com.houseease.homeservice.auth;

import com.houseease.homeservice.auth.dto.AuthResponse;
import com.houseease.homeservice.auth.dto.LoginRequest;
import com.houseease.homeservice.auth.dto.RegisterRequest;
import com.houseease.homeservice.user.Role;
import com.houseease.homeservice.user.User;
import com.houseease.homeservice.user.UserRepository;
import com.houseease.homeservice.location.Address;
import com.houseease.homeservice.location.AddressRepository;
import com.houseease.homeservice.location.ProviderServiceArea;
import com.houseease.homeservice.location.ProviderServiceAreaRepository;
import com.houseease.homeservice.provider.ProviderProfile;
import com.houseease.homeservice.provider.ProviderProfileRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AddressRepository addressRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ProviderServiceAreaRepository providerServiceAreaRepository;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        
        try {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        } catch (Exception e) {
            user.setRole(Role.CUSTOMER); // default
        }

        userRepository.save(user);

        if (user.getRole() == Role.CUSTOMER) {
            if (request.getAddressLine() != null && request.getCity() != null) {
                Address address = new Address();
                address.setUser(user);
                address.setLabel("Home");
                address.setAddressLine(request.getAddressLine());
                address.setCity(request.getCity());
                address.setLatitude(request.getLatitude() != null ? request.getLatitude() : new BigDecimal("40.7128"));
                address.setLongitude(request.getLongitude() != null ? request.getLongitude() : new BigDecimal("-74.0060"));
                addressRepository.save(address);
            }
        } else if (user.getRole() == Role.PROVIDER) {
            ProviderProfile profile = ProviderProfile.builder()
                    .user(user)
                    .id(user.getId())
                    .bio("New Provider")
                    .status("PENDING")
                    .build();
            providerProfileRepository.save(profile);

            if (request.getLatitude() != null && request.getLongitude() != null) {
                ProviderServiceArea area = new ProviderServiceArea();
                area.setProviderProfile(profile);
                area.setLatitude(request.getLatitude());
                area.setLongitude(request.getLongitude());
                area.setRadiusKm(new BigDecimal("25.0"));
                providerServiceAreaRepository.save(area);
            }
        }

        String jwt = jwtUtil.generateToken(new CustomUserDetails(user));
        return new AuthResponse(jwt);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = jwtUtil.generateToken(new CustomUserDetails(user));
        return new AuthResponse(jwt);
    }
}
