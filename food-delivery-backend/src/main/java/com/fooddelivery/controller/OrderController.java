package com.fooddelivery.controller;

import com.fooddelivery.dto.request.ShoppingRequests.PlaceOrderRequest;
import com.fooddelivery.dto.request.ShoppingRequests.ReviewRequest;
import com.fooddelivery.dto.response.CatalogResponses.ReviewResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.dto.response.ShoppingResponses.OrderResponse;
import com.fooddelivery.security.CustomUserDetails;
import com.fooddelivery.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> place(@AuthenticationPrincipal CustomUserDetails p,
                                               @Valid @RequestBody PlaceOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(p.getUser().getId(), req));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<OrderResponse> myOrders(@AuthenticationPrincipal CustomUserDetails p,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return orderService.myOrders(p.getUser().getId(), page, size);
    }

    /** Visible to the customer, the restaurant owner, the assigned rider and admins. */
    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long id) {
        return orderService.getOrder(p.getUser().getId(), p.getUser().getRole(), id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse cancel(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long id,
                                @RequestParam(required = false) String reason) {
        return orderService.cancel(p.getUser().getId(), id, reason);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> review(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long id,
                                                 @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addReview(p.getUser().getId(), id, req));
    }
}
