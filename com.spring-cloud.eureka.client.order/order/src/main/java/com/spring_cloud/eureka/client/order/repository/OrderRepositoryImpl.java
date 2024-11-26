package com.spring_cloud.eureka.client.order.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderSearchDto;
import com.spring_cloud.eureka.client.order.entity.Order;
import com.spring_cloud.eureka.client.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static com.spring_cloud.eureka.client.order.entity.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<OrderResponseDto> searchOrders(OrderSearchDto searchDto, Pageable pageable, String role, String userId) {

        List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);

        List<Order> results = queryFactory
                .selectFrom(order)
                .where(
                        statusEq(searchDto.getStatus()),
                        userCheck(role, userId)
                )
                .orderBy(orders.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<OrderResponseDto> content = results.stream()
                .map(Order::toResponseDto)
                .toList();

        long total = queryFactory
                .selectOne()
                .from(order)
                .where(
                        statusEq(searchDto.getStatus()),
                        userCheck(role, userId))
                .fetch()
                .size();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression statusEq(OrderStatus status) {
        return status != null ? order.status.eq(status) : null;
    }

    private BooleanExpression userCheck(String role, String userId) {
        return role.equals("MEMBER") ? order.createdBy.eq(userId) : null;
    }

//    private BooleanExpression orderProductListIn(List<OrderProductList> orderProductList) {
//        return orderProductList != null && !orderProductList.isEmpty() ? order.orderProductList.any().in(orderProductList) : null;
//    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        pageable.getSort();
        for (Sort.Order sortOrder : pageable.getSort()) {
            com.querydsl.core.types.Order direction = sortOrder.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
            switch (sortOrder.getProperty()) {
                case "createdAt" -> orders.add(new OrderSpecifier<>(direction, order.createdAt));
                case "status" -> orders.add(new OrderSpecifier<>(direction, order.status));
                default -> {
                }
            }
        }

        return orders;
    }
}
