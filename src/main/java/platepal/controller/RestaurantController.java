package platepal.controller;

import java.util.List;
import java.util.Optional;

import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.service.PersonalRankingService;
import platepal.service.RestaurantService;
import platepal.strategy.RestaurantSortStrategy;

public class RestaurantController {

    private final RestaurantService restaurantService;
    private final PersonalRankingService personalRankingService;

    public RestaurantController(
            RestaurantService restaurantService,
            PersonalRankingService personalRankingService) {

        this.restaurantService = restaurantService;
        this.personalRankingService = personalRankingService;
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    public Optional<Restaurant> getRestaurantById(String id) {
        return restaurantService.getRestaurantById(id);
    }

    public List<Restaurant> searchByName(String query) {
        return restaurantService.searchByName(query);
    }

    public List<Restaurant> searchByCuisine(String cuisine) {
        return restaurantService.searchByCuisine(cuisine);
    }

    public List<Restaurant> searchByLocation(String location) {
        return restaurantService.searchByLocation(location);
    }

    public List<Restaurant> searchByPrice(PriceCategory priceCategory) {
        return restaurantService.searchByPrice(priceCategory);
    }

    public List<Restaurant> sortRestaurants(
            List<Restaurant> restaurants,
            RestaurantSortStrategy strategy) {

        return restaurantService.sortRestaurants(
                restaurants,
                strategy);
    }

    public List<Restaurant> getPersonalRanking(String userId) {
        return personalRankingService.getPersonalRanking(userId);
    }
}