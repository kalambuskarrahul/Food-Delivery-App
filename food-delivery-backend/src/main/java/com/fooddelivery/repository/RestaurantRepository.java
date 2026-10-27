package com.fooddelivery.repository;

import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.enums.RestaurantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

    long countByStatus(RestaurantStatus status);

    @Query("""
            select r from Restaurant r
            where r.status = :status
              and (:city = '' or lower(r.city) = lower(:city))
              and (:cuisine = '' or lower(r.cuisine) like lower(concat('%', :cuisine, '%')))
              and (:q = '' or lower(r.name) like lower(concat('%', :q, '%')))
            """)
    Page<Restaurant> search(@Param("status") RestaurantStatus status,
                            @Param("city") String city,
                            @Param("cuisine") String cuisine,
                            @Param("q") String q,
                            Pageable pageable);
}
