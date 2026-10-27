package com.fooddelivery.repository;

import com.fooddelivery.entity.Order;
import com.fooddelivery.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByRestaurantOwnerId(Long ownerId, Pageable pageable);

    Page<Order> findByRestaurantOwnerIdAndStatus(Long ownerId, OrderStatus status, Pageable pageable);

    Page<Order> findByDeliveryPartnerId(Long partnerId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByStatusAndDeliveryPartnerIsNullOrderByPlacedAtAsc(OrderStatus status);

    long countByStatus(OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from FoodOrder o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("select coalesce(sum(o.total), 0) from FoodOrder o where o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);
}
