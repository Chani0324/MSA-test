package com.spring_cloud.eureka.client.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring_cloud.eureka.client.product.dto.ProductResponseDto;
import com.spring_cloud.eureka.client.product.dto.ProductSearchDto;
import com.spring_cloud.eureka.client.product.dto.QProductResponseDto;
import com.spring_cloud.eureka.client.product.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static com.spring_cloud.eureka.client.product.entity.QProduct.product;


@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductResponseDto> searchProducts(ProductSearchDto searchDto, String userId, UserRoleEnum role, Pageable pageable) {
        List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);

//         동적 쿼리 작성
        List<ProductResponseDto> results = queryFactory
                .select(new QProductResponseDto(
                        product.id.as("productId"),
                        product.name,
                        product.description,
                        product.price,
                        product.quantity,
                        product.createdAt,
                        product.createdBy,
                        product.updatedAt,
                        product.updatedBy
                ))
                .from(product)
                .where(isMemberOrManager(userId, role),
                        nameContains(searchDto.getName()),
                        descriptionContains(searchDto.getDescription()),
                        priceBetween(searchDto.getMinPrice(), searchDto.getMaxPrice()),
                        quantityBetween(searchDto.getMinQuantity(), searchDto.getMaxQuantity()))
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectOne()
                .from(product)
                .where(nameContains(searchDto.getName()),
                        descriptionContains(searchDto.getDescription()),
                        priceBetween(searchDto.getMinPrice(), searchDto.getMaxPrice()),
                        quantityBetween(searchDto.getMinQuantity(), searchDto.getMaxQuantity()))
                .fetch()
                .size(); // 결과의 총 개수

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression isMemberOrManager(String userId, UserRoleEnum role) {
        return role == UserRoleEnum.MEMBER ? product.deleted.eq(false) : null;
    }

    private BooleanExpression nameContains(String name) {
        return name != null ? product.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression descriptionContains(String description) {
        return description != null ? product.description.containsIgnoreCase(description) : null;
    }

    private BooleanExpression priceBetween(Double minPrice, Double maxPrice) {
        if (minPrice != null && maxPrice != null) {
            return product.price.between(minPrice, maxPrice);
        } else if (minPrice != null) {
            return product.price.goe(minPrice);
        } else if (maxPrice != null) {
            return product.price.loe(maxPrice);
        } else {
            return null;
        }
    }

    private BooleanExpression quantityBetween(Integer minQuantity, Integer maxQuantity) {
        if (minQuantity != null && maxQuantity != null) {
            return product.quantity.between(minQuantity, maxQuantity);
        } else if (minQuantity != null) {
            return product.quantity.goe(minQuantity);
        } else if (maxQuantity != null) {
            return product.quantity.loe(maxQuantity);
        } else {
            return null;
        }
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        pageable.getSort();
        for (Sort.Order sortOrder : pageable.getSort()) {
            com.querydsl.core.types.Order direction = sortOrder.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
            switch (sortOrder.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, product.createdAt));
                case "price" -> orders.add(new OrderSpecifier<>(direction, product.price));
                case "quantity" -> orders.add(new OrderSpecifier<>(direction, product.quantity));
                case "name" -> orders.add(new OrderSpecifier<>(direction, product.name));
                default -> {
                }
            }
        }

        return orders;
    }
}
