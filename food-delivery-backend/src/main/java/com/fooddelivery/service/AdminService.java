package com.fooddelivery.service;

import com.fooddelivery.dto.response.AdminResponses.AdminStatsResponse;
import com.fooddelivery.dto.response.AdminResponses.AdminUserResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.util.Paging;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public AdminService(UserRepository userRepository, RestaurantRepository restaurantRepository,
                        OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> users(int page, int size) {
        return PageResponse.from(userRepository.findAll(Paging.newestFirst(page, size, "createdAt"))
                .map(AdminUserResponse::from));
    }

    @Transactional
    public AdminUserResponse setUserEnabled(Long adminId, Long userId, boolean enabled) {
        if (adminId.equals(userId)) {
            throw new BadRequestException("You cannot disable your own account");
        }
        User u = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        u.setEnabled(enabled);
        return AdminUserResponse.from(u);
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
                userRepository.count(),
                restaurantRepository.count(),
                restaurantRepository.countByStatus(RestaurantStatus.PENDING),
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.DELIVERED),
                orderRepository.countByStatus(OrderStatus.CANCELLED),
                orderRepository.sumTotalByStatus(OrderStatus.DELIVERED));
    }
}
