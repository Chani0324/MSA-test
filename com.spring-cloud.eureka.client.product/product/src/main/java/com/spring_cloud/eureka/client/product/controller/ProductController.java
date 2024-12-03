package com.spring_cloud.eureka.client.product.controller;

import com.spring_cloud.eureka.client.product.annotation.CountApi;
import com.spring_cloud.eureka.client.product.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductRequestDto;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import com.spring_cloud.eureka.client.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@RefreshScope
@RequestMapping("/products")
@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductService productService;
    private final RedisTemplate<String, Integer> countRedisTemplate;

    @Value("${server.port}")
    private String serverPort;

    @CountApi
    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createProduct(@RequestBody ProductRequestDto productRequestDto,
                                                           @RequestHeader(value = "X-User_id", required = true) String userId,
                                                           @RequestHeader(value = "X-Role", required = true) String role
                                                           ) {
        checkManager(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(3000,
                        "제품을 등록하였습니다.",
                        productService.createProduct(productRequestDto, userId)));
    }

    @CountApi
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ProductResponseDto>>> getProducts(ProductSearchDto searchDto,
                                                                                @RequestHeader(value = "X-User_id", required = true) String userId,
                                                                                @RequestHeader(value = "X-Role", required = true) String role,
                                                                                Pageable pageable) {
        log.info(serverPort);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(3100,
                        "제품들을 조회합니다.",
                        productService.getProducts(searchDto, userId, role, pageable)));
    }

    @CountApi
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponseDto<? extends ProductResponseDto>> getProductById(@PathVariable UUID productId) {
        log.info(serverPort);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(3101,
                        "해당 제품을 조회합니다.",
                        productService.getProductById(productId)));
    }

    @CountApi
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> updateProduct(@PathVariable UUID productId,
                                            @RequestBody ProductRequestDto productRequestDto,
                                            @RequestHeader(value = "X-User_Id", required = true) String userId,
                                            @RequestHeader(value = "X-Role", required = true) String role) {
        checkManager(role);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.response(3200,
                        "등록된 제품을 수정하였습니다.",
                        productService.updateProduct(productId, productRequestDto, userId)));
    }

    @CountApi
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponseDto<?>> deleteProduct(@PathVariable UUID productId,
                              @RequestHeader(value = "X-User_Id", required = true) String userId,
                              @RequestHeader(value = "X-Role", required = true) String role) {
        checkManager(role);
        productService.deleteProduct(productId, userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.response(3300,
                        "등록된 제품을 삭제하였습니다.",
                        ""));
    }

    @GetMapping("/{productId}/reduceQuantity")
    public void reduceProductQuantity(@PathVariable UUID productId, @RequestParam int quantity) {
        productService.reduceProductQuantity(productId, quantity);
    }

    private void checkManager(String role) {
        if (!"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access denied. User role is not MANAGER.");
        }
    }

}
