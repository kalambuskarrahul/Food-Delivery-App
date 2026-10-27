package com.fooddelivery.repository;

import com.fooddelivery.entity.DeliveryProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryProfileRepository extends JpaRepository<DeliveryProfile, Long> {
}
