package com.delivery.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private Boolean available;
}
