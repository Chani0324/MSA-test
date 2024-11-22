package com.spring_cloud.eureka.client.auth.controller;

import com.spring_cloud.eureka.client.auth.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.auth.dto.UserRequestDto;
import com.spring_cloud.eureka.client.auth.dto.UserResponseDto;
import com.spring_cloud.eureka.client.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/signIn")
    public ResponseEntity<ApiResponseDto<?>> createAuthToken(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok()
                .header("Authorization", authService.signIn(userRequestDto))
                .body(ApiResponseDto.response(2000,
                        "로그인에 성공하였습니다.",
                        null));
    }

    @PostMapping("/auth/signUp")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> signUp(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(2100,
                        "회원가입에 성공하였습니다."
                        , authService.signUp(userRequestDto)));
    }
}
