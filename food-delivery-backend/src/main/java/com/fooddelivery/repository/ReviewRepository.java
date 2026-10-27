package com.fooddelivery.repository;

import com.fooddelivery.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderId(Long orderId);

    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.restaurant.id = :restaurantId")
    Double averageRating(@Param("restaurantId") Long restaurantId);
}
