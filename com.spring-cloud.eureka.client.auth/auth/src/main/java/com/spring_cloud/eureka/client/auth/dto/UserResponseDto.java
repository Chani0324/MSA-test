package com.spring_cloud.eureka.client.auth.dto;

import com.spring_cloud.eureka.client.auth.entity.User;
import com.spring_cloud.eureka.client.auth.entity.UserRoleEnum;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponseDto {
    private String email;
    private String username;
    private UserRoleEnum role;

    public static UserResponseDto userFrom(User user) {
        return UserResponseDto.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
