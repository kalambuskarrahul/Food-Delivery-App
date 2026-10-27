package com.fooddelivery.service;

import com.fooddelivery.dto.response.ShoppingResponses.OrderItemResponse;
import com.fooddelivery.dto.response.ShoppingResponses.OrderResponse;
import com.fooddelivery.dto.response.ShoppingResponses.StatusEntry;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Payment;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.OrderStatusHistoryRepository;
import com.fooddelivery.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/** Converts an Order entity to its API response. Call it from inside a transaction (lazy relations). */
@Component
public class OrderMapper {

    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public OrderMapper(PaymentRepository paymentRepository, OrderStatusHistoryRepository historyRepository) {
        this.paymentRepository = paymentRepository;
        this.historyRepository = historyRepository;
    }

    public OrderResponse toResponse(Order o) {
        Payment payment = paymentRepository.findByOrderId(o.getId()).orElse(null);
        List<StatusEntry> timeline = historyRepository.findByOrderIdOrderByIdAsc(o.getId()).stream()
                .map(h -> new StatusEntry(h.getStatus(), h.getChangedAt())).toList();
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(i.getItemName(), i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
                .toList();
        User partner = o.getDeliveryPartner();
        return new OrderResponse(
                o.getId(), o.getOrderNumber(), o.getStatus(),
                o.getRestaurant().getId(), o.getRestaurant().getName(), o.getRestaurant().getAddress(),
                o.getCustomer().getName(), o.getCustomer().getPhone(),
                partner != null ? partner.getName() : null,
                o.getDeliveryAddress(), items,
                o.getSubtotal(), o.getDeliveryFee(), o.getTax(), o.getTotal(),
                payment != null ? payment.getMethod() : null,
                payment != null ? payment.getStatus() : null,
                o.getCancelReason(), timeline, o.getPlacedAt(), o.getUpdatedAt());
    }
}
