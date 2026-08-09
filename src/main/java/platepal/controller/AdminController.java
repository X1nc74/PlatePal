package platepal.controller;

import java.util.List;

import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.service.AdminService;
import platepal.service.RestaurantService;

/**
 * Routes restaurant management actions from the UI to the right service.
 */
public class AdminController {

    private final AdminService adminService;
    private final RestaurantService restaurantService;

    public AdminController(AdminService adminService,
                           RestaurantService restaurantService) {

        this.adminService = adminService;
        this.restaurantService = restaurantService;
    }

    public Restaurant addRestaurant(String name,
                                    String cuisine,
                                    String location,
                                    PriceCategory priceCategory,
                                    String description) {

        return adminService.addRestaurant(
                name, cuisine, location, priceCategory, description);
    }

    public Restaurant updateRestaurant(String restaurantId,
                                       String name,
                                       String cuisine,
                                       String location,
                                       PriceCategory priceCategory,
                                       String description) {

        return adminService.updateRestaurant(
                restaurantId, name, cuisine, location, priceCategory, description);
    }

    public void removeRestaurant(String restaurantId) {
        adminService.removeRestaurant(restaurantId);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }
}
