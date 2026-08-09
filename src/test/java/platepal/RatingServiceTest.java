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
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.model.User;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.repository.UserRepository;
import platepal.service.AuthService;
import platepal.service.RatingService;
import platepal.service.UserService;

/**
 * Covers the four rating business rules, and the parts of them that a single
 * Rating object cannot enforce on its own.
 */
public class RatingServiceTest {

    private UserRepository userRepository;
    private FakeRatingRepository ratingRepository;
    private AuthService authService;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        FakeRestaurantRepository restaurantRepository = new FakeRestaurantRepository();

        restaurantRepository.save(new Restaurant(
                "R001", "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET));

        restaurantRepository.save(new Restaurant(
                "R002", "Sushi Nakazawa", "Japanese", "New York", PriceCategory.EXPENSIVE));

        ratingRepository = new FakeRatingRepository();

        userRepository = new FakeUserRepository();
        UserService userService = new UserService(userRepository);
        userService.register("sunny", "example");
        userService.register("alex", "example");

        authService = new AuthService(userRepository);

        ratingService = new RatingService(
                ratingRepository, restaurantRepository, authService);
    }

    private void loginAsSunny() {
        authService.login("sunny", "example");
    }

    private void loginAsAlex() {
        authService.login("alex", "example");
    }

    /**
     * Rule 5 requires a restaurant to be visited before it can be rated, so
     * every test that rates a restaurant marks it visited first. This helper
     * mutates and re-saves the logged-in user directly, the same way
     * {@code PersonalListService} would.
     */
    private void markVisited(String restaurantId) {
        User user = authService.requireCurrentUser();
        user.markAsVisited(restaurantId);
        userRepository.save(user);
    }

    // ------------------------------------------------------- rule 1: 1 to 10

    @Test
    @DisplayName("A score outside 1 to 10 is rejected and nothing is stored")
    void scoreOutsideRangeIsRejected() {
        loginAsSunny();
        markVisited("R001");

        assertThrows(IllegalArgumentException.class,
                () -> ratingService.rateRestaurant("R001", 0));

        assertThrows(IllegalArgumentException.class,
                () -> ratingService.rateRestaurant("R001", 11));

        assertTrue(ratingRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("The lowest and highest allowed scores are accepted")
    void boundaryScoresAreAccepted() {
        loginAsSunny();
        markVisited("R001");
        markVisited("R002");

        assertEquals(1, ratingService.rateRestaurant("R001", 1).getScore());
        assertEquals(10, ratingService.rateRestaurant("R002", 10).getScore());
    }

    // ------------------------------------------- rules 2 and 3: one per pair

    @Test
    @DisplayName("Rating the same restaurant again updates instead of adding a second")
    void ratingAgainUpdatesTheExistingRating() {
        loginAsSunny();
        markVisited("R001");

        Rating first = ratingService.rateRestaurant("R001", 6);
        Rating second = ratingService.rateRestaurant("R001", 9);

        assertEquals(first.getId(), second.getId());
        assertEquals(9, second.getScore());
        assertEquals(1, ratingRepository.findAll().size());
    }

    @Test
    @DisplayName("Updating a rating refreshes its timestamp but keeps the original creation time")
    void updatingARatingRefreshesTheTimestamp() {
        loginAsSunny();
        markVisited("R001");

        Rating rating = ratingService.rateRestaurant("R001", 6);
        var createdAt = rating.getCreatedAt();

        Rating updated = ratingService.rateRestaurant("R001", 8);

        assertEquals(createdAt, updated.getCreatedAt());
        assertTrue(updated.getUpdatedAt().compareTo(createdAt) >= 0);
    }

    @Test
    @DisplayName("One user may rate many restaurants")
    void oneUserMayRateManyRestaurants() {
        loginAsSunny();
        markVisited("R001");
        markVisited("R002");

        ratingService.rateRestaurant("R001", 7);
        ratingService.rateRestaurant("R002", 9);

        assertEquals(2, ratingService.getMyRatings().size());
    }

    // ---------------------------------------------------- rule 4: only yours

    @Test
    @DisplayName("Rating a restaurant never changes another user's rating of it")
    void ratingDoesNotTouchAnotherUsersRating() {
        loginAsSunny();
        markVisited("R001");
        ratingService.rateRestaurant("R001", 4);

        loginAsAlex();
        markVisited("R001");
        ratingService.rateRestaurant("R001", 10);

        assertEquals(2, ratingRepository.findByRestaurantId("R001").size());
        assertEquals(10, ratingService.getMyRating("R001").orElseThrow().getScore());

        loginAsSunny();
        assertEquals(4, ratingService.getMyRating("R001").orElseThrow().getScore());
    }

    @Test
    @DisplayName("Deleting a rating only removes the logged-in user's own")
    void deletingARatingOnlyRemovesYourOwn() {
        loginAsSunny();
        markVisited("R001");
        ratingService.rateRestaurant("R001", 4);

        loginAsAlex();
        markVisited("R001");
        ratingService.rateRestaurant("R001", 10);

        assertTrue(ratingService.removeMyRating("R001"));

        List<Rating> remaining = ratingRepository.findByRestaurantId("R001");
        assertEquals(1, remaining.size());
        assertEquals(4, remaining.get(0).getScore());
    }

    @Test
    @DisplayName("Deleting a rating that was never left reports no change")
    void deletingAMissingRatingReportsNoChange() {
        loginAsSunny();

        assertFalse(ratingService.removeMyRating("R001"));
    }

    // -------------------------------------------------------------- guards

    @Test
    @DisplayName("Rating a restaurant that does not exist is rejected")
    void ratingUnknownRestaurantIsRejected() {
        loginAsSunny();

        assertThrows(IllegalArgumentException.class,
                () -> ratingService.rateRestaurant("R999", 8));

        assertTrue(ratingRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Rating requires somebody to be logged in")
    void ratingRequiresALogin() {
        assertThrows(IllegalStateException.class,
                () -> ratingService.rateRestaurant("R001", 8));
    }

    // ------------------------------------------------- rule 5: must visit first

    @Test
    @DisplayName("Rating a restaurant that has not been visited is rejected")
    void ratingAnUnvisitedRestaurantIsRejected() {
        loginAsSunny();

        assertThrows(IllegalArgumentException.class,
                () -> ratingService.rateRestaurant("R001", 8));

        assertTrue(ratingRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Visiting one restaurant does not unlock rating a different one")
    void visitingOneRestaurantDoesNotUnlockAnother() {
        loginAsSunny();
        markVisited("R001");

        assertThrows(IllegalArgumentException.class,
                () -> ratingService.rateRestaurant("R002", 8));
    }

    @Test
    @DisplayName("A restaurant can be rated once it has been visited")
    void ratingIsAllowedOnceVisited() {
        loginAsSunny();
        markVisited("R001");

        assertEquals(7, ratingService.rateRestaurant("R001", 7).getScore());
    }

    // ------------------------------------------------------------ averages

    @Test
    @DisplayName("The average is the mean of every user's score")
    void averageIsTheMeanOfAllScores() {
        loginAsSunny();
        markVisited("R001");
        ratingService.rateRestaurant("R001", 6);

        loginAsAlex();
        markVisited("R001");
        ratingService.rateRestaurant("R001", 9);

        assertEquals(7.5, ratingService.getAverageRating("R001"), 0.001);
        assertEquals(2, ratingService.getRatingCount("R001"));
    }

    @Test
    @DisplayName("An unrated restaurant has an average of zero and no ratings")
    void unratedRestaurantHasNoAverage() {
        assertEquals(0.0, ratingService.getAverageRating("R002"), 0.001);
        assertEquals(0, ratingService.getRatingCount("R002"));
    }

    @Test
    @DisplayName("Updating a rating changes the average rather than adding to it")
    void updatingARatingChangesTheAverage() {
        loginAsSunny();
        markVisited("R001");

        ratingService.rateRestaurant("R001", 2);
        ratingService.rateRestaurant("R001", 8);

        assertEquals(8.0, ratingService.getAverageRating("R001"), 0.001);
        assertEquals(1, ratingService.getRatingCount("R001"));
    }

    // ------------------------------------------------------------------ ids

    @Test
    @DisplayName("Rating IDs follow the RT001 format and increase")
    void ratingIdsAreGeneratedInSequence() {
        loginAsSunny();
        markVisited("R001");
        markVisited("R002");

        assertEquals("RT001", ratingService.rateRestaurant("R001", 5).getId());
        assertEquals("RT002", ratingService.rateRestaurant("R002", 5).getId());
    }

    @Test
    @DisplayName("A new rating never takes an ID that is still in use")
    void newRatingIdDoesNotCollideWithAnExistingOne() {
        loginAsSunny();
        markVisited("R001");
        markVisited("R002");

        ratingService.rateRestaurant("R001", 5);   // RT001
        ratingService.rateRestaurant("R002", 5);   // RT002
        ratingService.removeMyRating("R001");      // RT002 is now the only one

        // Numbering from the count of remaining ratings would produce RT002 a
        // second time. Numbering from the highest existing ID gives RT003.
        assertEquals("RT003", ratingService.rateRestaurant("R001", 7).getId());
    }

    // ---------------------------------------------------------------- fakes

    private static class FakeRestaurantRepository implements RestaurantRepository {

        private final List<Restaurant> restaurants = new ArrayList<>();

        @Override
        public List<Restaurant> findAll() {
            return new ArrayList<>(restaurants);
        }

        @Override
        public Optional<Restaurant> findById(String id) {
            return restaurants.stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst();
        }

        @Override
        public void save(Restaurant restaurant) {
            restaurants.removeIf(r -> r.getId().equals(restaurant.getId()));
            restaurants.add(restaurant);
        }

        @Override
        public void deleteById(String id) {
            restaurants.removeIf(r -> r.getId().equals(id));
        }
    }

    private static class FakeRatingRepository implements RatingRepository {

        private final List<Rating> ratings = new ArrayList<>();

        @Override
        public List<Rating> findAll() {
            return new ArrayList<>(ratings);
        }

        @Override
        public Optional<Rating> findById(String id) {
            return ratings.stream()
                    .filter(rating -> rating.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Rating> findByUserId(String userId) {
            return ratings.stream()
                    .filter(rating -> rating.belongsTo(userId))
                    .toList();
        }

        @Override
        public List<Rating> findByRestaurantId(String restaurantId) {
            return ratings.stream()
                    .filter(rating -> rating.isFor(restaurantId))
                    .toList();
        }

        @Override
        public Optional<Rating> findByUserAndRestaurant(
                String userId, String restaurantId) {

            return ratings.stream()
                    .filter(rating -> rating.belongsTo(userId)
                            && rating.isFor(restaurantId))
                    .findFirst();
        }

        @Override
        public void save(Rating rating) {
            ratings.removeIf(stored -> stored.getId().equals(rating.getId()));
            ratings.add(rating);
        }

        @Override
        public void deleteById(String id) {
            ratings.removeIf(rating -> rating.getId().equals(id));
        }

        @Override
        public void deleteByRestaurantId(String restaurantId) {
            ratings.removeIf(rating -> rating.isFor(restaurantId));
        }
    }

    private static class FakeUserRepository implements UserRepository {

        private final List<User> users = new ArrayList<>();

        @Override
        public List<User> findAll() {
            return new ArrayList<>(users);
        }

        @Override
        public Optional<User> findById(String id) {
            return users.stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<User> findByUsername(String username) {
            if (username == null || username.isBlank()) {
                return Optional.empty();
            }

            String wanted = username.trim();

            return users.stream()
                    .filter(u -> u.getUsername().equalsIgnoreCase(wanted))
                    .findFirst();
        }

        @Override
        public void save(User user) {
            users.removeIf(u -> u.getId().equals(user.getId()));
            users.add(user);
        }

        @Override
        public void deleteById(String id) {
            users.removeIf(u -> u.getId().equals(id));
        }
    }
}
