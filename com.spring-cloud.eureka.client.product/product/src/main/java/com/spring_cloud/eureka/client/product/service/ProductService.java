package com.spring_cloud.eureka.client.product.service;

import com.spring_cloud.eureka.client.product.dto.ProductRequestDto;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import com.spring_cloud.eureka.client.product.entity.Product;
import com.spring_cloud.eureka.client.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<ProductResponseDto> getProducts(ProductSearchDto searchDto, Pageable pageable) {
        return productRepository.searchProducts(searchDto, pageable);
    }

}
