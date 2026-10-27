package com.fooddelivery.service;

import com.fooddelivery.dto.request.CatalogRequests.CategoryRequest;
import com.fooddelivery.dto.request.CatalogRequests.MenuItemRequest;
import com.fooddelivery.dto.response.CatalogResponses.MenuItemResponse;
import com.fooddelivery.dto.response.CatalogResponses.MenuResponse;
import com.fooddelivery.dto.response.CatalogResponses.MenuSectionResponse;
import com.fooddelivery.entity.MenuCategory;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.exception.BadRequestException;
import com.fooddelivery.exception.ConflictException;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.MenuCategoryRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MenuService {

    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantService restaurantService;

    public MenuService(RestaurantRepository restaurantRepository, MenuCategoryRepository categoryRepository,
                       MenuItemRepository itemRepository, RestaurantService restaurantService) {
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.restaurantService = restaurantService;
    }

    @Transactional(readOnly = true)
    public MenuResponse publicMenu(Long restaurantId) {
        Restaurant r = restaurantRepository.findById(restaurantId)
                .filter(x -> x.getStatus() == RestaurantStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return build(r, false);
    }

    @Transactional(readOnly = true)
    public MenuResponse ownerMenu(Long ownerId, Long restaurantId) {
        return build(restaurantService.getOwnedRestaurant(ownerId, restaurantId), true);
    }

    @Transactional
    public MenuSectionResponse createCategory(Long ownerId, Long restaurantId, CategoryRequest req) {
        Restaurant r = restaurantService.getOwnedRestaurant(ownerId, restaurantId);
        String name = req.name().trim();
        if (categoryRepository.existsByRestaurantIdAndNameIgnoreCase(restaurantId, name)) {
            throw new ConflictException("A category named '" + name + "' already exists");
        }
        MenuCategory c = new MenuCategory();
        c.setRestaurant(r);
        c.setName(name);
        c = categoryRepository.save(c);
        return new MenuSectionResponse(c.getId(), c.getName(), List.of());
    }

    @Transactional
    public void deleteCategory(Long ownerId, Long categoryId) {
        MenuCategory c = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        restaurantService.getOwnedRestaurant(ownerId, c.getRestaurant().getId());
        // items of this category are kept (category becomes empty) by the database foreign key
        categoryRepository.delete(c);
    }

    @Transactional
    public MenuItemResponse createItem(Long ownerId, Long restaurantId, MenuItemRequest req) {
        Restaurant r = restaurantService.getOwnedRestaurant(ownerId, restaurantId);
        MenuItem item = new MenuItem();
        item.setRestaurant(r);
        apply(item, r, req);
        return MenuItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public MenuItemResponse updateItem(Long ownerId, Long itemId, MenuItemRequest req) {
        MenuItem item = getOwnedItem(ownerId, itemId);
        apply(item, item.getRestaurant(), req);
        return MenuItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(Long ownerId, Long itemId) {
        itemRepository.delete(getOwnedItem(ownerId, itemId));
    }

    @Transactional
    public MenuItemResponse setAvailability(Long ownerId, Long itemId, boolean available) {
        MenuItem item = getOwnedItem(ownerId, itemId);
        item.setAvailable(available);
        return MenuItemResponse.from(item);
    }

    // ---------------- helpers ----------------

    private MenuItem getOwnedItem(Long ownerId, Long itemId) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        if (!item.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("This menu item belongs to another owner");
        }
        return item;
    }

    private void apply(MenuItem item, Restaurant r, MenuItemRequest req) {
        MenuCategory category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .filter(c -> c.getRestaurant().getId().equals(r.getId()))
                    .orElseThrow(() -> new BadRequestException("Category does not belong to this restaurant"));
        }
        item.setCategory(category);
        item.setName(req.name().trim());
        item.setDescription(req.description());
        item.setPrice(req.price().setScale(2, RoundingMode.HALF_UP));
        item.setVeg(req.veg() == null || req.veg());
        item.setAvailable(req.available() == null || req.available());
        item.setImageUrl(req.imageUrl());
    }

    private MenuResponse build(Restaurant r, boolean includeEmptySections) {
        List<MenuCategory> categories = categoryRepository.findByRestaurantIdOrderByNameAsc(r.getId());
        List<MenuItem> items = itemRepository.findByRestaurantIdOrderByNameAsc(r.getId());

        Map<Long, List<MenuItemResponse>> byCategory = new LinkedHashMap<>();
        List<MenuItemResponse> uncategorised = new ArrayList<>();
        for (MenuItem item : items) {
            MenuItemResponse dto = MenuItemResponse.from(item);
            if (dto.categoryId() == null) {
                uncategorised.add(dto);
            } else {
                byCategory.computeIfAbsent(dto.categoryId(), k -> new ArrayList<>()).add(dto);
            }
        }

        List<MenuSectionResponse> sections = new ArrayList<>();
        for (MenuCategory c : categories) {
            List<MenuItemResponse> list = byCategory.getOrDefault(c.getId(), List.of());
            if (includeEmptySections || !list.isEmpty()) {
                sections.add(new MenuSectionResponse(c.getId(), c.getName(), list));
            }
        }
        if (!uncategorised.isEmpty()) {
            sections.add(new MenuSectionResponse(null, "Other", uncategorised));
        }
        return new MenuResponse(r.getId(), r.getName(), r.isOpen(), sections);
    }
}
