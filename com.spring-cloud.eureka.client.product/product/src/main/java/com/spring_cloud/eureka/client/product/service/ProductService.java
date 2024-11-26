package com.spring_cloud.eureka.client.product.service;

import com.spring_cloud.eureka.client.product.dto.ProductRequestDto;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import com.spring_cloud.eureka.client.product.entity.Product;
import com.spring_cloud.eureka.client.product.entity.UserRoleEnum;
import com.spring_cloud.eureka.client.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto, String userId) {
        Product product = Product.createProductOf(requestDto, userId);
        Product savedProduct = productRepository.save(product);
        return ProductResponseDto.toProductResponseDtoFrom(savedProduct);
    }

    /**
     * MSA의 엔티티간 연관관계를 표현하려면 서비스 호출을 통해 해야한다.
     * 모놀리식의 경우엔 엔티티가 한 프로젝트 내에 존재했지만 MSA에서는 다 떨어져 있어서 연관관계 표현이 불가능하다.
     * 각 엔티티간 연관관계시 pk에 해당하는 값들만 저장을 해놓고 각 서비스에서 호출을 통해 필요한 엔티티 값을 가져와야한다.
     */
    public Page<ProductResponseDto> getProducts(ProductSearchDto searchDto, String userId, String role, Pageable pageable) {
        UserRoleEnum userRoleEnum;

        try {
            userRoleEnum = UserRoleEnum.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        return productRepository.searchProducts(searchDto, userId, userRoleEnum, pageable);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID productId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or has been deleted"));
        return ProductResponseDto.toProductResponseDtoFrom(product);
    }

    @Transactional
    public ProductResponseDto updateProduct(UUID productId, ProductRequestDto requestDto, String userId) {
        Product product = productRepository.findByIdAndDeletedFalse(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found or has been deleted"));

        product.updateProduct(requestDto, userId);
        Product updatedProduct = productRepository.save(product);

        return ProductResponseDto.toProductResponseDtoFrom(updatedProduct);
    }

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
    }

}
