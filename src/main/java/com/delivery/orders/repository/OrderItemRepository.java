package com.delivery.orders.repository;

import com.delivery.orders.entity.OrderItem;
import com.delivery.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.orderId = :orderId AND oi.productId = :productId AND oi.deletedAt IS NULL")
    Optional<OrderItem> findByOrderIdAndProductId(@Param("orderId") Integer orderId, @Param("productId") Integer productId);
}
