package com.spring_cloud.eureka.client.order.annotation;

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

    private final RedisTemplate<String, String> countRedisTemplate;
    private final HttpServletRequest request;

    @After("@annotation(CountApi)")
    public void countApiCall(JoinPoint joinPoint) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String key = method + " : " + uri;

        countRedisTemplate.opsForZSet().incrementScore("api_call_counts", key, 1);
    }
}
