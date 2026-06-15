package com.yeahn.security.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String DEFAULT_DEVELOPMENT_SECRET = "change-this-development-jwt-secret-at-least-32-bytes";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String JWT_ALGORITHM = "HS256";
    private static final String JWT_TYPE = "JWT";

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final byte[] secret;
    private final long expirationSeconds;

    @Autowired
    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret:${JWT_SECRET:change-this-development-jwt-secret-at-least-32-bytes}}") String secret,
            @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds,
            @Value("${spring.profiles.active:local}") String springEnvironment) {
        this(objectMapper, Clock.systemUTC(), secret, expirationSeconds, springEnvironment);
    }

    JwtService(ObjectMapper objectMapper, Clock clock, String secret, long expirationSeconds) {
        this(objectMapper, clock, secret, expirationSeconds, "local");
    }

    JwtService(ObjectMapper objectMapper, Clock clock, String secret, long expirationSeconds, String springEnvironment) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("app.jwt.secret은 32바이트 이상이어야 합니다.");
        }
        if ("prod".equalsIgnoreCase(springEnvironment) && DEFAULT_DEVELOPMENT_SECRET.equals(secret)) {
            throw new IllegalArgumentException("prod 환경에서는 환경변수 JWT_SECRET를 반드시 설정해야 합니다.");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("app.jwt.expiration-seconds는 양수여야 합니다.");
        }
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String createAccessToken(String userId, String role) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", JWT_ALGORITHM);
        header.put("typ", JWT_TYPE);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId);
        payload.put("role", role);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        try {
            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signingInput = encodedHeader + "." + encodedPayload;
            return signingInput + "." + encode(sign(signingInput));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JWT.", e);
        }
    }

    public JwtClaims parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("JWT 형식이 올바르지 않습니다.");
            }

            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSignature = sign(signingInput);
            byte[] actualSignature = BASE64_URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                throw new IllegalArgumentException("JWT 서명이 올바르지 않습니다.");
            }

            Map<String, Object> header = objectMapper.readValue(
                    BASE64_URL_DECODER.decode(parts[0]),
                    new TypeReference<Map<String, Object>>() {
                    });
            validateHeader(header);

            Map<String, Object> payload = objectMapper.readValue(
                    BASE64_URL_DECODER.decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {
                    });

            String subject = (String) payload.get("sub");
            String role = (String) payload.get("role");
            Instant issuedAt = Instant.ofEpochSecond(asLong(payload.get("iat")));
            Instant expiresAt = Instant.ofEpochSecond(asLong(payload.get("exp")));
            if (subject == null || subject.isBlank() || role == null || role.isBlank()) {
                throw new IllegalArgumentException("JWT 필수 클레임이 누락되었습니다.");
            }
            if (!expiresAt.isAfter(Instant.now(clock))) {
                throw new IllegalArgumentException("JWT가 만료되었습니다.");
            }

            return new JwtClaims(subject, role, issuedAt, expiresAt);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT가 올바르지 않습니다.", e);
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private String encodeJson(Map<String, Object> value) throws Exception {
        return encode(objectMapper.writeValueAsBytes(value));
    }

    private String encode(byte[] value) {
        return BASE64_URL_ENCODER.encodeToString(value);
    }

    private byte[] sign(String signingInput) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
    }

    private void validateHeader(Map<String, Object> header) {
        if (!JWT_ALGORITHM.equals(header.get("alg")) || !JWT_TYPE.equals(header.get("typ"))) {
            throw new IllegalArgumentException("JWT header가 올바르지 않습니다.");
        }
    }

    private long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("JWT 숫자 클레임이 올바르지 않습니다.");
    }
}
