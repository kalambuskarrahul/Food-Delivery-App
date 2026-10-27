package com.fooddelivery.dto.response;

import com.fooddelivery.entity.MenuCategory;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.enums.RestaurantStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class CatalogResponses {

    private CatalogResponses() {
    }

    public record RestaurantResponse(Long id, String name, String description, String cuisine, String phone,
                                     String address, String city, String pincode, String imageUrl, boolean open,
                                     RestaurantStatus status, BigDecimal avgRating, LocalDateTime createdAt) {
        public static RestaurantResponse from(Restaurant r) {
            return new RestaurantResponse(r.getId(), r.getName(), r.getDescription(), r.getCuisine(), r.getPhone(),
                    r.getAddress(), r.getCity(), r.getPincode(), r.getImageUrl(), r.isOpen(), r.getStatus(),
                    r.getAvgRating(), r.getCreatedAt());
        }
    }

    public record MenuItemResponse(Long id, Long categoryId, String categoryName, String name, String description,
                                   BigDecimal price, boolean veg, boolean available, String imageUrl) {
        public static MenuItemResponse from(MenuItem m) {
            MenuCategory c = m.getCategory();
            return new MenuItemResponse(m.getId(), c != null ? c.getId() : null, c != null ? c.getName() : null,
                    m.getName(), m.getDescription(), m.getPrice(), m.isVeg(), m.isAvailable(), m.getImageUrl());
        }
    }

    public record MenuSectionResponse(Long categoryId, String name, List<MenuItemResponse> items) {
    }

    public record MenuResponse(Long restaurantId, String restaurantName, boolean open, List<MenuSectionResponse> sections) {
    }

    public record ReviewResponse(Long id, int rating, String comment, String customerName, LocalDateTime createdAt) {
    }

    public record AdminRestaurantResponse(RestaurantResponse restaurant, String ownerName, String ownerEmail) {
    }
}
