package com.fooddelivery.controller;

import com.fooddelivery.dto.response.AdminResponses.AdminStatsResponse;
import com.fooddelivery.dto.response.AdminResponses.AdminUserResponse;
import com.fooddelivery.dto.response.CatalogResponses.AdminRestaurantResponse;
import com.fooddelivery.dto.response.CatalogResponses.RestaurantResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.dto.response.ShoppingResponses.OrderResponse;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.security.CustomUserDetails;
import com.fooddelivery.service.AdminService;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.RestaurantService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    public AdminController(AdminService adminService, RestaurantService restaurantService, OrderService orderService) {
        this.adminService = adminService;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminService.stats();
    }

    @GetMapping("/users")
    public PageResponse<AdminUserResponse> users(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return adminService.users(page, size);
    }

    @PatchMapping("/users/{id}/enabled")
    public AdminUserResponse setEnabled(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long id,
                                        @RequestParam boolean enabled) {
        return adminService.setUserEnabled(p.getUser().getId(), id, enabled);
    }

    @GetMapping("/restaurants")
    public PageResponse<AdminRestaurantResponse> restaurants(@RequestParam(required = false) RestaurantStatus status,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
        return restaurantService.adminList(status, page, size);
    }

    @PatchMapping("/restaurants/{id}/status")
    public RestaurantResponse setRestaurantStatus(@PathVariable Long id, @RequestParam RestaurantStatus status) {
        return restaurantService.adminSetStatus(id, status);
    }

    @GetMapping("/orders")
    public PageResponse<OrderResponse> orders(@RequestParam(required = false) OrderStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return orderService.adminOrders(status, page, size);
    }
}
