package com.spring_cloud.eureka.client.product.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponseDto<T> {
    private int serviceCode;
    private String message;
    private T data;

    public static <T> ApiResponseDto<T> response(int serviceCode, String message, T data) {
        return ApiResponseDto.<T>builder()
                .serviceCode(serviceCode)
                .message(message)
                .data(data)
                .build();
    }
}

