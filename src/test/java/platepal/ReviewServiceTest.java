package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.model.Review;
import platepal.model.User;
import platepal.repository.RestaurantRepository;
import platepal.repository.ReviewRepository;
import platepal.repository.UserRepository;
import platepal.service.AuthService;
import platepal.service.ReviewService;
import platepal.service.UserService;

/**
 * Reviews follow the same rules as ratings:
 * one per user per restaurant,
 * a user may only change their own review,
 * and only Visited restaurants may be reviewed.
 */
public class ReviewServiceTest {

    private FakeReviewRepository reviewRepository;
    private UserRepository userRepository;
    private AuthService authService;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        FakeRestaurantRepository restaurantRepository =
                new FakeRestaurantRepository();

        restaurantRepository.save(
                new Restaurant(
                        "R001",
                        "Joe's Pizza",
                        "Pizza",
                        "New York",
                        PriceCategory.BUDGET));

        restaurantRepository.save(
                new Restaurant(
                        "R002",
                        "Sushi Nakazawa",
                        "Japanese",
                        "New York",
                        PriceCategory.EXPENSIVE));

        reviewRepository =
                new FakeReviewRepository();

        userRepository =
                new FakeUserRepository();

        UserService userService =
                new UserService(userRepository);

        User sunny =
                userService.register(
                        "sunny",
                        "example");

        User alex =
                userService.register(
                        "alex",
                        "example");

        // R001 is visited by both users so the existing normal review tests
        // continue to represent valid review behavior.
        sunny.markAsVisited("R001");
        alex.markAsVisited("R001");

        userRepository.save(sunny);
        userRepository.save(alex);

        authService =
                new AuthService(userRepository);

