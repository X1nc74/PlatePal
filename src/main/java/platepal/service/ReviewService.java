package platepal.service;

import java.util.List;
import java.util.Optional;

import platepal.model.Review;
import platepal.model.User;
import platepal.repository.RestaurantRepository;
import platepal.repository.ReviewRepository;
import platepal.repository.UserRepository;

/**
 * Writing, editing and reading written reviews.
 *
 * <p>The rules mirror {@link RatingService}: the {@link Review} model rejects
 * empty and over-long text, while the one-review-per-restaurant rule and the
 * "only your own" rule need the repository and the session, so they live here.
 *
 * <p>A review does not require a rating. The two are independent, which is why
 * this service does not touch the rating repository at all.
 */
public class ReviewService {

    private static final String ID_PREFIX = "RV";

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public ReviewService(ReviewRepository reviewRepository,
                         RestaurantRepository restaurantRepository,
                         UserRepository userRepository,
                         AuthService authService) {

        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /**
     * Saves the logged-in user's review of a restaurant, replacing the text of
     * their previous review if they have already written one.
     *
     * @return the stored review, new or updated
     * @throws IllegalArgumentException if no such restaurant exists, or the
     *                                  content is empty or too long
     * @throws IllegalStateException    if nobody is logged in
     */
    public Review writeReview(String restaurantId, String content) {
        String id = requireRestaurant(restaurantId);
        User user = authService.requireCurrentUser();

        Optional<Review> existing =
                reviewRepository.findByUserAndRestaurant(user.getId(), id);

        Review review;

        if (existing.isPresent()) {
            review = existing.get();
            review.updateContent(content);
        } else {
            // The constructor validates the content, so an empty or over-long
            // review throws before anything is written.
            review = new Review(nextReviewId(), user.getId(), id, content);
        }

        reviewRepository.save(review);

        return review;
    }

    /** @return the logged-in user's review of a restaurant, if they wrote one */
    public Optional<Review> getMyReview(String restaurantId) {
        User user = authService.requireCurrentUser();

        return reviewRepository.findByUserAndRestaurant(user.getId(), restaurantId);
    }

    /**
     * Deletes the logged-in user's own review of a restaurant.
     *
     * @return true if a review was found and removed
     * @throws IllegalStateException if nobody is logged in
     */
    public boolean removeMyReview(String restaurantId) {
        Optional<Review> existing = getMyReview(restaurantId);

        existing.ifPresent(review -> reviewRepository.deleteById(review.getId()));

        return existing.isPresent();
    }

    /** @return every review the logged-in user has written */
    public List<Review> getMyReviews() {
        return getReviewsByUser(authService.requireCurrentUser().getId());
    }

    public List<Review> getReviewsByUser(String userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<Review> getReviewsForRestaurant(String restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId);
    }

    /**
     * Looks up the display name for a review's author.
     *
     * <p>A review stores a user ID, not a name, so the menu would otherwise have
     * to reach past this service to the user repository to print anything
     * readable. A deleted author falls back to the raw ID instead of hiding the
     * review.
     */
    public String getAuthorName(Review review) {
        return userRepository.findById(review.getUserId())
                .map(User::getUsername)
                .orElse(review.getUserId());
    }

    /** @return the trimmed ID, once the restaurant is known to exist */
    private String requireRestaurant(String restaurantId) {
        return restaurantRepository.findById(
                        restaurantId == null ? null : restaurantId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No restaurant found with id " + restaurantId + "."))
                .getId();
    }

    /**
     * Produces the next ID in the {@code RV001} format, from the highest
     * existing ID rather than the number of reviews, so that a deleted review's
     * ID is not handed to a different one later.
     */
    private String nextReviewId() {
        int highest = 0;

        for (Review review : reviewRepository.findAll()) {
            highest = Math.max(highest, numericSuffix(review.getId()));
        }

        return String.format("%s%03d", ID_PREFIX, highest + 1);
    }

    private static int numericSuffix(String id) {
        if (id == null || !id.startsWith(ID_PREFIX)) {
            return 0;
        }

        try {
            return Integer.parseInt(id.substring(ID_PREFIX.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
