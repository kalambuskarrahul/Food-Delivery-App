package com.fooddelivery.controller;

import com.fooddelivery.dto.request.ShoppingRequests.AddCartItemRequest;
import com.fooddelivery.dto.request.ShoppingRequests.UpdateQuantityRequest;
import com.fooddelivery.dto.response.ShoppingResponses.CartResponse;
import com.fooddelivery.security.CustomUserDetails;
import com.fooddelivery.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal CustomUserDetails p) {
        return cartService.getCart(p.getUser().getId());
    }

    @PostMapping("/items")
    public CartResponse add(@AuthenticationPrincipal CustomUserDetails p, @Valid @RequestBody AddCartItemRequest req) {
        return cartService.addItem(p.getUser().getId(), req);
    }

    @PutMapping("/items/{menuItemId}")
    public CartResponse update(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long menuItemId,
                               @Valid @RequestBody UpdateQuantityRequest req) {
        return cartService.updateQuantity(p.getUser().getId(), menuItemId, req.quantity());
    }

    @DeleteMapping("/items/{menuItemId}")
    public CartResponse remove(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long menuItemId) {
        return cartService.removeItem(p.getUser().getId(), menuItemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(@AuthenticationPrincipal CustomUserDetails p) {
        cartService.clear(p.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
