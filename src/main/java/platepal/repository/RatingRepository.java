package platepal.repository;

import java.util.List;

import platepal.model.Rating;

public interface RatingRepository {

    List<Rating> findAll();

    List<Rating> findByUserId(String userId);

    List<Rating> findByRestaurantId(String restaurantId);
}