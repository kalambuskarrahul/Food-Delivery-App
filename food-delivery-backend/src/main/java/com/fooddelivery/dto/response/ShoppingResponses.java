package com.fooddelivery.dto.response;

import com.fooddelivery.entity.Address;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentMethod;
import com.fooddelivery.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ShoppingResponses {

    private ShoppingResponses() {
    }

    public record CartItemResponse(Long menuItemId, String name, BigDecimal unitPrice, int quantity,
                                   BigDecimal lineTotal, boolean veg, boolean available, String imageUrl) {
    }

    public record CartResponse(Long restaurantId, String restaurantName, List<CartItemResponse> items,
                               BigDecimal subtotal, int totalItems) {
    }

    public record AddressResponse(Long id, String label, String line1, String line2, String city, String state,
                                  String pincode, boolean defaultAddress, String fullAddress) {
        public static AddressResponse from(Address a, String fullAddress) {
            return new AddressResponse(a.getId(), a.getLabel(), a.getLine1(), a.getLine2(), a.getCity(), a.getState(),
                    a.getPincode(), a.isDefaultAddress(), fullAddress);
        }
    }

    public record OrderItemResponse(String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }

    public record StatusEntry(OrderStatus status, LocalDateTime at) {
    }

    public record OrderResponse(Long id, String orderNumber, OrderStatus status, Long restaurantId,
                                String restaurantName, String restaurantAddress, String customerName,
                                String customerPhone, String deliveryPartnerName, String deliveryAddress,
                                List<OrderItemResponse> items, BigDecimal subtotal, BigDecimal deliveryFee,
                                BigDecimal tax, BigDecimal total, PaymentMethod paymentMethod,
                                PaymentStatus paymentStatus, String cancelReason, List<StatusEntry> timeline,
                                LocalDateTime placedAt, LocalDateTime updatedAt) {
    }

    public record AvailabilityResponse(boolean available) {
    }
}
