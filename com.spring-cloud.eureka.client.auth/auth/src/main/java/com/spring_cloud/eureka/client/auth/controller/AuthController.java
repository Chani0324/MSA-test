package com.spring_cloud.eureka.client.auth.controller;

import com.spring_cloud.eureka.client.auth.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.auth.dto.UserRequestDto;
import com.spring_cloud.eureka.client.auth.dto.UserResponseDto;
import com.spring_cloud.eureka.client.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signIn")
    public ResponseEntity<ApiResponseDto<?>> createAuthToken(@RequestBody UserRequestDto userRequestDto) {
        String token = authService.signIn(userRequestDto);
        return ResponseEntity.ok()
                .header("Authorization", token)
                .body(ApiResponseDto.response(2000,
                        "로그인에 성공하였습니다.",
                        ""));
    }

    @PostMapping("/signUp")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> signUp(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(2100,
                        "회원가입에 성공하였습니다."
                        , authService.signUp(userRequestDto)));
    }

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUser(@PathVariable String email) {
        log.info(email);
        return ResponseEntity.ok()
                .body(ApiResponseDto.response(2001,
                        "유저 조회 성공.",
                        authService.getUserByEmail(email)));
    }
}
