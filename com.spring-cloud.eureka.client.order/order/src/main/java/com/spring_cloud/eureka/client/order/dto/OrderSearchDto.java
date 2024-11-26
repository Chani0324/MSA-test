package com.spring_cloud.eureka.client.order.dto;

import com.spring_cloud.eureka.client.order.entity.OrderStatus;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Getter
public class OrderSearchDto {
    private OrderStatus status;
    private List<OrderProductListDto> orderProductListDto;
    private String sortBy;
    private Pageable pageable;
}
