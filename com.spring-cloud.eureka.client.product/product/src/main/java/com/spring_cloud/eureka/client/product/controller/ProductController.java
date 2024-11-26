package com.spring_cloud.eureka.client.product.controller;

import com.spring_cloud.eureka.client.product.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductRequestDto;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import com.spring_cloud.eureka.client.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RefreshScope
@RequestMapping("/products")
@RequiredArgsConstructor
@RestController
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> createProduct(@RequestBody ProductRequestDto productRequestDto,
                                                           @RequestHeader(value = "X-User_id", required = true) String userId,
                                                           @RequestHeader(value = "X-Role", required = true) String role
                                                           ) {
        if (!"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. User role is not MANAGER.");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(3000,
                        "제품을 등록하였습니다.",
                        productService.createProduct(productRequestDto, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ProductResponseDto>>> getProducts(ProductSearchDto searchDto,
                                                                                @RequestHeader(value = "X-User_id", required = true) String userId,
                                                                                @RequestHeader(value = "X-Role", required = true) String role,
                                                                                Pageable pageable) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(3000,
                        "제품들을 조회합니다.",
                        productService.getProducts(searchDto, userId, role, pageable)));
    }

    @GetMapping("/{productId}")
    public ProductResponseDto getProductById(@PathVariable UUID productId) {
        return productService.getProductById(productId);
    }

    @PutMapping("/{productId}")
    public ProductResponseDto updateProduct(@PathVariable UUID productId,
                                            @RequestBody ProductRequestDto orderRequestDto,
                                            @RequestHeader(value = "X-User_Id", required = true) String userId,
                                            @RequestHeader(value = "X-Role", required = true) String role) {
        if (!"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. User role is not MANAGER.");
        }
        return productService.updateProduct(productId, orderRequestDto, userId);
    }

    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable UUID productId,
                              @RequestHeader(value = "X-User_Id", required = true) String userId,
                              @RequestHeader(value = "X-Role", required = true) String role) {
        if (!"MANAGER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied. User role is not MANAGER.");
        }
        productService.deleteProduct(productId, userId);
    }

    @GetMapping("/{productId}/reduceQuantity")
    public void reduceProductQuantity(@PathVariable UUID productId, @RequestParam int quantity) {
        productService.reduceProductQuantity(productId, quantity);
    }
}
