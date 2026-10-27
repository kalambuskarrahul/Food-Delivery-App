package com.fooddelivery.service;

import com.fooddelivery.dto.request.CatalogRequests.RestaurantRequest;
import com.fooddelivery.dto.response.CatalogResponses.AdminRestaurantResponse;
import com.fooddelivery.dto.response.CatalogResponses.RestaurantResponse;
import com.fooddelivery.dto.response.CatalogResponses.ReviewResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.Review;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.ReviewRepository;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.util.Paging;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserRepository userRepository,
                             ReviewRepository reviewRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    // ---------------- public browsing ----------------

    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> search(String city, String cuisine, String q, String sort,
                                                   int page, int size) {
        Sort order = switch (sort == null ? "" : sort) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "avgRating");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        Pageable pageable = Paging.of(page, size, order);
        Page<Restaurant> result = restaurantRepository.search(RestaurantStatus.APPROVED,
                blank(city), blank(cuisine), blank(q), pageable);
        return PageResponse.from(result.map(RestaurantResponse::from));
    }

    @Transactional(readOnly = true)
    public RestaurantResponse get(Long id) {
        return RestaurantResponse.from(getApproved(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> reviews(Long restaurantId, int page, int size) {
        getApproved(restaurantId);
        Page<Review> result = reviewRepository.findByRestaurantId(restaurantId,
                Paging.newestFirst(page, size, "createdAt"));
        return PageResponse.from(result.map(r -> new ReviewResponse(r.getId(), r.getRating(), r.getComment(),
                r.getCustomer().getName(), r.getCreatedAt())));
    }

    // ---------------- restaurant owner ----------------

    @Transactional
    public RestaurantResponse create(Long ownerId, RestaurantRequest req) {
        Restaurant r = new Restaurant();
        r.setOwner(userRepository.getReferenceById(ownerId));
        r.setStatus(RestaurantStatus.PENDING);
        apply(r, req);
        return RestaurantResponse.from(restaurantRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> listMine(Long ownerId) {
        return restaurantRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(RestaurantResponse::from).toList();
    }

    @Transactional
    public RestaurantResponse update(Long ownerId, Long id, RestaurantRequest req) {
        Restaurant r = getOwnedRestaurant(ownerId, id);
        apply(r, req);
        return RestaurantResponse.from(r);
    }

    @Transactional
    public RestaurantResponse setOpen(Long ownerId, Long id, boolean open) {
        Restaurant r = getOwnedRestaurant(ownerId, id);
        r.setOpen(open);
        return RestaurantResponse.from(r);
    }

    /** Must be called from inside a transaction. */
    public Restaurant getOwnedRestaurant(Long ownerId, Long id) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (!r.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("This restaurant belongs to another owner");
        }
        return r;
    }

    // ---------------- admin ----------------

    @Transactional(readOnly = true)
    public PageResponse<AdminRestaurantResponse> adminList(RestaurantStatus status, int page, int size) {
        Pageable pageable = Paging.newestFirst(page, size, "createdAt");
        Page<Restaurant> result = status == null
                ? restaurantRepository.findAll(pageable)
                : restaurantRepository.findByStatus(status, pageable);
        return PageResponse.from(result.map(r -> new AdminRestaurantResponse(RestaurantResponse.from(r),
                r.getOwner().getName(), r.getOwner().getEmail())));
    }

    @Transactional
    public RestaurantResponse adminSetStatus(Long id, RestaurantStatus status) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        r.setStatus(status);
        return RestaurantResponse.from(r);
    }

    // ---------------- helpers ----------------

    private Restaurant getApproved(Long id) {
        return restaurantRepository.findById(id)
                .filter(r -> r.getStatus() == RestaurantStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    private void apply(Restaurant r, RestaurantRequest req) {
        r.setName(req.name().trim());
        r.setDescription(req.description());
        r.setCuisine(req.cuisine());
        r.setPhone(req.phone());
        r.setAddress(req.address().trim());
        r.setCity(req.city().trim());
        r.setPincode(req.pincode());
        r.setImageUrl(req.imageUrl());
    }

    private static String blank(String s) {
        return s == null ? "" : s.trim();
    }
}
