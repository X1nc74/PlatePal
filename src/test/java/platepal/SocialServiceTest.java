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
import platepal.model.User;
import platepal.repository.RestaurantRepository;
import platepal.repository.UserRepository;
import platepal.service.AuthService;
import platepal.service.PersonalListService;
import platepal.service.SocialService;
import platepal.service.UserService;

public class SocialServiceTest {

    private UserRepository userRepository;
    private AuthService authService;
    private SocialService socialService;
    private PersonalListService listService;

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

        socialService = new SocialService(
                userRepository, restaurantRepository, authService);

        listService = new PersonalListService(
                userRepository, restaurantRepository, authService);
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
