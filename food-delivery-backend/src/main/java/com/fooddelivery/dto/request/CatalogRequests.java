package com.fooddelivery.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class CatalogRequests {

    private CatalogRequests() {
    }

    public record RestaurantRequest(
            @NotBlank(message = "Restaurant name is required") @Size(max = 150, message = "Name is too long") String name,
            @Size(max = 2000, message = "Description is too long") String description,
            @Size(max = 100, message = "Cuisine is too long") String cuisine,
            @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Phone number is invalid") String phone,
            @NotBlank(message = "Address is required") @Size(max = 255, message = "Address is too long") String address,
            @NotBlank(message = "City is required") @Size(max = 80, message = "City is too long") String city,
            @Size(max = 10, message = "Pincode is too long") String pincode,
            @Size(max = 255, message = "Image URL is too long") String imageUrl) {
    }

    public record CategoryRequest(
            @NotBlank(message = "Category name is required") @Size(max = 100, message = "Name is too long") String name) {
    }

    public record MenuItemRequest(
            Long categoryId,
            @NotBlank(message = "Item name is required") @Size(max = 150, message = "Name is too long") String name,
            @Size(max = 1000, message = "Description is too long") String description,
            @NotNull(message = "Price is required")
            @DecimalMin(value = "0.01", message = "Price must be greater than 0")
            @Digits(integer = 8, fraction = 2, message = "Price format is invalid") BigDecimal price,
            Boolean veg,
            Boolean available,
            @Size(max = 255, message = "Image URL is too long") String imageUrl) {
    }
}
