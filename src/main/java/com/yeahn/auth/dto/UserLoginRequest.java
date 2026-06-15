package com.yeahn.auth.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String userId;
    private String password;
}
