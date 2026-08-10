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
import platepal.service.PersonalListService;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;
import platepal.service.SocialService;
import platepal.service.UserService;

public class SocialServiceTest {

    private UserRepository userRepository;
    private FakeRatingRepository ratingRepository;
    private AuthService authService;
    private SocialService socialService;
    private PersonalListService listService;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        RestaurantRepository restaurantRepository = new FakeRestaurantRepository();

        restaurantRepository.save(new Restaurant(
                "R001", "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET));
        restaurantRepository.save(new Restaurant(
                "R002", "Sushi Nakazawa", "Japanese", "New York", PriceCategory.EXPENSIVE));

        UserService userService = new UserService(userRepository);
        userService.register("sunny", "example");
        userService.register("nicole", "example");
        userService.register("xinpeng", "example");

        authService = new AuthService(userRepository);
        authService.login("sunny", "example");

        ratingRepository = new FakeRatingRepository();

        ratingService = new RatingService(
                ratingRepository, restaurantRepository, authService);

        PersonalRankingService personalRankingService =
                new PersonalRankingService(ratingService, restaurantRepository);

        socialService = new SocialService(
                userRepository, restaurantRepository, ratingRepository,
                personalRankingService, authService);

        listService = new PersonalListService(
                userRepository, restaurantRepository, authService);
    }

    /** Visits and rates a restaurant as whoever is currently logged in. */
    private void visitAndRate(String restaurantId, int score) {
        listService.markAsVisited(restaurantId);
        ratingService.rateRestaurant(restaurantId, score);
    }

    // ------------------------------------------------------------ following

    @Test
    @DisplayName("A user can follow another user")
    void userCanFollowAnotherUser() {
        assertTrue(socialService.follow("nicole"));

        assertTrue(socialService.isFollowing("nicole"));
        assertEquals(1, socialService.getFollowing().size());
        assertEquals("nicole", socialService.getFollowing().get(0).getUsername());
    }

    @Test
    @DisplayName("Following the same user twice changes nothing")
    void followingTwiceChangesNothing() {
        assertTrue(socialService.follow("nicole"));
        assertFalse(socialService.follow("nicole"));

        assertEquals(1, socialService.getFollowing().size());
    }

    @Test
    @DisplayName("Following works regardless of how the username is capitalised")
    void followingIgnoresUsernameCase() {
        assertTrue(socialService.follow("NICOLE"));
        assertTrue(socialService.isFollowing("nicole"));
    }

    @Test
    @DisplayName("A user cannot follow themselves")
    void userCannotFollowThemselves() {
        assertThrows(IllegalArgumentException.class,
                () -> socialService.follow("sunny"));

        assertTrue(socialService.getFollowing().isEmpty());
    }

    @Test
    @DisplayName("Following a user who does not exist is rejected")
    void followingUnknownUserIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> socialService.follow("nobody"));
    }

    @Test
    @DisplayName("A user can unfollow somebody they follow")
    void userCanUnfollow() {
        socialService.follow("nicole");

        assertTrue(socialService.unfollow("nicole"));
        assertFalse(socialService.isFollowing("nicole"));
        assertTrue(socialService.getFollowing().isEmpty());
    }

    @Test
    @DisplayName("Unfollowing somebody who was not followed reports no change")
    void unfollowingSomebodyNotFollowedReportsNoChange() {
        assertFalse(socialService.unfollow("nicole"));
    }

    @Test
    @DisplayName("Following is one-way and needs no approval")
    void followingIsOneWay() {
        socialService.follow("nicole");

        authService.login("nicole", "example");

        assertTrue(socialService.getFollowing().isEmpty());
        assertEquals(1, socialService.getFollowers().size());
        assertEquals("sunny", socialService.getFollowers().get(0).getUsername());
    }

    @Test
    @DisplayName("Follows are saved, not just held in memory")
    void followsArePersisted() {
        socialService.follow("nicole");

        User stored = userRepository.findByUsername("sunny").orElseThrow();
        User nicole = userRepository.findByUsername("nicole").orElseThrow();

        assertTrue(stored.isFollowing(nicole.getId()));
    }

    @Test
    @DisplayName("Following keeps the order users were followed in")
    void followingKeepsOrder() {
        socialService.follow("xinpeng");
        socialService.follow("nicole");

        List<String> names = socialService.getFollowing()
                .stream().map(User::getUsername).toList();

        assertEquals(List.of("xinpeng", "nicole"), names);
    }

    // ----------------------------------------------------- browsing others

    @Test
    @DisplayName("Browsing users excludes the person logged in")
    void browsingExcludesTheCurrentUser() {
        List<String> names = socialService.getOtherUsers()
                .stream().map(User::getUsername).toList();

        assertEquals(2, names.size());
        assertTrue(names.contains("nicole"));
        assertTrue(names.contains("xinpeng"));
        assertFalse(names.contains("sunny"));
    }

    @Test
    @DisplayName("Another user's lists can be viewed")
    void anotherUsersListsCanBeViewed() {
        authService.login("nicole", "example");
        listService.markAsVisited("R001");
        listService.addToWantToTry("R002");

        authService.login("sunny", "example");

        List<Restaurant> visited = socialService.getVisitedRestaurants("nicole");
        List<Restaurant> wantToTry = socialService.getWantToTryRestaurants("nicole");

        assertEquals(1, visited.size());
        assertEquals("Joe's Pizza", visited.get(0).getName());
        assertEquals(1, wantToTry.size());
        assertEquals("Sushi Nakazawa", wantToTry.get(0).getName());
    }

    @Test
    @DisplayName("Viewing lists does not require following that user")
    void viewingListsDoesNotRequireFollowing() {
        authService.login("nicole", "example");
        listService.markAsVisited("R001");

        authService.login("sunny", "example");

        assertFalse(socialService.isFollowing("nicole"));
        assertEquals(1, socialService.getVisitedRestaurants("nicole").size());
    }

    @Test
    @DisplayName("Viewing the lists of a user who does not exist is rejected")
    void viewingUnknownUsersListsIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> socialService.getVisitedRestaurants("nobody"));
    }

    @Test
    @DisplayName("Nobody logged in means no social operations")
    void loggedOutUserCannotUseSocialFeatures() {
        authService.logout();

        assertThrows(IllegalStateException.class,
                () -> socialService.follow("nicole"));
        assertThrows(IllegalStateException.class,
                () -> socialService.getFollowing());
    }

    // -------------------------------------------------------- highest rated

    @Test
    @DisplayName("A user's highest-rated restaurants are ordered best score first")
    void highestRatedRestaurantsAreOrderedByScore() {
        authService.login("nicole", "example");
        visitAndRate("R001", 6);
        visitAndRate("R002", 9);

        authService.login("sunny", "example");

        List<Restaurant> ranking = socialService.getHighestRatedRestaurants("nicole");

        assertEquals(2, ranking.size());
        assertEquals("Sushi Nakazawa", ranking.get(0).getName());
        assertEquals("Joe's Pizza", ranking.get(1).getName());
    }

    @Test
    @DisplayName("Viewing highest-rated restaurants does not require following that user")
    void highestRatedDoesNotRequireFollowing() {
        authService.login("nicole", "example");
        visitAndRate("R001", 8);

        authService.login("sunny", "example");

        assertFalse(socialService.isFollowing("nicole"));
        assertEquals(1, socialService.getHighestRatedRestaurants("nicole").size());
    }

    @Test
    @DisplayName("A user with no ratings has an empty highest-rated list")
    void userWithNoRatingsHasEmptyRanking() {
        assertTrue(socialService.getHighestRatedRestaurants("nicole").isEmpty());
    }

    @Test
    @DisplayName("Viewing the ranking of a user who does not exist is rejected")
    void viewingUnknownUsersRankingIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> socialService.getHighestRatedRestaurants("nobody"));
    }

    // ---------------------------------------------------------- friend activity

    @Test
    @DisplayName("Friend activity shows ratings only from followed users")
    void friendActivityShowsOnlyFollowedUsersRatings() {
        socialService.follow("nicole");

        authService.login("nicole", "example");
        visitAndRate("R001", 7);

        authService.login("xinpeng", "example");
        visitAndRate("R002", 5);

        authService.login("sunny", "example");

        List<Rating> activity = socialService.getRecentActivityFromFollowedUsers();

        assertEquals(1, activity.size());
        assertEquals("R001", activity.get(0).getRestaurantId());
    }

    @Test
    @DisplayName("Friend activity is empty when the user follows nobody")
    void friendActivityIsEmptyWithNoFollows() {
        assertTrue(socialService.getRecentActivityFromFollowedUsers().isEmpty());
    }

    @Test
    @DisplayName("Friend activity lists the most recently changed rating first")
    void friendActivityOrdersMostRecentFirst() {
        socialService.follow("nicole");

        authService.login("nicole", "example");
        visitAndRate("R001", 4);
        visitAndRate("R002", 9);
        ratingService.rateRestaurant("R001", 8); // updates R001, now most recent

        authService.login("sunny", "example");

        List<Rating> activity = socialService.getRecentActivityFromFollowedUsers();

        assertEquals(2, activity.size());
        assertEquals("R001", activity.get(0).getRestaurantId());
        assertEquals(8, activity.get(0).getScore());
    }

    @Test
    @DisplayName("Friend activity requires somebody to be logged in")
    void friendActivityRequiresALogin() {
        authService.logout();

        assertThrows(IllegalStateException.class,
                () -> socialService.getRecentActivityFromFollowedUsers());
    }

    @Test
    @DisplayName("Usernames resolve back from a rating's user ID")
    void usernameResolvesFromUserId() {
        User nicole = userRepository.findByUsername("nicole").orElseThrow();

        assertEquals("nicole", socialService.getUsername(nicole.getId()));
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
