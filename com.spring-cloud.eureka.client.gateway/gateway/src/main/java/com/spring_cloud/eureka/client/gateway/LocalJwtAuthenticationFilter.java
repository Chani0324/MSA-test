package com.spring_cloud.eureka.client.gateway;

import com.spring_cloud.eureka.client.gateway.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.gateway.dto.UserResponseDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalJwtAuthenticationFilter implements GlobalFilter {

    private final WebClient.Builder webClientBuilder;

    @Value("${service.jwt.secret-key}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);

        if (token == null || !validateToken(token, exchange)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private boolean validateToken(String token,  ServerWebExchange exchange) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(key)
                    .build().parseSignedClaims(token);
            log.info("#####payload :: " + claimsJws.getPayload().toString());
            Claims claims = claimsJws.getPayload();

            // 비동기로 진행시 권한을 못 가져오는 문제가 있긴 함. 동기로 진행.
            UserResponseDto getUser = getUserByEmail(claims.get("email").toString()).block();

            if (!(getUser != null && getUser.getEmail().equals(claims.get("email").toString()))) {
                return false;
            }

            exchange.getRequest().mutate()
                    .header("X-User_Id", getUser.getId().toString())
                    .header("X-Email", getUser.getEmail())
                    .header("X-Role", getUser.getRole())
                    .build();
//             추가적인 검증 로직 (예: 토큰 만료 여부 확인 등)을 여기에 추가할 수 있습니다.
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
    }

    public Mono<UserResponseDto> getUserByEmail(String email) {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:19091/auth/" + email) // gateway를 통해 한번더 요청이 발생함.. lb를 이용하는 방법을 찾던지, FeginClient 사용 생각해보기.
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponseDto<UserResponseDto>>() {}) // ApiResponseDto로 응답 변환
                .map(ApiResponseDto::getData) // UserResponseDto로 변환
                .flatMap(userResponseDto -> {
                    if (userResponseDto != null) {
                        return Mono.just(userResponseDto); // UserResponseDto 반환
                    } else {
                        return Mono.error(new RuntimeException("User not found")); // 사용자 미발견 시 에러
                    }
                });
    }
}
