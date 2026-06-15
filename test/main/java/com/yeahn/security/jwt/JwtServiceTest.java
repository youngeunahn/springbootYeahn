package com.yeahn.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "unit-test-jwt-secret-value-that-is-long-enough";
    private static final Instant NOW = Instant.parse("2026-06-10T00:00:00Z");

    @Test
    @DisplayName("JWT를 발급하고 사용자 ID와 권한을 검증한다")
    void createAndValidateToken() {
        // [Given] 고정된 현재 시간과 테스트용 secret으로 JWT 서비스를 준비
        JwtService jwtService = new JwtService(new ObjectMapper(), Clock.fixed(NOW, ZoneOffset.UTC), SECRET, 3600);

        // [When] access token을 발급하고 검증
        String token = jwtService.createAccessToken("api_user", "ROLE_USER");
        JwtClaims claims = jwtService.parseAndValidate(token);

        // [Then] payload의 사용자 ID, 권한, 만료 시간이 발급값과 일치
        assertEquals("api_user", claims.subject());
        assertEquals("ROLE_USER", claims.role());
        assertEquals(NOW.plusSeconds(3600), claims.expiresAt());
    }

    @Test
    @DisplayName("만료된 JWT는 거부한다")
    void rejectExpiredToken() {
        // [Given] 1초 뒤 만료되는 JWT를 발급
        JwtService issuer = new JwtService(new ObjectMapper(), Clock.fixed(NOW, ZoneOffset.UTC), SECRET, 1);
        String token = issuer.createAccessToken("api_user", "ROLE_USER");
        JwtService verifier = new JwtService(new ObjectMapper(), Clock.fixed(NOW.plusSeconds(2), ZoneOffset.UTC), SECRET, 1);

        // [When, Then] 만료 시간이 지난 기준으로 검증하면 예외 발생
        assertThrows(IllegalArgumentException.class, () -> verifier.parseAndValidate(token));
    }

    @Test
    @DisplayName("서명이 변조된 JWT는 거부한다")
    void rejectInvalidSignature() {
        // [Given] 정상 JWT 뒤에 임의 문자열을 붙여 서명을 변조
        JwtService jwtService = new JwtService(new ObjectMapper(), Clock.fixed(NOW, ZoneOffset.UTC), SECRET, 3600);
        String token = jwtService.createAccessToken("api_user", "ROLE_USER") + "tampered";

        // [When, Then] 변조된 토큰을 검증하면 예외 발생
        assertThrows(IllegalArgumentException.class, () -> jwtService.parseAndValidate(token));
    }

    @Test
    @DisplayName("JWT header의 alg, typ 값이 다르면 거부한다")
    void rejectInvalidHeader() throws Exception {
        // [Given] 서명은 맞지만 header alg 값이 기대값과 다른 JWT 생성
        ObjectMapper objectMapper = new ObjectMapper();
        JwtService jwtService = new JwtService(objectMapper, Clock.fixed(NOW, ZoneOffset.UTC), SECRET, 3600);
        String token = createToken(objectMapper, Map.of("alg", "none", "typ", "JWT"));

        // [When, Then] header 정책과 다른 토큰을 검증하면 예외 발생
        assertThrows(IllegalArgumentException.class, () -> jwtService.parseAndValidate(token));
    }

    @Test
    @DisplayName("prod 환경에서는 기본 개발용 JWT secret을 거부한다")
    void rejectDefaultDevelopmentSecretInProd() {
        // [Given, When, Then] prod 환경에서 기본 개발용 secret으로 서비스를 만들면 예외 발생
        assertThrows(IllegalArgumentException.class, () -> new JwtService(
                new ObjectMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC),
                "change-this-development-jwt-secret-at-least-32-bytes",
                3600,
                "prod"));
    }

    private String createToken(ObjectMapper objectMapper, Map<String, Object> header) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", "api_user");
        payload.put("role", "ROLE_USER");
        payload.put("iat", NOW.getEpochSecond());
        payload.put("exp", NOW.plusSeconds(3600).getEpochSecond());

        String encodedHeader = encode(objectMapper.writeValueAsBytes(header));
        String encodedPayload = encode(objectMapper.writeValueAsBytes(payload));
        String signingInput = encodedHeader + "." + encodedPayload;
        return signingInput + "." + encode(sign(signingInput));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] sign(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
    }
}
