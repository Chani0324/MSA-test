package com.spring_cloud.eureka.client.order.repository;

import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {
    Page<OrderResponseDto> searchOrders(OrderSearchDto searchDto, Pageable pageable, String role, String userId);
}
