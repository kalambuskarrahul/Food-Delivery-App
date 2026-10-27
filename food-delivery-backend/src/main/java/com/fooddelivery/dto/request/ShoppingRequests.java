package com.fooddelivery.dto.request;

import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentMethod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ShoppingRequests {

    private ShoppingRequests() {
    }

    public record AddCartItemRequest(
            @NotNull(message = "Menu item is required") Long menuItemId,
            @Min(value = 1, message = "Quantity must be at least 1") @Max(value = 50, message = "Quantity cannot exceed 50") int quantity) {
    }

    public record UpdateQuantityRequest(
            @Min(value = 1, message = "Quantity must be at least 1") @Max(value = 50, message = "Quantity cannot exceed 50") int quantity) {
    }

    public record AddressRequest(
            @Size(max = 30, message = "Label is too long") String label,
            @NotBlank(message = "Address line 1 is required") @Size(max = 200, message = "Line 1 is too long") String line1,
            @Size(max = 200, message = "Line 2 is too long") String line2,
            @NotBlank(message = "City is required") @Size(max = 80, message = "City is too long") String city,
            @NotBlank(message = "State is required") @Size(max = 80, message = "State is too long") String state,
            @NotBlank(message = "Pincode is required") @Pattern(regexp = "^[0-9]{5,10}$", message = "Pincode must be 5-10 digits") String pincode,
            Boolean defaultAddress) {
    }

    public record PlaceOrderRequest(
            Long addressId,
            @Size(max = 500, message = "Address is too long") String deliveryAddress,
            @NotNull(message = "Payment method is required") PaymentMethod paymentMethod) {
    }

    public record UpdateOrderStatusRequest(
            @NotNull(message = "Status is required") OrderStatus status,
            @Size(max = 255, message = "Reason is too long") String reason) {
    }

    public record ReviewRequest(
            @Min(value = 1, message = "Rating must be 1-5") @Max(value = 5, message = "Rating must be 1-5") int rating,
            @Size(max = 500, message = "Comment is too long") String comment) {
    }
}
