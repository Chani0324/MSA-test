package com.spring_cloud.eureka.client.order.controller;

import com.spring_cloud.eureka.client.order.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderRequestDto;
import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderSearchDto;
import com.spring_cloud.eureka.client.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> createOrder(@RequestBody OrderRequestDto orderRequestDto,
                                                                        @RequestHeader(value = "X-User_Id", required = true) String userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(4000,
                        "주문을 등록하였습니다.",
                        orderService.createOrder(orderRequestDto, userId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<OrderResponseDto>>> getOrders(OrderSearchDto searchDto, Pageable pageable,
                                            @RequestHeader(value = "X-User_Id", required = true) String userId,
                                            @RequestHeader(value = "X-Role", required = true) String role) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.response(4100,
                        "주문을 조회합니다.",
                        orderService.getOrders(searchDto, pageable, role, userId)));
    }

    @GetMapping("/{orderId}")
    public OrderResponseDto getOrderById(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId);
    }

    @PutMapping("/{orderId}")
    public OrderResponseDto updateOrder(@PathVariable UUID orderId,
                                        @RequestBody OrderRequestDto orderRequestDto,
                                        @RequestHeader(value = "X-User_Id", required = true) String userId,
                                        @RequestHeader(value = "X-Role", required = true) String role) {
        return orderService.updateOrder(orderId, orderRequestDto, userId);
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable UUID orderId, @RequestParam String deletedBy) {
        orderService.deleteOrder(orderId, deletedBy);
    }
}
