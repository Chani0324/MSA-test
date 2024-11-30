package com.spring_cloud.eureka.client.gateway;

import com.spring_cloud.eureka.client.gateway.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.gateway.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/auth/{email}")
    ResponseEntity<ApiResponseDto<UserResponseDto>> getAuthByEmail(@PathVariable("email") String email);
}
