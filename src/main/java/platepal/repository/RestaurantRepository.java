package platepal.repository;

import java.util.List;
import java.util.Optional;

import platepal.model.Restaurant;

public interface RestaurantRepository {

    List<Restaurant> findAll();

    Optional<Restaurant> findById(String id);

    void save(Restaurant restaurant);

    void deleteById(String id);
}