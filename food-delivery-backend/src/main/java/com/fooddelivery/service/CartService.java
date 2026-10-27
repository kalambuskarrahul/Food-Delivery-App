package com.fooddelivery.service;

import com.fooddelivery.dto.request.ShoppingRequests.AddCartItemRequest;
import com.fooddelivery.dto.response.ShoppingResponses.CartItemResponse;
import com.fooddelivery.dto.response.ShoppingResponses.CartResponse;
import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.CartItem;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ConflictException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.CartRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private static final int MAX_QUANTITY = 50;

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, MenuItemRepository menuItemRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return cartRepository.findByUserId(userId).map(this::toResponse)
                .orElseGet(() -> new CartResponse(null, null, List.of(), BigDecimal.ZERO, 0));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest req) {
        MenuItem menuItem = menuItemRepository.findById(req.menuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        Restaurant restaurant = menuItem.getRestaurant();
        if (restaurant.getStatus() != RestaurantStatus.APPROVED || !restaurant.isOpen()) {
            throw new BadRequestException(restaurant.getName() + " is not accepting orders right now");
        }
        if (!menuItem.isAvailable()) {
            throw new BadRequestException(menuItem.getName() + " is currently unavailable");
        }

        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart c = new Cart();
            c.setUser(userRepository.getReferenceById(userId));
            return c;
        });

        if (!cart.getItems().isEmpty() && cart.getRestaurant() != null
                && !cart.getRestaurant().getId().equals(restaurant.getId())) {
            throw new ConflictException("Your cart has items from another restaurant. Clear the cart to order from "
                    + restaurant.getName());
        }
        cart.setRestaurant(restaurant);

        CartItem existing = cart.getItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst().orElse(null);
        if (existing != null) {
            int quantity = existing.getQuantity() + req.quantity();
            if (quantity > MAX_QUANTITY) {
                throw new BadRequestException("You can order at most " + MAX_QUANTITY + " of one item");
            }
            existing.setQuantity(quantity);
        } else {
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setMenuItem(menuItem);
            ci.setQuantity(req.quantity());
            cart.getItems().add(ci);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateQuantity(Long userId, Long menuItemId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Your cart is empty"));
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItemId))
                .findFirst().orElseThrow(() -> new ResourceNotFoundException("Item is not in your cart"));
        item.setQuantity(quantity);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long menuItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Your cart is empty"));
        cart.getItems().removeIf(ci -> ci.getMenuItem().getId().equals(menuItemId));
        if (cart.getItems().isEmpty()) {
            cart.setRestaurant(null);
        }
        return toResponse(cart);
    }

    @Transactional
    public void clear(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setRestaurant(null);
        });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(ci -> {
            MenuItem m = ci.getMenuItem();
            BigDecimal line = m.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            return new CartItemResponse(m.getId(), m.getName(), m.getPrice(), ci.getQuantity(), line,
                    m.isVeg(), m.isAvailable(), m.getImageUrl());
        }).toList();
        BigDecimal subtotal = items.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        Restaurant r = cart.getRestaurant();
        return new CartResponse(r != null ? r.getId() : null, r != null ? r.getName() : null, items, subtotal, totalItems);
    }
}
