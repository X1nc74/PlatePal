package platepal.service;

import java.util.List;
import java.util.Optional;

import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.repository.RestaurantRepository;

public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public Optional<Restaurant> getRestaurantById(String id) {
        return restaurantRepository.findById(id);
    }

    public List<Restaurant> searchByName(String query) {
        return restaurantRepository.findAll()
                .stream()
                .filter(restaurant -> restaurant.matchesName(query))
                .toList();
    }

    public List<Restaurant> searchByCuisine(String cuisine) {
        return restaurantRepository.findAll()
                .stream()
                .filter(restaurant -> restaurant.matchesCuisine(cuisine))
                .toList();
    }

    public List<Restaurant> searchByLocation(String location) {
        return restaurantRepository.findAll()
                .stream()
                .filter(restaurant -> restaurant.matchesLocation(location))
                .toList();
    }

    public List<Restaurant> searchByPrice(PriceCategory priceCategory) {
        return restaurantRepository.findAll()
                .stream()
                .filter(restaurant ->
                        restaurant.getPriceCategory() == priceCategory)
                .toList();
    }
}