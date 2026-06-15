package com.yeahn.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserTokenResponse {
    private String tokenType;
    private String accessToken;
    private long expiresIn;
    private String userId;
    private String userName;
    private String role;
}
