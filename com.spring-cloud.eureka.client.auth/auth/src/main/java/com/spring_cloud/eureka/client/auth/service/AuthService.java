package com.spring_cloud.eureka.client.auth.service;

import com.spring_cloud.eureka.client.auth.dto.UserRequestDto;
import com.spring_cloud.eureka.client.auth.dto.UserResponseDto;
import com.spring_cloud.eureka.client.auth.entity.User;
import com.spring_cloud.eureka.client.auth.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class AuthService {

    @Value("${spring.application.name}")
    private String issuer;

    @Value("${service.jwt.access-expiration}")
    private Long accessExpiration;

    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(@Value("${service.jwt.secret-key}") String secretKey,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto signUp(UserRequestDto userRequestDto) {

        // ..각종 검증 로직들..

        String password = userRequestDto.getPassword();
        String encodedPassword = passwordEncoder.encode(password);

        User user = User.userRequestDtoEncodedPasswordOf(userRequestDto, encodedPassword);
        userRepository.save(user);

        return UserResponseDto.toUserDtoFrom(user);
    }

    public String signIn(UserRequestDto userRequestDto) {
        User user = userRepository.findByEmail(userRequestDto.getEmail()).orElseThrow(()
                -> new IllegalArgumentException("이메일을 확인해주세요."));

        if (!passwordEncoder.matches(userRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 확인해주세요.");
        }

        return createAccessToken(user);
    }

    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new IllegalIdentifierException("해당 유저의 email이 없습니다."));
        return UserResponseDto.toUserDtoFrom(user);
    }

    public String createAccessToken(User user) {
        return BEARER_PREFIX + Jwts.builder()
                .claim("user_id", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
