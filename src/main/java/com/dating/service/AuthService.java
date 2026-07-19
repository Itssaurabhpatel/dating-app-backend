package com.dating.service;

import com.dating.dto.*;
import com.dating.entity.RefreshToken;
import com.dating.entity.User;
import com.dating.mapper.UserMapper;
import com.dating.repository.RefreshTokenRepository;
import com.dating.repository.UserRepository;
import com.dating.dto.ApiResponse;
import com.dating.dto.JwtTokenDto;
import com.dating.exception.DatingAppException;
import com.dating.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 3;

    @Transactional
    public ApiResponse<JwtTokenDto> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw DatingAppException.conflict("Email already registered");
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);
        JwtTokenDto tokens = generateTokens(user);
        saveRefreshToken(user, tokens.getRefreshToken(), "Web Registration");
        kafkaTemplate.send("user-events", Map.of("event", "USER_REGISTERED", "userId", user.getId(), "email", user.getEmail()));
        return ApiResponse.success(tokens, "Registration successful");
    }

    @Transactional
    public ApiResponse<JwtTokenDto> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> DatingAppException.unauthorized("Invalid credentials"));
        if (user.getStatus() != User.UserStatus.ACTIVE) throw DatingAppException.forbidden("Account is " + user.getStatus().name().toLowerCase());
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) throw DatingAppException.unauthorized("Invalid credentials");
        userRepository.updateLastActive(user.getId(), Instant.now());
        JwtTokenDto tokens = generateTokens(user);
        saveRefreshToken(user, tokens.getRefreshToken(), request.getDeviceInfo());
        return ApiResponse.success(tokens, "Login successful");
    }

    @Transactional
    public ApiResponse<JwtTokenDto> refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> DatingAppException.unauthorized("Invalid refresh token"));
        if (refreshToken.getRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now()))
            throw DatingAppException.unauthorized("Refresh token expired or revoked");
        User user = refreshToken.getUser();
        if (user.getStatus() != User.UserStatus.ACTIVE) throw DatingAppException.forbidden("Account is inactive");
        JwtTokenDto tokens = generateTokens(user);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        saveRefreshToken(user, tokens.getRefreshToken(), refreshToken.getDeviceInfo());
        return ApiResponse.success(tokens, "Token refreshed successfully");
    }

    @Transactional
    public ApiResponse<Void> logout(String userId, String accessToken) {
        User user = userRepository.findById(userId).orElseThrow(() -> DatingAppException.notFound("User"));
        refreshTokenRepository.revokeAllByUser(user);
        long ttl = jwtUtil.getAccessTokenExpiration();
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "true", ttl, TimeUnit.MILLISECONDS);
        return ApiResponse.success(null, "Logout successful");
    }

    @Transactional
    public ApiResponse<Void> deleteAccount(String userId, String accessToken) {
        User user = userRepository.findById(userId).orElseThrow(() -> DatingAppException.notFound("User"));
        refreshTokenRepository.revokeAllByUser(user);
        userRepository.softDelete(userId, Instant.now());
        long ttl = jwtUtil.getAccessTokenExpiration();
        redisTemplate.opsForValue().set("blacklist:" + accessToken, "true", ttl, TimeUnit.MILLISECONDS);
        kafkaTemplate.send("user-events", Map.of("event", "USER_DELETED", "userId", userId));
        return ApiResponse.success(null, "Account deleted successfully");
    }

    @Transactional(readOnly = true)
    public ApiResponse<UserDto> getCurrentUser(String userId) {
        User user = userRepository.findActiveById(userId).orElseThrow(() -> DatingAppException.notFound("User"));
        return ApiResponse.success(userMapper.toDto(user));
    }

    @Transactional
    public ApiResponse<JwtTokenDto> socialLogin(SocialLoginRequest request) {
        User user;
        if ("GOOGLE".equalsIgnoreCase(request.getProvider())) {
            user = userRepository.findByGoogleId(request.getToken()).orElseThrow(() -> DatingAppException.badRequest("Invalid social token"));
        } else if ("APPLE".equalsIgnoreCase(request.getProvider())) {
            user = userRepository.findByAppleId(request.getToken()).orElseThrow(() -> DatingAppException.badRequest("Invalid social token"));
        } else throw DatingAppException.badRequest("Unsupported provider");
        JwtTokenDto tokens = generateTokens(user);
        saveRefreshToken(user, tokens.getRefreshToken(), request.getDeviceInfo());
        return ApiResponse.success(tokens, "Social login successful");
    }

    private JwtTokenDto generateTokens(User user) {
        Set<String> roles = user.getRoles().stream().map(User.Role::name).collect(java.util.stream.Collectors.toSet());
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        return JwtTokenDto.builder()
                .accessToken(accessToken).refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .refreshExpiresIn(jwtUtil.getRefreshTokenExpiration() / 1000)
                .build();
    }

    private void saveRefreshToken(User user, String token, String deviceInfo) {
        RefreshToken rt = RefreshToken.builder()
                .token(token).user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getRefreshTokenExpiration()))
                .deviceInfo(deviceInfo).revoked(false).build();
        refreshTokenRepository.save(rt);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
