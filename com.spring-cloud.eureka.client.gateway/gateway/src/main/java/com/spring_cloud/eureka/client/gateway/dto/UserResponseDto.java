package com.spring_cloud.eureka.client.gateway.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {

    private UUID id;
    private String email;
    private String username;
    private String role;
}
