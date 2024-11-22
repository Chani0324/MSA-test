package com.spring_cloud.eureka.client.auth.dto;

import com.spring_cloud.eureka.client.auth.entity.UserRoleEnum;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserRequestDto {
    private String email;
    private String username;
    private String password;
    private UserRoleEnum role;
}
