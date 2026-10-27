package com.fooddelivery.dto.response;

import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class AdminResponses {

    private AdminResponses() {
    }

    public record AdminUserResponse(Long id, String name, String email, String phone, Role role, boolean enabled,
                                    LocalDateTime createdAt) {
        public static AdminUserResponse from(User u) {
            return new AdminUserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole(),
                    u.isEnabled(), u.getCreatedAt());
        }
    }

    public record AdminStatsResponse(long totalUsers, long totalRestaurants, long pendingRestaurants,
                                     long totalOrders, long deliveredOrders, long cancelledOrders,
                                     BigDecimal totalRevenue) {
    }
}
