package com.spring_cloud.eureka.client.auth.controller;

import com.spring_cloud.eureka.client.auth.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.auth.dto.UserRequestDto;
import com.spring_cloud.eureka.client.auth.dto.UserResponseDto;
import com.spring_cloud.eureka.client.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${server.port}")
    private String serverPort;

    @PostMapping("/signIn")
    public ResponseEntity<ApiResponseDto<?>> createAuthToken(@RequestBody UserRequestDto userRequestDto) {
        String token = authService.signIn(userRequestDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", token);
        headers.add("Server-Port", serverPort);

        return ResponseEntity.ok()
                .headers(headers)
                .body(ApiResponseDto.response(2000,
                        "로그인에 성공하였습니다.",
                        ""));
    }

    @PostMapping("/signUp")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> signUp(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(2100,
                        "회원가입에 성공하였습니다."
                        , authService.signUp(userRequestDto)));
    }

    @GetMapping("/{email}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUser(@PathVariable String email) {
        log.info(email);
        return ResponseEntity.ok()
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(2001,
                        "유저 조회 성공.",
                        authService.getUserByEmail(email)));
    }
}
