package platepal.repository;

import java.util.List;
import java.util.Optional;

import platepal.model.Review;

/**
 * Storage operations for written reviews.
 *
 * <p>Reviews are stored separately from ratings, under the {@code "reviews"} key
 * of the data file, because a user may rate a restaurant without writing about
 * it and may edit the two independently.
 */
public interface ReviewRepository {

    List<Review> findAll();

    Optional<Review> findById(String id);

    List<Review> findByUserId(String userId);

    List<Review> findByRestaurantId(String restaurantId);

    /**
     * Finds the one review a user has written for a restaurant.
     *
     * <p>Business rule 2 allows at most one, so writing again updates the
     * existing review rather than adding a second.
     */
    Optional<Review> findByUserAndRestaurant(String userId, String restaurantId);

    /** Adds the review, or replaces the stored one with the same ID. */
    void save(Review review);

    void deleteById(String id);

    /**
     * Removes every review for a restaurant, so that deleting a restaurant does
     * not leave reviews pointing at an ID that is gone.
     */
    void deleteByRestaurantId(String restaurantId);
}
