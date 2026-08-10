package platepal.controller;

import java.util.List;
import java.util.Optional;

import platepal.model.PriceCategory;
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;
import platepal.service.RestaurantService;
import platepal.strategy.RestaurantSortStrategy;
import platepal.strategy.SortByRating;

public class RestaurantController {

    private final RestaurantService restaurantService;
    private final PersonalRankingService personalRankingService;
    private final RatingService ratingService;

    public RestaurantController(
            RestaurantService restaurantService,
            PersonalRankingService personalRankingService,
            RatingService ratingService) {

        this.restaurantService = restaurantService;
        this.personalRankingService = personalRankingService;
        this.ratingService = ratingService;
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

    public List<Restaurant> searchByPrice(
            PriceCategory priceCategory) {

        return restaurantService.searchByPrice(
                priceCategory);
    }

    public List<Restaurant> sortRestaurants(
            List<Restaurant> restaurants,
            RestaurantSortStrategy strategy) {

        return restaurantService.sortRestaurants(
                restaurants,
                strategy);
    }

    public List<Restaurant> sortByAverageRating() {
        return restaurantService.sortRestaurants(
                restaurantService.getAllRestaurants(),
                new SortByRating(ratingService));
    }

    public double getAverageRating(
            String restaurantId) {

        return ratingService.getAverageRating(
                restaurantId);
    }

    public int getRatingCount(
            String restaurantId) {

        return ratingService.getRatingCount(
                restaurantId);
    }

    public Optional<Rating> getMyRating(
            String restaurantId) {

        return ratingService.getMyRating(
                restaurantId);
    }

    public List<Restaurant> getPersonalRanking(
            String userId) {

        return personalRankingService
                .getPersonalRanking(userId);
    }
}