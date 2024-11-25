package com.spring_cloud.eureka.client.auth.entity;

import com.spring_cloud.eureka.client.auth.dto.UserRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users",
        indexes = @Index(name = "idx_email", columnList = "email"))
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    /**
     * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
     */
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String email;

    private String username;
    private String password;
    private UserRoleEnum role;

    /**
     * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
     */


    /**
     * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
     */


    /**
     * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
     */


    /**
     * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
     */
    public static User userRequestDtoEncodedPasswordOf(UserRequestDto userRequestDto, String encodedPassword) {
        return User.builder()
                .email(userRequestDto.getEmail())
                .username(userRequestDto.getUsername())
                .role(userRequestDto.getRole())
                .password(encodedPassword)
                .build();
    }
}
