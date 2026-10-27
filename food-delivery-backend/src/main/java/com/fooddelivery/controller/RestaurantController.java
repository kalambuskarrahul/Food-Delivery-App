package com.fooddelivery.controller;

import com.fooddelivery.dto.response.CatalogResponses.MenuResponse;
import com.fooddelivery.dto.response.CatalogResponses.RestaurantResponse;
import com.fooddelivery.dto.response.CatalogResponses.ReviewResponse;
import com.fooddelivery.dto.response.PageResponse;
import com.fooddelivery.service.MenuService;
import com.fooddelivery.service.RestaurantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoints: anyone can browse approved restaurants and their menus. */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    public RestaurantController(RestaurantService restaurantService, MenuService menuService) {
        this.restaurantService = restaurantService;
        this.menuService = menuService;
    }

    @GetMapping
    public PageResponse<RestaurantResponse> search(@RequestParam(required = false) String city,
                                                   @RequestParam(required = false) String cuisine,
                                                   @RequestParam(required = false) String q,
                                                   @RequestParam(defaultValue = "newest") String sort,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "12") int size) {
        return restaurantService.search(city, cuisine, q, sort, page, size);
    }

    @GetMapping("/{id}")
    public RestaurantResponse get(@PathVariable Long id) {
        return restaurantService.get(id);
    }

    @GetMapping("/{id}/menu")
    public MenuResponse menu(@PathVariable Long id) {
        return menuService.publicMenu(id);
    }

    @GetMapping("/{id}/reviews")
    public PageResponse<ReviewResponse> reviews(@PathVariable Long id,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return restaurantService.reviews(id, page, size);
    }
}
