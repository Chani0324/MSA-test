package com.spring_cloud.eureka.client.product.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.spring_cloud.eureka.client.product.entity.QProduct.product;


@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto, Pageable pageable) {

        // 동적 쿼리 작성
//        queryFactory
//                .selectFrom(product)
//                .where(
//
//                )
    return null;
    }
}