        reviewService =
                new ReviewService(
                        reviewRepository,
                        restaurantRepository,
                        userRepository,
                        authService);
    }

    private void loginAsSunny() {
        authService.login(
                "sunny",
                "example");
    }

    private void loginAsAlex() {
        authService.login(
                "alex",
                "example");
    }

    private void markVisited(
            String username,
            String restaurantId) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow();

        user.markAsVisited(restaurantId);
        userRepository.save(user);
    }

    // ------------------------------------------------------------- content

    @Test
    @DisplayName("An empty review is rejected and nothing is stored")
    void emptyReviewIsRejected() {
        loginAsSunny();

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.writeReview(
                        "R001",
                        "   "));

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.writeReview(
                        "R001",
                        null));

        assertTrue(
                reviewRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("A review longer than the limit is rejected")
    void overLongReviewIsRejected() {
        loginAsSunny();

        String tooLong =
                "x".repeat(
                        Review.MAX_CONTENT_LENGTH + 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.writeReview(
                        "R001",
                        tooLong));

        assertTrue(
                reviewRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("A review is stored trimmed")
    void reviewIsStoredTrimmed() {
        loginAsSunny();

        Review review =
                reviewService.writeReview(
                        "R001",
                        "  Great pizza.  ");

        assertEquals(
                "Great pizza.",
                review.getContent());
    }

    // ---------------------------------------------------- one per restaurant

    @Test
    @DisplayName(
            "Writing again replaces the existing review instead of adding a second")
    void writingAgainUpdatesTheExistingReview() {
        loginAsSunny();

        Review first =
                reviewService.writeReview(
                        "R001",
                        "Good.");

        Review second =
                reviewService.writeReview(
                        "R001",
                        "Actually excellent.");

        assertEquals(
                first.getId(),
                second.getId());

        assertEquals(
                "Actually excellent.",
                second.getContent());

        assertEquals(
                1,
                reviewRepository.findAll().size());
    }

    @Test
    @DisplayName("Updating a review keeps its original creation time")
    void updatingAReviewKeepsTheCreationTime() {
        loginAsSunny();

        Review review =
                reviewService.writeReview(
                        "R001",
                        "Good.");

        var createdAt =
                review.getCreatedAt();

        Review updated =
                reviewService.writeReview(
                        "R001",
                        "Better.");

        assertEquals(
                createdAt,
                updated.getCreatedAt());

        assertTrue(
                updated.getUpdatedAt()
                        .compareTo(createdAt) >= 0);
    }

    // ---------------------------------------------------------- only yours

    @Test
    @DisplayName("Writing a review never changes another user's review")
    void writingDoesNotTouchAnotherUsersReview() {
        loginAsSunny();

        reviewService.writeReview(
                "R001",
                "Too greasy.");

        loginAsAlex();

        reviewService.writeReview(
                "R001",
                "Perfect crust.");

        assertEquals(
                2,
                reviewService
                        .getReviewsForRestaurant("R001")
                        .size());

        assertEquals(
                "Perfect crust.",
                reviewService
                        .getMyReview("R001")
                        .orElseThrow()
                        .getContent());

        loginAsSunny();

        assertEquals(
                "Too greasy.",
                reviewService
                        .getMyReview("R001")
                        .orElseThrow()
                        .getContent());
    }

    @Test
    @DisplayName("Deleting a review only removes the logged-in user's own")
    void deletingAReviewOnlyRemovesYourOwn() {
        loginAsSunny();

        reviewService.writeReview(
                "R001",
                "Too greasy.");

        loginAsAlex();

        reviewService.writeReview(
                "R001",
                "Perfect crust.");

        assertTrue(
                reviewService.removeMyReview(
                        "R001"));

        List<Review> remaining =
                reviewService
                        .getReviewsForRestaurant("R001");

        assertEquals(
                1,
                remaining.size());

        assertEquals(
                "Too greasy.",
                remaining.get(0).getContent());
    }

    @Test
    @DisplayName("Deleting a review that was never written reports no change")
    void deletingAMissingReviewReportsNoChange() {
        loginAsSunny();

        assertFalse(
                reviewService.removeMyReview(
                        "R001"));
    }

    // -------------------------------------------------------------- guards

    @Test
    @DisplayName("Reviewing a restaurant that does not exist is rejected")
    void reviewingUnknownRestaurantIsRejected() {
        loginAsSunny();

        assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.writeReview(
                        "R999",
                        "Nice."));

        assertTrue(
                reviewRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Reviewing an unvisited restaurant is rejected")
    void reviewingUnvisitedRestaurantIsRejected() {
        loginAsSunny();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> reviewService.writeReview(
                                "R002",
                                "Great sushi."));

        assertTrue(
                exception.getMessage()
                        .contains("Visited list"));

        assertTrue(
                reviewRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Writing a review requires somebody to be logged in")
    void writingRequiresALogin() {
        assertThrows(
                IllegalStateException.class,
                () -> reviewService.writeReview(
                        "R001",
                        "Nice."));
    }

    // ------------------------------------------------------------ authors

    @Test
    @DisplayName("A review reports its author's username")
    void reviewReportsAuthorUsername() {
        loginAsSunny();

        Review review =
                reviewService.writeReview(
                        "R001",
                        "Nice.");

        assertEquals(
                "sunny",
                reviewService.getAuthorName(review));
    }

    @Test
    @DisplayName("A user's own reviews are listed across restaurants")
    void ownReviewsAreListedAcrossRestaurants() {
        loginAsSunny();

        markVisited(
                "sunny",
                "R002");

        reviewService.writeReview(
                "R001",
                "Nice.");

        reviewService.writeReview(
                "R002",
                "Also nice.");

        assertEquals(
                2,
                reviewService.getMyReviews().size());
    }

    // ---------------------------------------------------------------- ids

    @Test
    @DisplayName("Review IDs follow the RV001 format and increase")
    void reviewIdsAreGeneratedInSequence() {
        loginAsSunny();

        markVisited(
                "sunny",
                "R002");

        assertEquals(
                "RV001",
                reviewService
                        .writeReview(
                                "R001",
                                "One.")
                        .getId());

        assertEquals(
                "RV002",
                reviewService
                        .writeReview(
                                "R002",
                                "Two.")
                        .getId());
    }

    // ---------------------------------------------------------------- fakes

    private static class FakeRestaurantRepository
            implements RestaurantRepository {

        private final List<Restaurant> restaurants =
                new ArrayList<>();

        @Override
        public List<Restaurant> findAll() {
            return new ArrayList<>(
                    restaurants);
        }

        @Override
        public Optional<Restaurant> findById(
                String id) {

            return restaurants.stream()
                    .filter(
                            r -> r.getId()
                                    .equals(id))
                    .findFirst();
        }

        @Override
        public void save(
                Restaurant restaurant) {

            restaurants.removeIf(
                    r -> r.getId()
                            .equals(
                                    restaurant.getId()));

            restaurants.add(
                    restaurant);
        }

        @Override
        public void deleteById(
                String id) {

            restaurants.removeIf(
                    r -> r.getId()
                            .equals(id));
        }
    }

    private static class FakeReviewRepository
            implements ReviewRepository {

        private final List<Review> reviews =
                new ArrayList<>();

        @Override
        public List<Review> findAll() {
            return new ArrayList<>(
                    reviews);
        }

        @Override
        public Optional<Review> findById(
                String id) {

            return reviews.stream()
                    .filter(
                            review ->
                                    review.getId()
                                            .equals(id))
                    .findFirst();
        }

        @Override
        public List<Review> findByUserId(
                String userId) {

            return reviews.stream()
                    .filter(
                            review ->
                                    review.belongsTo(
                                            userId))
                    .toList();
        }

        @Override
        public List<Review> findByRestaurantId(
                String restaurantId) {

            return reviews.stream()
                    .filter(
                            review ->
                                    review.isFor(
                                            restaurantId))
                    .toList();
        }

        @Override
        public Optional<Review> findByUserAndRestaurant(
                String userId,
                String restaurantId) {

            return reviews.stream()
                    .filter(
                            review ->
                                    review.belongsTo(
                                            userId)
                                            && review.isFor(
                                                    restaurantId))
                    .findFirst();
        }

        @Override
        public void save(
                Review review) {

            reviews.removeIf(
                    stored ->
                            stored.getId()
                                    .equals(
                                            review.getId()));

            reviews.add(
                    review);
        }

        @Override
        public void deleteById(
                String id) {

            reviews.removeIf(
                    review ->
                            review.getId()
                                    .equals(id));
        }

        @Override
        public void deleteByRestaurantId(
                String restaurantId) {

            reviews.removeIf(
                    review ->
                            review.isFor(
                                    restaurantId));
        }
    }

    private static class FakeUserRepository
            implements UserRepository {

        private final List<User> users =
                new ArrayList<>();

        @Override
        public List<User> findAll() {
            return new ArrayList<>(
                    users);
        }

        @Override
        public Optional<User> findById(
                String id) {

            return users.stream()
                    .filter(
                            u -> u.getId()
                                    .equals(id))
                    .findFirst();
        }

        @Override
        public Optional<User> findByUsername(
                String username) {

            if (username == null
                    || username.isBlank()) {
                return Optional.empty();
            }

            String wanted =
                    username.trim();

            return users.stream()
                    .filter(
                            u -> u.getUsername()
                                    .equalsIgnoreCase(
                                            wanted))
                    .findFirst();
        }

        @Override
        public void save(
                User user) {

            users.removeIf(
                    u -> u.getId()
                            .equals(
                                    user.getId()));

            users.add(
                    user);
        }

        @Override
        public void deleteById(
                String id) {

            users.removeIf(
                    u -> u.getId()
                            .equals(id));
        }
    }
}