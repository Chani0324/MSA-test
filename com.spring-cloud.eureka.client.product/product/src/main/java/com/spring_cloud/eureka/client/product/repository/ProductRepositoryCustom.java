package com.spring_cloud.eureka.client.product.repository;

import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto, Pageable pageable);
}
