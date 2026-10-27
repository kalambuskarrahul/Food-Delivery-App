package com.fooddelivery.service;

import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OrderStatusHistory;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.OrderStatusHistoryRepository;
import com.fooddelivery.repository.PaymentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.fooddelivery.enums.OrderStatus.ACCEPTED;
import static com.fooddelivery.enums.OrderStatus.CANCELLED;
import static com.fooddelivery.enums.OrderStatus.DELIVERED;
import static com.fooddelivery.enums.OrderStatus.OUT_FOR_DELIVERY;
import static com.fooddelivery.enums.OrderStatus.PLACED;
import static com.fooddelivery.enums.OrderStatus.PREPARING;
import static com.fooddelivery.enums.OrderStatus.READY_FOR_PICKUP;
import static com.fooddelivery.enums.OrderStatus.REJECTED;

/** The single place that knows which order status changes are legal and what they cause. */
@Component
public class OrderWorkflow {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
            PLACED, EnumSet.of(ACCEPTED, REJECTED, CANCELLED),
            ACCEPTED, EnumSet.of(PREPARING),
            PREPARING, EnumSet.of(READY_FOR_PICKUP),
            READY_FOR_PICKUP, EnumSet.of(OUT_FOR_DELIVERY),
            OUT_FOR_DELIVERY, EnumSet.of(DELIVERED));

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;

    public OrderWorkflow(OrderRepository orderRepository, OrderStatusHistoryRepository historyRepository,
                         PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.paymentRepository = paymentRepository;
    }

    public void transition(Order order, OrderStatus target, User actor, String reason) {
        Set<OrderStatus> allowed = ALLOWED.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(target)) {
            throw new BadRequestException("Cannot change an order from " + order.getStatus() + " to " + target);
        }
        order.setStatus(target);

        if (target == REJECTED || target == CANCELLED) {
            order.setCancelReason(reason);
            paymentRepository.findByOrderId(order.getId()).ifPresent(p -> {
                if (p.getStatus() == PaymentStatus.SUCCESS) {
                    p.setStatus(PaymentStatus.REFUNDED);
                } else if (p.getStatus() == PaymentStatus.PENDING) {
                    p.setStatus(PaymentStatus.FAILED);
                }
            });
        } else if (target == DELIVERED) {
            paymentRepository.findByOrderId(order.getId()).ifPresent(p -> {
                if (p.getStatus() == PaymentStatus.PENDING) {   // cash collected on delivery
                    p.setStatus(PaymentStatus.SUCCESS);
                    p.setPaidAt(LocalDateTime.now());
                }
            });
        }
        orderRepository.save(order);
        record(order, target, actor);
    }

    public void record(Order order, OrderStatus status, User actor) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setStatus(status);
        h.setChangedBy(actor);
        historyRepository.save(h);
    }
}
