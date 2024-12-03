package com.spring_cloud.eureka.client.product.service;

import com.spring_cloud.eureka.client.product.dto.ProductRankDto;
import com.spring_cloud.eureka.client.product.dto.ProductRequestDto;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import com.spring_cloud.eureka.client.product.entity.Product;
import com.spring_cloud.eureka.client.product.entity.UserRoleEnum;
import com.spring_cloud.eureka.client.product.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RedisTemplate<String, ProductRankDto> rankOps;

    @CacheEvict(cacheNames = "getProductAllCache", allEntries = true)
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, String userId) {
        Product product = Product.ofDtoAndEmail(requestDto, userId);
        Product savedProduct = productRepository.save(product);
        return ProductResponseDto.fromEntity(savedProduct);
    }

    /**
     * MSA의 엔티티간 연관관계를 표현하려면 서비스 호출을 통해 해야한다.
     * 모놀리식의 경우엔 엔티티가 한 프로젝트 내에 존재했지만 MSA에서는 다 떨어져 있어서 연관관계 표현이 불가능하다.
     * 각 엔티티간 연관관계시 pk에 해당하는 값들만 저장을 해놓고 각 서비스에서 호출을 통해 필요한 엔티티 값을 가져와야한다.
     */
    @Cacheable(cacheNames = "getProductAllCache", key = "getMethodName()")
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(ProductSearchDto searchDto, String userId, String role, Pageable pageable) {
        UserRoleEnum userRoleEnum;

        try {
            userRoleEnum = UserRoleEnum.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        return productRepository.searchProducts(searchDto, userId, userRoleEnum, pageable);
    }

    @CircuitBreaker(name = "ProductService-getProductById", fallbackMethod = "fallbackInGetProductById")
    @Cacheable(cacheNames = "getProductByProductIdCache", key = "args[0]")
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID productId) {
        log.info("productId = : {}", productId.toString());
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or has been deleted"));
        return ProductResponseDto.fromEntity(product);
    }

    @CachePut(cacheNames = "getProductByProductIdCache", key = "args[0]")
    @CacheEvict(cacheNames = "getProductAllCache", allEntries = true)
    @Transactional
    public ProductResponseDto updateProduct(UUID productId, ProductRequestDto requestDto, String userId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or has been deleted"));

        product.updateProduct(requestDto, userId);
        Product updatedProduct = productRepository.save(product);

        return ProductResponseDto.fromEntity(updatedProduct);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "getProductByProductIdCache", key = "args[0]"),
            @CacheEvict(cacheNames = "getProductAllCache", allEntries = true)
    })
    @Transactional
    public void deleteProduct(UUID productId, String deletedBy) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or has been deleted"));
        product.softDeleteProduct(deletedBy);
        productRepository.save(product);
    }

    @Transactional
    public void reduceProductQuantity(UUID productId, int quantity) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (product.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough quantity for product ID: " + productId);
        }

        product.reduceQuantity(quantity);
        rankOps.opsForZSet().incrementScore(
                "soldRanks",
                ProductRankDto.fromEntity(product),
                quantity
        );
    }

    public List<ProductRankDto> getMostSold() {
        Set<ProductRankDto> ranks = rankOps.opsForZSet().reverseRange("soldRanks", 0, 9);
        if (ranks == null) return Collections.emptyList();
        return ranks.stream().toList();
    }

    // fallback 메서드는 주 메서드와 동일한 매개변수, 반환 타입을 가져야 한다.
    // 서킷브레이커 메서드들은 따로 class를 만들어 관리하는게 좋을 듯.
    public ProductResponseDto fallbackInGetProductById(UUID productId, Throwable throwable) {
        log.error(throwable.getMessage());

        if (circuitBreakerRegistry.circuitBreaker("ProductService-getProductById").getState().toString().equals("OPEN")) {
            return ProductResponseDto.builder()
                    .name("circuit breaker is open state.")
                    .build();
        }

        return ProductResponseDto.builder()
                .name("empty or deleted product")
                .build();
    }
    // 등록할 서킷브레이커들

    @PostConstruct
    public void registerEventListeners() {
        registerEventListener("ProductService-getProductById");
    }

    public void registerEventListener(String circuitBreakerName) {
        circuitBreakerRegistry.circuitBreaker(circuitBreakerName).getEventPublisher()
                .onStateTransition(event -> log.info("#######CircuitBreaker State Transition: {}", event)) // 상태 전환 이벤트 리스너
                .onFailureRateExceeded(event -> log.info("#######CircuitBreaker Failure Rate Exceeded: {}", event)) // 실패율 초과 이벤트 리스너
                .onCallNotPermitted(event -> log.info("#######CircuitBreaker Call Not Permitted: {}", event)) // 호출 차단 이벤트 리스너
                .onError(event -> log.info("#######CircuitBreaker Error: {}", event)); // 오류 발생 이벤트 리스너
    }
}
