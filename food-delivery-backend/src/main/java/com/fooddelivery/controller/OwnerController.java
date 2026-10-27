package com.fooddelivery.controller;

import com.fooddelivery.dto.request.CatalogRequests.CategoryRequest;
import com.fooddelivery.dto.request.CatalogRequests.MenuItemRequest;
import com.fooddelivery.dto.request.CatalogRequests.RestaurantRequest;
import com.fooddelivery.dto.request.ShoppingRequests.UpdateOrderStatusRequest;
import com.fooddelivery.dto.response.CatalogResponses.MenuItemResponse;
import com.fooddelivery.dto.response.CatalogResponses.MenuResponse;
import com.fooddelivery.dto.response.CatalogResponses.MenuSectionResponse;
import com.fooddelivery.dto.response.CatalogResponses.RestaurantResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.dto.response.ShoppingResponses.OrderResponse;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.security.CustomUserDetails;
import com.fooddelivery.service.MenuService;
import com.fooddelivery.service.OrderService;
import com.fooddelivery.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/** Everything a restaurant owner can do: restaurants, menu and incoming orders. */
@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class OwnerController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;
    private final OrderService orderService;

    public OwnerController(RestaurantService restaurantService, MenuService menuService, OrderService orderService) {
        this.restaurantService = restaurantService;
        this.menuService = menuService;
        this.orderService = orderService;
    }

    private static Long id(CustomUserDetails p) {
        return p.getUser().getId();
    }

    // ---- restaurants ----
    @GetMapping("/restaurants")
    public List<RestaurantResponse> myRestaurants(@AuthenticationPrincipal CustomUserDetails p) {
        return restaurantService.listMine(id(p));
    }

    @PostMapping("/restaurants")
    public ResponseEntity<RestaurantResponse> createRestaurant(@AuthenticationPrincipal CustomUserDetails p,
                                                               @Valid @RequestBody RestaurantRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.create(id(p), req));
    }

    @PutMapping("/restaurants/{restaurantId}")
    public RestaurantResponse updateRestaurant(@AuthenticationPrincipal CustomUserDetails p,
                                               @PathVariable Long restaurantId,
                                               @Valid @RequestBody RestaurantRequest req) {
        return restaurantService.update(id(p), restaurantId, req);
    }

    @PatchMapping("/restaurants/{restaurantId}/open")
    public RestaurantResponse setOpen(@AuthenticationPrincipal CustomUserDetails p,
                                      @PathVariable Long restaurantId, @RequestParam boolean open) {
        return restaurantService.setOpen(id(p), restaurantId, open);
    }

    // ---- menu ----
    @GetMapping("/restaurants/{restaurantId}/menu")
    public MenuResponse menu(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long restaurantId) {
        return menuService.ownerMenu(id(p), restaurantId);
    }

    @PostMapping("/restaurants/{restaurantId}/categories")
    public ResponseEntity<MenuSectionResponse> createCategory(@AuthenticationPrincipal CustomUserDetails p,
                                                              @PathVariable Long restaurantId,
                                                              @Valid @RequestBody CategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createCategory(id(p), restaurantId, req));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@AuthenticationPrincipal CustomUserDetails p,
                                               @PathVariable Long categoryId) {
        menuService.deleteCategory(id(p), categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemResponse> createItem(@AuthenticationPrincipal CustomUserDetails p,
                                                       @PathVariable Long restaurantId,
                                                       @Valid @RequestBody MenuItemRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.createItem(id(p), restaurantId, req));
    }

    @PutMapping("/menu-items/{itemId}")
    public MenuItemResponse updateItem(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long itemId,
                                       @Valid @RequestBody MenuItemRequest req) {
        return menuService.updateItem(id(p), itemId, req);
    }

    @DeleteMapping("/menu-items/{itemId}")
    public ResponseEntity<Void> deleteItem(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long itemId) {
        menuService.deleteItem(id(p), itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/menu-items/{itemId}/availability")
    public MenuItemResponse setAvailability(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long itemId,
                                            @RequestParam boolean available) {
        return menuService.setAvailability(id(p), itemId, available);
    }

    // ---- orders ----
    @GetMapping("/orders")
    public PageResponse<OrderResponse> orders(@AuthenticationPrincipal CustomUserDetails p,
                                              @RequestParam(required = false) OrderStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return orderService.ownerOrders(id(p), status, page, size);
    }

    @PatchMapping("/orders/{orderId}/status")
    public OrderResponse updateOrderStatus(@AuthenticationPrincipal CustomUserDetails p, @PathVariable Long orderId,
                                           @Valid @RequestBody UpdateOrderStatusRequest req) {
        return orderService.ownerUpdateStatus(id(p), orderId, req);
    }
}
