package platepal.service;

import java.util.List;
import java.util.Optional;

import platepal.model.Rating;
import platepal.model.User;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;

/**
 * Leaving, changing and reading restaurant ratings.
 *
 * <p>The model enforces what a single Rating can check for itself: business
 * rule 1, that the score is between 1 and 10. The rules that need to see more
 * than one object live here:
 *
 * <ul>
 *   <li><b>Rule 2</b> — at most one rating per user per restaurant, by looking
 *       for an existing rating before creating a new one.</li>
 *   <li><b>Rule 3</b> — rating the same restaurant again updates the existing
 *       rating and refreshes its timestamp.</li>
 *   <li><b>Rule 4</b> — a user may only change their own rating, which holds
 *       because every write is looked up by the logged-in user's ID. There is
 *       no path that takes someone else's rating ID.</li>
 *   <li><b>Rule 5</b> — only restaurants in the user's Visited list may be
 *       rated, checked against the logged-in user's own list before a rating
 *       is created or updated.</li>
 * </ul>
 */
public class RatingService {

    private static final String ID_PREFIX = "RT";

    private final RatingRepository ratingRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuthService authService;

    public RatingService(RatingRepository ratingRepository,
                         RestaurantRepository restaurantRepository,
                         AuthService authService) {

        this.ratingRepository = ratingRepository;
        this.restaurantRepository = restaurantRepository;
        this.authService = authService;
    }

    /**
     * Records the logged-in user's score for a restaurant, replacing their
     * previous score if they have already rated it.
     *
     * @return the stored rating, new or updated
     * @throws IllegalArgumentException if no such restaurant exists, the score
     *                                  is outside 1..10, or the restaurant is
     *                                  not in the user's Visited list
     * @throws IllegalStateException    if nobody is logged in
     */
    public Rating rateRestaurant(String restaurantId, int score) {
        String id = requireRestaurant(restaurantId);
        User user = authService.requireCurrentUser();
        requireVisited(user, id);

        Optional<Rating> existing =
                ratingRepository.findByUserAndRestaurant(user.getId(), id);

        Rating rating;

        if (existing.isPresent()) {
            rating = existing.get();
            rating.updateScore(score);
        } else {
            // The constructor validates the score, so an invalid one throws
            // before anything is written.
            rating = new Rating(nextRatingId(), user.getId(), id, score);
        }

        ratingRepository.save(rating);

        return rating;
    }

    /** @return the logged-in user's rating for a restaurant, if they left one */
    public Optional<Rating> getMyRating(String restaurantId) {
        User user = authService.requireCurrentUser();

        return ratingRepository.findByUserAndRestaurant(user.getId(), restaurantId);
    }

    /**
     * Deletes the logged-in user's own rating for a restaurant.
     *
     * @return true if a rating was found and removed
     * @throws IllegalStateException if nobody is logged in
     */
    public boolean removeMyRating(String restaurantId) {
        Optional<Rating> existing = getMyRating(restaurantId);

        existing.ifPresent(rating -> ratingRepository.deleteById(rating.getId()));

        return existing.isPresent();
    }

    /** @return every rating the logged-in user has left, newest change first */
    public List<Rating> getMyRatings() {
        return getRatingsByUser(authService.requireCurrentUser().getId());
    }

    public List<Rating> getRatingsByUser(String userId) {
        return ratingRepository.findByUserId(userId);
    }

    public List<Rating> getRatingsForRestaurant(String restaurantId) {
        return ratingRepository.findByRestaurantId(restaurantId);
    }

    /** @return how many users have rated the restaurant */
    public int getRatingCount(String restaurantId) {
        return ratingRepository.findByRestaurantId(restaurantId).size();
    }

    /** @return the mean score, or 0.0 when nobody has rated the restaurant yet */
    public double getAverageRating(String restaurantId) {
        List<Rating> ratings =
                ratingRepository.findByRestaurantId(restaurantId);

        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);
    }

    /**
     * Business rule 5: a restaurant must be in the user's Visited list before
     * it can be rated.
     *
     * @throws IllegalArgumentException if the user has not visited it
     */
    private void requireVisited(User user, String restaurantId) {
        if (!user.hasVisited(restaurantId)) {
            throw new IllegalArgumentException(
                    "You can only rate restaurants in your Visited list. "
                            + "Mark \"" + restaurantId + "\" as visited first.");
        }
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
     * Produces the next ID in the {@code RT001} format, from the highest
     * existing ID rather than the number of ratings, so that a deleted rating's
     * ID is not handed to a different one later.
     */
    private String nextRatingId() {
        int highest = 0;

        for (Rating rating : ratingRepository.findAll()) {
            highest = Math.max(highest, numericSuffix(rating.getId()));
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
