package platepal.controller;

import java.util.List;
import java.util.Optional;

import platepal.model.Rating;
import platepal.service.RatingService;

/**
 * Sits between the rating menu and {@link RatingService}, so the screen never
 * talks to a service directly.
 */
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    public Rating rateRestaurant(String restaurantId, int score) {
        return ratingService.rateRestaurant(restaurantId, score);
    }

    public Optional<Rating> getMyRating(String restaurantId) {
        return ratingService.getMyRating(restaurantId);
    }

    public boolean removeMyRating(String restaurantId) {
        return ratingService.removeMyRating(restaurantId);
    }

    public List<Rating> getMyRatings() {
        return ratingService.getMyRatings();
    }

    public List<Rating> getRatingsForRestaurant(String restaurantId) {
        return ratingService.getRatingsForRestaurant(restaurantId);
    }

    public double getAverageRating(String restaurantId) {
        return ratingService.getAverageRating(restaurantId);
    }

    public int getRatingCount(String restaurantId) {
        return ratingService.getRatingCount(restaurantId);
    }
}
