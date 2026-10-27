package com.fooddelivery.service.impl;

import com.fooddelivery.dto.request.LoginRequest;
import com.fooddelivery.dto.request.RefreshTokenRequest;
import com.fooddelivery.dto.request.RegisterRequest;
import com.fooddelivery.dto.response.AuthResponse;
import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.entity.DeliveryProfile;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ConflictException;
import com.fooddelivery.exception.InvalidTokenException;
import com.fooddelivery.repository.DeliveryProfileRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.security.CustomUserDetails;
import com.fooddelivery.security.JwtService;
import com.fooddelivery.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DeliveryProfileRepository deliveryProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           DeliveryProfileRepository deliveryProfileRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.deliveryProfileRepository = deliveryProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setEnabled(true);
        user = userRepository.save(user);

        if (user.getRole() == Role.DELIVERY_PARTNER) {
            DeliveryProfile profile = new DeliveryProfile();
            profile.setUserId(user.getId());
            profile.setAvailable(false);
            deliveryProfileRepository.save(profile);
        }
        return buildResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return buildResponse(principal.getUser());
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            Claims claims = jwtService.parse(request.refreshToken());
            if (!jwtService.isRefreshToken(claims)) {
                throw new InvalidTokenException("The provided token is not a refresh token");
            }
            User user = userRepository.findByEmail(claims.getSubject())
                    .orElseThrow(() -> new InvalidTokenException("User no longer exists"));
            if (!user.isEnabled()) {
                throw new InvalidTokenException("This account is disabled");
            }
            return buildResponse(user);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
    }

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                "Bearer",
                jwtService.getAccessTokenSeconds(),
                UserResponse.from(user));
    }
}
