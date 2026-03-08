package com.delivery.orders.repository;

import com.delivery.orders.entity.Order;
import com.delivery.orders.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    @Query("SELECT o FROM Order o WHERE o.customer.customerId = :customerId AND o.status = :status AND o.deletedAt IS NULL")
    Optional<Order> findByCustomerIdAndStatus(@Param("customerId") Integer customerId, @Param("status") String status);
}
