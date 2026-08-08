package platepal.repository;

import java.util.List;

import platepal.model.Rating;

public interface RatingRepository {

    List<Rating> findAll();

    List<Rating> findByUserId(String userId);

    List<Rating> findByRestaurantId(String restaurantId);

    /**
     * Removes every rating for a restaurant. Used when an administrator deletes
     * the restaurant, so that no rating is left pointing at an ID that is gone.
     */
    void deleteByRestaurantId(String restaurantId);
}