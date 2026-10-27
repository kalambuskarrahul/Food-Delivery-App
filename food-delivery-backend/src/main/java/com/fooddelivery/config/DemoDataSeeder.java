package com.fooddelivery.config;

import com.fooddelivery.entity.DeliveryProfile;
import com.fooddelivery.entity.MenuCategory;
import com.fooddelivery.entity.MenuItem;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.RestaurantStatus;
import com.fooddelivery.enums.Role;
import com.fooddelivery.repository.DeliveryProfileRepository;
import com.fooddelivery.repository.MenuCategoryRepository;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Creates demo accounts, restaurants and menus on the very first start so the app is usable immediately.
 * Disable with app.seed-demo-data=false.
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String DEMO_PASSWORD = "Demo@1234";

    private final UserRepository userRepository;
    private final DeliveryProfileRepository deliveryProfileRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository, DeliveryProfileRepository deliveryProfileRepository,
                          RestaurantRepository restaurantRepository, MenuCategoryRepository categoryRepository,
                          MenuItemRepository itemRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.deliveryProfileRepository = deliveryProfileRepository;
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (restaurantRepository.count() > 0) {
            return;
        }
        User owner = user("Demo Owner", "owner@demo.com", "9000000001", Role.RESTAURANT_OWNER);
        user("Demo Customer", "customer@demo.com", "9000000002", Role.CUSTOMER);
        User rider = user("Demo Rider", "rider@demo.com", "9000000003", Role.DELIVERY_PARTNER);
        if (!deliveryProfileRepository.existsById(rider.getId())) {
            DeliveryProfile profile = new DeliveryProfile();
            profile.setUserId(rider.getId());
            profile.setVehicleType("Bike");
            profile.setVehicleNumber("MH01AB1234");
            profile.setAvailable(true);
            deliveryProfileRepository.save(profile);
        }

        Restaurant spice = restaurant(owner, "Spice Garden", "North Indian",
                "Rich curries, tandoori starters and fresh breads from a wood-fired tandoor.",
                "12 Linking Road, Bandra West", "Mumbai", "400050", "9820000001", "4.4", RestaurantStatus.APPROVED);
        section(spice, "Starters",
                item("Paneer Tikka", "Char-grilled cottage cheese with peppers", "260.00", true),
                item("Hara Bhara Kebab", "Spinach and green pea patties", "220.00", true),
                item("Chicken 65", "Spicy deep-fried chicken bites", "280.00", false));
        section(spice, "Main Course",
                item("Butter Chicken", "Tandoori chicken in a creamy tomato gravy", "340.00", false),
                item("Paneer Butter Masala", "Cottage cheese in a buttery tomato gravy", "290.00", true),
                item("Dal Makhani", "Slow-cooked black lentils with cream", "240.00", true),
                item("Veg Biryani", "Fragrant basmati rice with vegetables", "250.00", true),
                item("Chicken Biryani", "Dum-cooked rice with spiced chicken", "320.00", false));
        section(spice, "Breads",
                item("Butter Naan", "Soft tandoor-baked bread", "60.00", true),
                item("Garlic Naan", "Naan topped with garlic and coriander", "70.00", true),
                item("Tandoori Roti", "Whole wheat flatbread", "30.00", true));
        section(spice, "Desserts",
                item("Gulab Jamun", "Two warm milk dumplings in syrup", "90.00", true),
                item("Rasmalai", "Soft cheese patties in saffron milk", "120.00", true));

        Restaurant pizza = restaurant(owner, "Pizza Palace", "Italian",
                "Hand-tossed pizzas, creamy pastas and sides baked fresh to order.",
                "45 Veera Desai Road, Andheri West", "Mumbai", "400053", "9820000002", "4.2", RestaurantStatus.APPROVED);
        section(pizza, "Pizzas",
                item("Margherita", "Classic tomato, mozzarella and basil", "299.00", true),
                item("Farmhouse", "Capsicum, onion, mushroom and olives", "399.00", true),
                item("Pepperoni", "Spicy pepperoni with extra cheese", "449.00", false),
                item("BBQ Chicken", "Smoky BBQ chicken with onions", "459.00", false));
        section(pizza, "Pasta",
                item("Alfredo Pasta", "Penne in a creamy white sauce", "329.00", true),
                item("Arrabbiata Pasta", "Penne in a spicy tomato sauce", "309.00", true));
        section(pizza, "Sides",
                item("Garlic Bread", "Toasted bread with garlic butter", "149.00", true),
                item("French Fries", "Crispy salted fries", "119.00", true));
        section(pizza, "Beverages",
                item("Cold Coffee", "Chilled coffee with ice cream", "129.00", true),
                item("Fresh Lime Soda", "Sweet or salted", "79.00", true));

        Restaurant wok = restaurant(owner, "Dragon Wok", "Chinese",
                "Indo-Chinese favourites tossed on a roaring flame.",
                "8 FC Road, Shivajinagar", "Pune", "411004", "9820000003", "4.0", RestaurantStatus.APPROVED);
        section(wok, "Starters",
                item("Veg Manchurian", "Crispy vegetable balls in a tangy sauce", "190.00", true),
                item("Crispy Corn", "Fried sweet corn with spices", "180.00", true),
                item("Chilli Chicken", "Chicken tossed with peppers and soy", "250.00", false));
        section(wok, "Noodles and Rice",
                item("Veg Hakka Noodles", "Stir-fried noodles with vegetables", "210.00", true),
                item("Schezwan Fried Rice", "Spicy fried rice", "220.00", true),
                item("Chicken Hakka Noodles", "Noodles with chicken and egg", "260.00", false));
        section(wok, "Soups",
                item("Hot and Sour Soup", "Peppery and tangy", "120.00", true),
                item("Sweet Corn Soup", "Mild and creamy", "110.00", true));

        // waits for admin approval, so the approval flow can be demonstrated
        Restaurant cafe = restaurant(owner, "Sunrise Cafe", "Cafe",
                "Coffee, sandwiches and all-day breakfast.",
                "3 Carter Road, Bandra West", "Mumbai", "400050", "9820000004", "0.0", RestaurantStatus.PENDING);
        section(cafe, "Breakfast",
                item("Masala Omelette Sandwich", "Grilled sandwich with a masala omelette", "160.00", false),
                item("Avocado Toast", "Sourdough with smashed avocado", "220.00", true));

        log.info("Demo data created. Logins (password {}): owner@demo.com, customer@demo.com, rider@demo.com",
                DEMO_PASSWORD);
    }

    private User user(String name, String email, String phone, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setName(name);
            u.setEmail(email);
            u.setPhone(phone);
            u.setRole(role);
            u.setEnabled(true);
            u.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            return userRepository.save(u);
        });
    }

    private Restaurant restaurant(User owner, String name, String cuisine, String description, String address,
                                  String city, String pincode, String phone, String rating, RestaurantStatus status) {
        Restaurant r = new Restaurant();
        r.setOwner(owner);
        r.setName(name);
        r.setCuisine(cuisine);
        r.setDescription(description);
        r.setAddress(address);
        r.setCity(city);
        r.setPincode(pincode);
        r.setPhone(phone);
        r.setAvgRating(new BigDecimal(rating));
        r.setStatus(status);
        r.setOpen(true);
        return restaurantRepository.save(r);
    }

    private static Object[] item(String name, String description, String price, boolean veg) {
        return new Object[]{name, description, price, veg};
    }

    private void section(Restaurant restaurant, String categoryName, Object[]... items) {
        MenuCategory category = new MenuCategory();
        category.setRestaurant(restaurant);
        category.setName(categoryName);
        category = categoryRepository.save(category);
        for (Object[] i : items) {
            MenuItem m = new MenuItem();
            m.setRestaurant(restaurant);
            m.setCategory(category);
            m.setName((String) i[0]);
            m.setDescription((String) i[1]);
            m.setPrice(new BigDecimal((String) i[2]));
            m.setVeg((Boolean) i[3]);
            m.setAvailable(true);
            itemRepository.save(m);
        }
    }
}
