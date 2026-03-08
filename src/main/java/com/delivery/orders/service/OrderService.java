package com.delivery.orders.service;

import com.delivery.orders.dto.*;
import com.delivery.orders.entity.*;
import com.delivery.orders.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    
    @Transactional
    public Mono<OrderResponse> addProductToCart(AddProductRequest request) {
        log.info("Adding product {} to cart for customer {}", request.getProductId(), request.getCustomerId());
        
        return productService.getProductById(request.getProductId())
                .flatMap(product -> {
                    log.info("Product response: id={}, name={}, price={}, available={}, stock={}", 
                            product.getId(), product.getName(), product.getPrice(), 
                            product.getAvailable(), product.getStock());
                    
                    if (product.getAvailable() != null && !product.getAvailable()) {
                        return Mono.error(new RuntimeException("Product is not available"));
                    }
                    if (product.getStock() != null && product.getStock() < request.getQuantity()) {
                        return Mono.error(new RuntimeException("Insufficient stock"));
                    }
                    
                    return Mono.fromCallable(() -> {
                        Customer customer = customerRepository.findById(request.getCustomerId())
                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                        
                        Order cartOrder = orderRepository.findByCustomerIdAndStatus(
                                request.getCustomerId(), Order.STATUS_CART)
                                .orElseGet(() -> orderRepository.save(createNewCartOrder(customer)));
                        
                        OrderItem existingItem = orderItemRepository.findByOrderIdAndProductId(
                                cartOrder.getOrderId(), request.getProductId())
                                .orElse(null);
                        
                        BigDecimal itemTotal = product.getPrice() != null ? 
                product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())) : 
                BigDecimal.ZERO;
                        
                        if (existingItem != null) {
                            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                            orderItemRepository.save(existingItem);
                        } else {
                            OrderItem newItem = OrderItem.builder()
                                    .order(cartOrder)
                                    .productId(request.getProductId())
                                    .quantity(request.getQuantity())
                                    .build();
                            orderItemRepository.save(newItem);
                        }
                        
                        BigDecimal newTotal = cartOrder.getTotal().add(itemTotal);
                        cartOrder.setTotal(newTotal);
                        orderRepository.save(cartOrder);
                        
                        // Refresh the order to ensure all relationships are loaded
                        Order savedOrder = orderRepository.findById(cartOrder.getOrderId()).orElseThrow();
                        
                        return buildOrderResponse(savedOrder, product);
                    });
                })
                .doOnSuccess(response -> log.info("Successfully added product to cart"))
                .doOnError(error -> log.error("Error adding product to cart: {}", error.getMessage()));
    }
    
    private Order createNewCartOrder(Customer customer) {
        Order order = Order.builder()
                .customer(customer)
                .orderDate(LocalDate.now())
                .total(BigDecimal.ZERO)
                .status(Order.STATUS_CART)
                .build();
        order.setOrderItems(new ArrayList<>());
        return order;
    }
    
    private OrderResponse buildOrderResponse(Order order, ProductResponse product) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getCustomerId())
                .customerEmail(order.getCustomer().getEmail())
                .status(order.getStatus())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .orderItems(order.getOrderItems().stream()
                        .filter(item -> item.getDeletedAt() == null)
                        .map(item -> OrderItemResponse.builder()
                                .orderItemId(item.getOrderItemId())
                                .productId(item.getProductId())
                                .productName(item.getProductId().equals(product.getId()) ? product.getName() : "Product " + item.getProductId())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getProductId().equals(product.getId()) ? product.getPrice() : BigDecimal.ZERO)
                                .totalPrice(item.getProductId().equals(product.getId()) ? 
                                        product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO)
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
