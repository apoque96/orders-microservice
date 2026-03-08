package com.delivery.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Integer orderId;
    private Integer customerId;
    private String customerEmail;
    private String status;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> orderItems;
}
