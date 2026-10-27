package com.fooddelivery.service;

import com.fooddelivery.dto.request.ShoppingRequests.PlaceOrderRequest;
import com.fooddelivery.dto.request.ShoppingRequests.ReviewRequest;
import com.fooddelivery.dto.request.ShoppingRequests.UpdateOrderStatusRequest;
import com.fooddelivery.dto.response.CatalogResponses.ReviewResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.dto.response.ShoppingResponses.OrderResponse;
import com.fooddelivery.entity.Address;
import com.fooddelivery.entity.Cart;
import com.fooddelivery.entity.CartItem;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.OrderItem;
import com.fooddelivery.entity.Payment;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.Review;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.enums.PaymentMethod;
import com.fooddelivery.enums.PaymentStatus;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ConflictException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.AddressRepository;
import com.fooddelivery.repository.CartRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.PaymentRepository;
import com.fooddelivery.repository.ReviewRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.util.Paging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private static final Set<OrderStatus> OWNER_TARGETS =
            EnumSet.of(OrderStatus.ACCEPTED, OrderStatus.REJECTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final OrderWorkflow workflow;
    private final OrderMapper mapper;

    @Value("${app.order.delivery-fee:40.00}")
    private BigDecimal deliveryFee;

    @Value("${app.order.free-delivery-above:500.00}")
    private BigDecimal freeDeliveryAbove;

    @Value("${app.order.tax-percent:5}")
    private BigDecimal taxPercent;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        AddressRepository addressRepository, UserRepository userRepository,
                        PaymentRepository paymentRepository, ReviewRepository reviewRepository,
                        OrderWorkflow workflow, OrderMapper mapper) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.workflow = workflow;
        this.mapper = mapper;
    }

    // ---------------- customer ----------------

    @Transactional
    public OrderResponse placeOrder(Long userId, PlaceOrderRequest req) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));
        if (cart.getItems().isEmpty() || cart.getRestaurant() == null) {
            throw new BadRequestException("Your cart is empty");
        }
        Restaurant restaurant = cart.getRestaurant();
        if (restaurant.getStatus() != RestaurantStatus.APPROVED || !restaurant.isOpen()) {
            throw new BadRequestException(restaurant.getName() + " is not accepting orders right now");
        }
        String addressText = resolveAddress(userId, req);
        User customer = userRepository.getReferenceById(userId);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryAddress(addressText);
        order.setStatus(OrderStatus.PLACED);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            MenuItem mi = ci.getMenuItem();
            if (!mi.isAvailable()) {
                throw new BadRequestException(mi.getName() + " is no longer available. Please remove it from your cart.");
            }
            BigDecimal line = mi.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setMenuItem(mi);
            oi.setItemName(mi.getName());
            oi.setUnitPrice(mi.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setLineTotal(line);
            order.getItems().add(oi);
            subtotal = subtotal.add(line);
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = (subtotal.compareTo(freeDeliveryAbove) >= 0 ? BigDecimal.ZERO : deliveryFee)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = subtotal.multiply(taxPercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal);
        order.setDeliveryFee(fee);
        order.setTax(tax);
        order.setTotal(subtotal.add(fee).add(tax));

        Order saved = orderRepository.save(order);
        workflow.record(saved, OrderStatus.PLACED, customer);
        createPayment(saved, req.paymentMethod());

        cart.getItems().clear();
        cart.setRestaurant(null);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(Long userId, int page, int size) {
        Page<Order> result = orderRepository.findByCustomerId(userId, Paging.newestFirst(page, size, "placedAt"));
        return PageResponse.from(result.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Role role, Long orderId) {
        Order o = find(orderId);
        boolean allowed = role == Role.ADMIN
                || o.getCustomer().getId().equals(userId)
                || o.getRestaurant().getOwner().getId().equals(userId)
                || (o.getDeliveryPartner() != null && o.getDeliveryPartner().getId().equals(userId));
        if (!allowed) {
            throw new AccessDeniedException("You cannot view this order");
        }
        return mapper.toResponse(o);
    }

    @Transactional
    public OrderResponse cancel(Long userId, Long orderId, String reason) {
        Order o = find(orderId);
        if (!o.getCustomer().getId().equals(userId)) {
            throw new AccessDeniedException("This is not your order");
        }
        if (o.getStatus() != OrderStatus.PLACED) {
            throw new BadRequestException("An order can only be cancelled before the restaurant accepts it");
        }
        String why = (reason == null || reason.isBlank()) ? "Cancelled by customer" : reason.trim();
        workflow.transition(o, OrderStatus.CANCELLED, userRepository.getReferenceById(userId), why);
        return mapper.toResponse(o);
    }

    @Transactional
    public ReviewResponse addReview(Long userId, Long orderId, ReviewRequest req) {
        Order o = find(orderId);
        if (!o.getCustomer().getId().equals(userId)) {
            throw new AccessDeniedException("This is not your order");
        }
        if (o.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can review an order only after it is delivered");
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new ConflictException("You have already reviewed this order");
        }
        Review review = new Review();
        review.setOrder(o);
        review.setCustomer(o.getCustomer());
        review.setRestaurant(o.getRestaurant());
        review.setRating(req.rating());
        review.setComment(req.comment());
        review = reviewRepository.save(review);

        Double avg = reviewRepository.averageRating(o.getRestaurant().getId());
        o.getRestaurant().setAvgRating(BigDecimal.valueOf(avg == null ? 0 : avg).setScale(1, RoundingMode.HALF_UP));
        return new ReviewResponse(review.getId(), review.getRating(), review.getComment(),
                o.getCustomer().getName(), review.getCreatedAt());
    }

    // ---------------- restaurant owner ----------------

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> ownerOrders(Long ownerId, OrderStatus status, int page, int size) {
        Pageable pageable = Paging.newestFirst(page, size, "placedAt");
        Page<Order> result = status == null
                ? orderRepository.findByRestaurantOwnerId(ownerId, pageable)
                : orderRepository.findByRestaurantOwnerIdAndStatus(ownerId, status, pageable);
        return PageResponse.from(result.map(mapper::toResponse));
    }

    @Transactional
    public OrderResponse ownerUpdateStatus(Long ownerId, Long orderId, UpdateOrderStatusRequest req) {
        Order o = find(orderId);
        if (!o.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("This order belongs to another restaurant");
        }
        if (!OWNER_TARGETS.contains(req.status())) {
            throw new BadRequestException("Restaurants can only set ACCEPTED, REJECTED, PREPARING or READY_FOR_PICKUP");
        }
        String reason = req.status() == OrderStatus.REJECTED
                ? (req.reason() == null || req.reason().isBlank() ? "Rejected by restaurant" : req.reason().trim())
                : null;
        workflow.transition(o, req.status(), userRepository.getReferenceById(ownerId), reason);
        return mapper.toResponse(o);
    }

    // ---------------- admin ----------------

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> adminOrders(OrderStatus status, int page, int size) {
        Pageable pageable = Paging.newestFirst(page, size, "placedAt");
        Page<Order> result = status == null ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(status, pageable);
        return PageResponse.from(result.map(mapper::toResponse));
    }

    // ---------------- helpers ----------------

    private Order find(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private String resolveAddress(Long userId, PlaceOrderRequest req) {
        if (req.addressId() != null) {
            Address a = addressRepository.findByIdAndUserId(req.addressId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            return AddressService.format(a);
        }
        if (req.deliveryAddress() != null && !req.deliveryAddress().isBlank()) {
            return req.deliveryAddress().trim();
        }
        throw new BadRequestException("A delivery address is required");
    }

    /** Online payments are simulated (no real gateway): they succeed instantly. Cash is collected on delivery. */
    private void createPayment(Order order, PaymentMethod method) {
        Payment p = new Payment();
        p.setOrder(order);
        p.setMethod(method);
        p.setAmount(order.getTotal());
        if (method == PaymentMethod.COD) {
            p.setStatus(PaymentStatus.PENDING);
        } else {
            p.setStatus(PaymentStatus.SUCCESS);
            p.setTransactionRef("SIM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            p.setPaidAt(LocalDateTime.now());
        }
        paymentRepository.save(p);
    }

    private String generateOrderNumber() {
        return "FD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
