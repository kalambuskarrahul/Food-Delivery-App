package com.fooddelivery.service;

import com.fooddelivery.dto.request.ShoppingRequests.UpdateOrderStatusRequest;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.dto.response.ShoppingResponses.AvailabilityResponse;
import com.fooddelivery.dto.response.ShoppingResponses.OrderResponse;
import com.fooddelivery.entity.DeliveryProfile;
import com.fooddelivery.entity.Order;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ConflictException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.DeliveryProfileRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.util.Paging;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class DeliveryService {

    private static final Set<OrderStatus> RIDER_TARGETS = EnumSet.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED);

    private final OrderRepository orderRepository;
    private final DeliveryProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final OrderWorkflow workflow;
    private final OrderMapper mapper;

    public DeliveryService(OrderRepository orderRepository, DeliveryProfileRepository profileRepository,
                           UserRepository userRepository, OrderWorkflow workflow, OrderMapper mapper) {
        this.orderRepository = orderRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.workflow = workflow;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(Long userId) {
        return new AvailabilityResponse(profileRepository.findById(userId).map(DeliveryProfile::isAvailable).orElse(false));
    }

    @Transactional
    public AvailabilityResponse setAvailability(Long userId, boolean available) {
        DeliveryProfile profile = profileRepository.findById(userId).orElseGet(() -> {
            DeliveryProfile p = new DeliveryProfile();
            p.setUserId(userId);
            return p;
        });
        profile.setAvailable(available);
        profileRepository.save(profile);
        return new AvailabilityResponse(available);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> availableOrders() {
        return orderRepository.findByStatusAndDeliveryPartnerIsNullOrderByPlacedAtAsc(OrderStatus.READY_FOR_PICKUP)
                .stream().map(mapper::toResponse).toList();
    }

    /** A rider claims an order. The row is locked so two riders can never take the same order. */
    @Transactional
    public OrderResponse claim(Long userId, Long orderId) {
        DeliveryProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Delivery profile not found"));
        if (!profile.isAvailable()) {
            throw new BadRequestException("Go online before accepting deliveries");
        }
        Order o = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (o.getStatus() != OrderStatus.READY_FOR_PICKUP || o.getDeliveryPartner() != null) {
            throw new ConflictException("This order is no longer available");
        }
        o.setDeliveryPartner(userRepository.getReferenceById(userId));
        return mapper.toResponse(o);
    }

    @Transactional
    public OrderResponse updateStatus(Long userId, Long orderId, UpdateOrderStatusRequest req) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (o.getDeliveryPartner() == null || !o.getDeliveryPartner().getId().equals(userId)) {
            throw new AccessDeniedException("This order is not assigned to you");
        }
        if (!RIDER_TARGETS.contains(req.status())) {
            throw new BadRequestException("Delivery partners can only set OUT_FOR_DELIVERY or DELIVERED");
        }
        workflow.transition(o, req.status(), userRepository.getReferenceById(userId), null);
        return mapper.toResponse(o);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myDeliveries(Long userId, int page, int size) {
        return PageResponse.from(orderRepository
                .findByDeliveryPartnerId(userId, Paging.newestFirst(page, size, "placedAt"))
                .map(mapper::toResponse));
    }
}
