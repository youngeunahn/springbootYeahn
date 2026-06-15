package com.yeahn.security.jwt;

import java.time.Instant;

public record JwtClaims(
        String subject,   // sub: 사용자 ID
        String role,      // role: 사용자 권한
        Instant issuedAt, // iat: 토큰 발급 시각
        Instant expiresAt // exp: 토큰 만료 시각
) {
}
