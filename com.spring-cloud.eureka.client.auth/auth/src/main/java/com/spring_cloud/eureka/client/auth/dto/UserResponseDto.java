package com.spring_cloud.eureka.client.auth.dto;

import com.spring_cloud.eureka.client.auth.entity.User;
import com.spring_cloud.eureka.client.auth.entity.UserRoleEnum;
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

    public static UserResponseDto toUserDtoFrom(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().toString())
                .build();
    }
}
