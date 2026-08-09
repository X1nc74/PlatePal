package platepal.controller;

import java.util.List;
import java.util.Optional;

import platepal.model.Review;
import platepal.service.ReviewService;

/**
 * Sits between the rating menu and {@link ReviewService}, so the screen never
 * talks to a service directly.
 */
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public Review writeReview(String restaurantId, String content) {
        return reviewService.writeReview(restaurantId, content);
    }

    public Optional<Review> getMyReview(String restaurantId) {
        return reviewService.getMyReview(restaurantId);
    }

    public boolean removeMyReview(String restaurantId) {
        return reviewService.removeMyReview(restaurantId);
    }

    public List<Review> getMyReviews() {
        return reviewService.getMyReviews();
    }

    public List<Review> getReviewsForRestaurant(String restaurantId) {
        return reviewService.getReviewsForRestaurant(restaurantId);
    }

    public String getAuthorName(Review review) {
        return reviewService.getAuthorName(review);
    }
}
