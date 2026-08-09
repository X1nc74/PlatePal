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

import platepal.exception.PermissionDeniedException;
import platepal.model.PriceCategory;
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.model.Review;
import platepal.model.User;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.repository.ReviewRepository;
import platepal.repository.UserRepository;
import platepal.service.AdminService;
import platepal.service.AuthService;
import platepal.service.UserService;

public class AdminServiceTest {

    private FakeRestaurantRepository restaurantRepository;
    private FakeRatingRepository ratingRepository;
    private FakeReviewRepository reviewRepository;
    private UserRepository userRepository;
    private AuthService authService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        restaurantRepository = new FakeRestaurantRepository();
        ratingRepository = new FakeRatingRepository();
        reviewRepository = new FakeReviewRepository();
        userRepository = new FakeUserRepository();

        UserService userService = new UserService(userRepository);
        userService.register("sunny", "example");
        userService.registerAdministrator("boss", "adminpass");

        authService = new AuthService(userRepository);

        adminService = new AdminService(
                restaurantRepository,
                ratingRepository,
                reviewRepository,
                userRepository,
                authService);
    }

    private void loginAsAdmin() {
        authService.login("boss", "adminpass");
    }

    private void loginAsRegularUser() {
        authService.login("sunny", "example");
    }

    // ------------------------------------------------------------ permissions

    @Test
    @DisplayName("A regular user cannot add, update or remove restaurants")
    void regularUserCannotManageRestaurants() {
        loginAsAdmin();
        adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, "");

        loginAsRegularUser();

        assertThrows(PermissionDeniedException.class, () -> adminService.addRestaurant(
                "Sneaky", "Pizza", "New York", PriceCategory.BUDGET, ""));

        assertThrows(PermissionDeniedException.class, () -> adminService.updateRestaurant(
                "R001", "Renamed", "Pizza", "New York", PriceCategory.BUDGET, ""));

        assertThrows(PermissionDeniedException.class,
                () -> adminService.removeRestaurant("R001"));

        assertEquals(1, restaurantRepository.findAll().size());
        assertEquals("Joe's Pizza", restaurantRepository.findAll().get(0).getName());
    }

    @Test
    @DisplayName("A logged-out visitor cannot manage restaurants")
    void loggedOutVisitorCannotManageRestaurants() {
        assertThrows(PermissionDeniedException.class, () -> adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, ""));
    }

    @Test
    @DisplayName("Permission is checked before the details are validated")
    void permissionIsCheckedBeforeValidation() {
        loginAsRegularUser();

        assertThrows(PermissionDeniedException.class, () -> adminService.addRestaurant(
                "", "", "", null, ""));
    }

    // ------------------------------------------------------------------- add

    @Test
    @DisplayName("An administrator can add a restaurant")
    void administratorCanAddRestaurant() {
        loginAsAdmin();

        Restaurant added = adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York",
                PriceCategory.BUDGET, "Cheap and good.");

        assertEquals("R001", added.getId());
        assertEquals("Joe's Pizza", added.getName());
        assertEquals("Cheap and good.", added.getDescription());
        assertTrue(restaurantRepository.findById("R001").isPresent());
    }

    @Test
    @DisplayName("Restaurant IDs follow the R001 format and increase")
    void restaurantIdsAreGeneratedInSequence() {
        loginAsAdmin();

        assertEquals("R001", adminService.addRestaurant(
                "A", "Pizza", "NY", PriceCategory.BUDGET, "").getId());
        assertEquals("R002", adminService.addRestaurant(
                "B", "Sushi", "NY", PriceCategory.EXPENSIVE, "").getId());
        assertEquals("R003", adminService.addRestaurant(
                "C", "Pasta", "Boston", PriceCategory.MODERATE, "").getId());
    }

    @Test
    @DisplayName("A restaurant without a name is rejected")
    void restaurantWithoutNameIsRejected() {
        loginAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> adminService.addRestaurant(
                "  ", "Pizza", "New York", PriceCategory.BUDGET, ""));

        assertTrue(restaurantRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("A restaurant without a price category is rejected")
    void restaurantWithoutPriceCategoryIsRejected() {
        loginAsAdmin();

        assertThrows(NullPointerException.class, () -> adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", null, ""));
    }

    // ---------------------------------------------------------------- update

    @Test
    @DisplayName("An administrator can update a restaurant without changing its ID")
    void administratorCanUpdateRestaurant() {
        loginAsAdmin();
        adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, "");

        Restaurant updated = adminService.updateRestaurant(
                "R001", "Joe's Pizzeria", "Italian", "Brooklyn",
                PriceCategory.MODERATE, "Now with pasta.");

        assertEquals("R001", updated.getId());
        assertEquals("Joe's Pizzeria", updated.getName());
        assertEquals("Italian", updated.getCuisine());
        assertEquals("Brooklyn", updated.getLocation());
        assertEquals(PriceCategory.MODERATE, updated.getPriceCategory());
        assertEquals("Now with pasta.", updated.getDescription());
        assertEquals(1, restaurantRepository.findAll().size());
    }

    @Test
    @DisplayName("Updating a restaurant that does not exist is rejected")
    void updatingUnknownRestaurantIsRejected() {
        loginAsAdmin();

        assertThrows(IllegalArgumentException.class, () -> adminService.updateRestaurant(
                "R999", "Ghost", "Pizza", "NY", PriceCategory.BUDGET, ""));
    }

    // ---------------------------------------------------------------- remove

    @Test
    @DisplayName("An administrator can remove a restaurant")
    void administratorCanRemoveRestaurant() {
        loginAsAdmin();
        adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, "");

        adminService.removeRestaurant("R001");

        assertTrue(restaurantRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Removing a restaurant that does not exist is rejected")
    void removingUnknownRestaurantIsRejected() {
        loginAsAdmin();

        assertThrows(IllegalArgumentException.class,
                () -> adminService.removeRestaurant("R999"));
    }

    @Test
    @DisplayName("Removing a restaurant deletes its ratings")
    void removingRestaurantDeletesItsRatings() {
        loginAsAdmin();
        adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, "");
        adminService.addRestaurant(
                "Pasta House", "Italian", "Boston", PriceCategory.MODERATE, "");

        ratingRepository.add(new Rating("RT001", "U001", "R001", 9));
        ratingRepository.add(new Rating("RT002", "U001", "R002", 7));

        adminService.removeRestaurant("R001");

        assertTrue(ratingRepository.findByRestaurantId("R001").isEmpty());
        assertEquals(1, ratingRepository.findByRestaurantId("R002").size());
    }

    @Test
    @DisplayName("Removing a restaurant deletes its reviews")
    void removingRestaurantDeletesItsReviews() {
        loginAsAdmin();
        adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, "");
        adminService.addRestaurant(
                "Pasta House", "Italian", "Boston", PriceCategory.MODERATE, "");

        reviewRepository.add(new Review("RV001", "U001", "R001", "Great."));
        reviewRepository.add(new Review("RV002", "U001", "R002", "Fine."));

        adminService.removeRestaurant("R001");

        assertTrue(reviewRepository.findByRestaurantId("R001").isEmpty());
        assertEquals(1, reviewRepository.findByRestaurantId("R002").size());
    }

    @Test
    @DisplayName("Removing a restaurant clears it from every user's lists")
    void removingRestaurantClearsItFromUserLists() {
        loginAsAdmin();
        adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, "");
        adminService.addRestaurant(
                "Pasta House", "Italian", "Boston", PriceCategory.MODERATE, "");

        User sunny = userRepository.findByUsername("sunny").orElseThrow();
        sunny.markAsVisited("R001");
        sunny.addToWantToTry("R002");
        userRepository.save(sunny);

        adminService.removeRestaurant("R001");

        User reloaded = userRepository.findByUsername("sunny").orElseThrow();
        assertFalse(reloaded.hasVisited("R001"));
        assertTrue(reloaded.wantsToTry("R002"));
    }

    @Test
    @DisplayName("Removing a restaurant refreshes the administrator's own session")
    void removingRestaurantRefreshesTheSession() {
        loginAsAdmin();
        adminService.addRestaurant(
                "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET, "");

        User admin = authService.requireCurrentUser();
        admin.markAsVisited("R001");
        userRepository.save(admin);

        adminService.removeRestaurant("R001");

        assertFalse(authService.requireCurrentUser().hasVisited("R001"));
    }

    // ------------------------------------------------------------------ fakes

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

    private static class FakeReviewRepository implements ReviewRepository {

        private final List<Review> reviews = new ArrayList<>();

        void add(Review review) {
            reviews.add(review);
        }

        @Override
        public List<Review> findAll() {
            return new ArrayList<>(reviews);
        }

        @Override
        public Optional<Review> findById(String id) {
            return reviews.stream()
                    .filter(review -> review.getId().equals(id))
                    .findFirst();
        }

        @Override
        public List<Review> findByUserId(String userId) {
            return reviews.stream()
                    .filter(review -> review.belongsTo(userId))
                    .toList();
        }

        @Override
        public List<Review> findByRestaurantId(String restaurantId) {
            return reviews.stream()
                    .filter(review -> review.isFor(restaurantId))
                    .toList();
        }

        @Override
        public Optional<Review> findByUserAndRestaurant(
                String userId, String restaurantId) {

            return reviews.stream()
                    .filter(review -> review.belongsTo(userId)
                            && review.isFor(restaurantId))
                    .findFirst();
        }

        @Override
        public void save(Review review) {
            reviews.removeIf(stored -> stored.getId().equals(review.getId()));
            reviews.add(review);
        }

        @Override
        public void deleteById(String id) {
            reviews.removeIf(review -> review.getId().equals(id));
        }

        @Override
        public void deleteByRestaurantId(String restaurantId) {
            reviews.removeIf(review -> review.isFor(restaurantId));
        }
    }

    private static class FakeRatingRepository implements RatingRepository {

        private final List<Rating> ratings = new ArrayList<>();

        void add(Rating rating) {
            ratings.add(rating);
        }

        @Override
        public List<Rating> findAll() {
            return new ArrayList<>(ratings);
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
        public Optional<Rating> findById(String id) {
            return ratings.stream()
                    .filter(rating -> rating.getId().equals(id))
                    .findFirst();
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
