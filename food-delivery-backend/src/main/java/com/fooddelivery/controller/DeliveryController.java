package com.fooddelivery.controller;

import com.fooddelivery.dto.request.ShoppingRequests.UpdateOrderStatusRequest;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.dto.response.ShoppingResponses.AvailabilityResponse;
import com.fooddelivery.dto.response.ShoppingResponses.OrderResponse;
import com.fooddelivery.security.CustomUserDetails;
import com.fooddelivery.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@PreAuthorize("hasRole('DELIVERY_PARTNER')")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/availability")
    public AvailabilityResponse availability(@AuthenticationPrincipal CustomUserDetails p) {
        return deliveryService.getAvailability(p.getUser().getId());
    }

    @PutMapping("/availability")
    public AvailabilityResponse setAvailability(@AuthenticationPrincipal CustomUserDetails p,
                                                @RequestParam boolean available) {
        return deliveryService.setAvailability(p.getUser().getId(), available);
    }

    @GetMapping("/available-orders")
    public List<OrderResponse> availableOrders() {
        return deliveryService.availableOrders();
    }

    @PostMapping("/orders/{orderId}/claim")
    public OrderResponse claim(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long orderId) {
        return deliveryService.claim(p.getUser().getId(), orderId);
    }

    @PatchMapping("/orders/{orderId}/status")
    public OrderResponse updateStatus(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long orderId,
                                      @Valid @RequestBody UpdateOrderStatusRequest req) {
        return deliveryService.updateStatus(p.getUser().getId(), orderId, req);
    }

    @GetMapping("/orders")
    public PageResponse<OrderResponse> myDeliveries(@AuthenticationPrincipal CustomUserDetails p,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return deliveryService.myDeliveries(p.getUser().getId(), page, size);
    }
}
