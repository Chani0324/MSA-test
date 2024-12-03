package com.spring_cloud.eureka.client.order.service;

import com.spring_cloud.eureka.client.order.client.product.ProductClient;
import com.spring_cloud.eureka.client.order.client.product.ProductResponseDto;
import com.spring_cloud.eureka.client.order.dto.*;
import com.spring_cloud.eureka.client.order.entity.Order;
import com.spring_cloud.eureka.client.order.entity.OrderProductList;
import com.spring_cloud.eureka.client.order.entity.OrderStatus;
import com.spring_cloud.eureka.client.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, OrderResponseDto> orderTemplate;

    /**
     * 나중에 TaskScheduler를 이용해서 주문 취소가 없을 시 5분마나 넣는다던지, 결제를 확인 후 넣는다던지.. 등등
     * 다양한 메소드 필요할듯?
     * cache를 이용해 bulk insert를 할때 UUID나 createdAt같은게 생기니까
     * return에서 null이 뜨는 값들은 dto를 따로 만들어서 없애고 주는게 여기선 맞을지도.
     */
    @CircuitBreaker(name = "OrderService-createOrder", fallbackMethod = "fallbackInCreateOrder")
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto requestDto, String userId) {
        Order order = Order.createOrderFrom(userId);
        for (OrderProductListDto orderProductList : requestDto.getOrderList()) {
            ResponseEntity<ApiResponseDto<ProductResponseDto>> responseEntity = productClient.getProduct(orderProductList.getProductId());
            ProductResponseDto product = Objects.requireNonNull(responseEntity.getBody()).getData();
            validateProductQuantity(product, orderProductList.getProductId());

            productClient.reduceProductQuantity(orderProductList.getProductId(), orderProductList.getQuantity());
            order.addOrderProduct(OrderProductList.fromDto(orderProductList));
        }

        OrderResponseDto orderResponseDto = OrderResponseDto.fromEntity(order);
        orderTemplate.opsForList().rightPush("orderCache::behind", orderResponseDto);
        return orderResponseDto;
    }

    @Cacheable(cacheNames = "getOrderAllCache", key = "getMethodName()")
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getOrders(OrderSearchDto searchDto, Pageable pageable, String role, String userId) {
        log.info("============into method=============");
        return orderRepository.searchOrders(searchDto, pageable, role, userId);
    }

    @Cacheable(cacheNames = "getOrderByOrderIdCache", key = "args[0]")
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID orderId) {
        Order order = orderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));
        return OrderResponseDto.fromEntity(order);
    }

    @CachePut(cacheNames = "getOrderByOrderIdCache", key = "args[0]")
    @CacheEvict(cacheNames = "getOrderAllCache", allEntries = true)
    @Transactional
    public OrderResponseDto updateOrder(UUID orderId, OrderRequestDto requestDto, String userId) {
        Order order = orderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));

        List<OrderProductList> orderProductLists = new ArrayList<>();

        for (OrderProductListDto orderProductList : requestDto.getOrderList()) {
            orderProductLists.add(OrderProductList.fromDto(orderProductList));
        }
        order.updateOrder(orderProductLists, userId, OrderStatus.valueOf(requestDto.getStatus()));
        Order updatedOrder = orderRepository.save(order);

        return OrderResponseDto.fromEntity(updatedOrder);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "getOrderByOrderIdCache", key = "args[0]"),
            @CacheEvict(cacheNames = "getOrderAllCache", allEntries = true)
    })
    @Transactional
    public void deleteOrder(UUID orderId, String deletedBy) {
        Order order = orderRepository.findByOrderIdAndDeletedFalse(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or has been deleted"));
        order.softDelete(deletedBy);
        orderRepository.save(order);
    }

    @CacheEvict(cacheNames = "getOrderAllCache", allEntries = true)
    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void insertOrders() {
        boolean exists = Optional.of(orderTemplate.hasKey("orderCache::behind"))
                .orElse(false);
        if (!exists) {
            log.info("no orders in cache");
            return;
        }

        ListOperations<String, OrderResponseDto> orderOps = orderTemplate.opsForList();

        List<OrderResponseDto> orderResponseDtos = orderOps.range("orderCache::behind", 0, -1);
        List<Order> ordersToSave = orderResponseDtos.stream()
                .map(orderResponseDto -> {
                    Order order = Order.fromResponseDto(orderResponseDto);
                    orderResponseDto.getOrderProductList().forEach(orderProductListDto -> order.addOrderProduct(OrderProductList.fromDto(orderProductListDto)));
                    return order;
                })
                .toList();
        orderRepository.saveAll(ordersToSave);
        orderTemplate.delete("orderCache::behind");
    }

    private void validateProductQuantity(ProductResponseDto product, UUID productId) {
        if (product.getName().equals("empty or deleted product")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found.");
        }

        if (product.getName().equals("circuit breaker is open state.")) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service is currently unavailable");
        }

        Integer quantity = product.getQuantity();

        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product with ID " + productId + " is out of stock.");
        }
    }

    @PostConstruct
    public void registerEventListeners() {
        registerEventListener("OrderService-createOrder");
    }

    public void registerEventListener(String circuitBreakerName) {
        circuitBreakerRegistry.circuitBreaker(circuitBreakerName).getEventPublisher()
                .onStateTransition(event -> log.info("#######CircuitBreaker State Transition: {}", event)) // 상태 전환 이벤트 리스너
                .onFailureRateExceeded(event -> log.info("#######CircuitBreaker Failure Rate Exceeded: {}", event)) // 실패율 초과 이벤트 리스너
                .onCallNotPermitted(event -> log.info("#######CircuitBreaker Call Not Permitted: {}", event)) // 호출 차단 이벤트 리스너
                .onError(event -> log.info("#######CircuitBreaker Error: {}", event)); // 오류 발생 이벤트 리스너
    }

    public OrderResponseDto fallbackInCreateOrder(OrderRequestDto requestDto, String userId, Throwable throwable) {
        log.error(throwable.getMessage());

        return OrderResponseDto.builder()
                .build();
    }
}
