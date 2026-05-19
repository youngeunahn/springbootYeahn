package com.yeahn.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto<T> {
    private String status;
    private String message;
    private T data;

    public static <T> ResponseDto<T> success(T data) {
        return ResponseDto.<T>builder()
                .status("SUCCESS")
                .message("Request processed successfully.")
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> success(String message, T data) {
        return ResponseDto.<T>builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> fail(String message) {
        return ResponseDto.<T>builder()
                .status("FAIL")
                .message(message)
                .build();
    }
}
