package com.spring_cloud.eureka.client.order.controller;

import com.spring_cloud.eureka.client.order.annotation.CountApi;
import com.spring_cloud.eureka.client.order.dto.ApiResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderRequestDto;
import com.spring_cloud.eureka.client.order.dto.OrderResponseDto;
import com.spring_cloud.eureka.client.order.dto.OrderSearchDto;
import com.spring_cloud.eureka.client.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.port}")
    private String serverPort;

    @CountApi
    @PostMapping
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> createOrder(@RequestBody OrderRequestDto orderRequestDto,
                                                                        @RequestHeader(value = "X-User_Id", required = true) String userId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(4000,
                        "주문을 등록하였습니다.",
                        orderService.createOrder(orderRequestDto, userId)));
    }

    @CountApi
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<OrderResponseDto>>> getOrders(OrderSearchDto searchDto, Pageable pageable,
                                            @RequestHeader(value = "X-User_Id", required = true) String userId,
                                            @RequestHeader(value = "X-Role", required = true) String role) {
        return ResponseEntity.status(HttpStatus.OK)
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(4100,
                        "주문을 조회합니다.",
                        orderService.getOrders(searchDto, pageable, role, userId)));
    }

    @CountApi
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> getOrderById(@PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK)
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(4101,
                        "해당 주문을 조회하였습니다.",
                        orderService.getOrderById(orderId)));
    }

    @CountApi
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<OrderResponseDto>> updateOrder(@PathVariable UUID orderId,
                                        @RequestBody OrderRequestDto orderRequestDto,
                                        @RequestHeader(value = "X-User_Id", required = true) String userId,
                                        @RequestHeader(value = "X-Role", required = true) String role) {
        return ResponseEntity.status(HttpStatus.OK)
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(4200,
                        "해당 주문을 수정하였습니다.",
                        orderService.updateOrder(orderId, orderRequestDto, userId)));
    }

    @CountApi
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<?>> deleteOrder(@PathVariable UUID orderId, @RequestParam String deletedBy) {
        orderService.deleteOrder(orderId, deletedBy);
        return ResponseEntity.status(HttpStatus.OK)
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(4300,
                        "해당 주문을 삭제하였습니다.",
                        ""));
    }

    @CountApi
    @PostMapping("/fail")
    public ResponseEntity<ApiResponseDto<?>> failOrders() {
        return ResponseEntity.status(HttpStatus.OK)
                .header("Server-Port", serverPort)
                .body(ApiResponseDto.response(4999,
                        "잠시 후에 주문 추가를 요청해주세요.",
                        ""));
    }
}
