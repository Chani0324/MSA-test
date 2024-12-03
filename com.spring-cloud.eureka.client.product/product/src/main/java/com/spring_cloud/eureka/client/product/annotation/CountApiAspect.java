package com.spring_cloud.eureka.client.product.annotation;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Aspect
@RequiredArgsConstructor
@Component
public class CountApiAspect {

    private final RedisTemplate<String, Integer> countRedisTemplate;
    private final HttpServletRequest request;

    @After("@annotation(CountApi)")
    public void countApiCall(JoinPoint joinPoint) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        countRedisTemplate.opsForValue().increment(method + " : " + uri);
    }
}
