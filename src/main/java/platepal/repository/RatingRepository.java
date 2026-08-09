package platepal.repository;

import java.util.List;
import java.util.Optional;

import platepal.model.Rating;

public interface RatingRepository {

    List<Rating> findAll();

    Optional<Rating> findById(String id);

    List<Rating> findByUserId(String userId);

    List<Rating> findByRestaurantId(String restaurantId);

    /**
     * Finds the one rating a user has left for a restaurant.
     *
     * <p>Business rule 2 allows at most one, so this returns an Optional rather
     * than a list. RatingService calls it before saving, to decide between
     * creating a new rating and updating the existing one.
     */
    Optional<Rating> findByUserAndRestaurant(String userId, String restaurantId);

    /** Adds the rating, or replaces the stored one with the same ID. */
    void save(Rating rating);

    void deleteById(String id);

    /**
     * Removes every rating for a restaurant. Used when an administrator deletes
     * the restaurant, so that no rating is left pointing at an ID that is gone.
     */
    void deleteByRestaurantId(String restaurantId);
}
