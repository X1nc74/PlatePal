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
import platepal.service.UserService;

public class PersonalListServiceTest {

    private UserRepository userRepository;
    private AuthService authService;
    private PersonalListService listService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        RestaurantRepository restaurantRepository = new FakeRestaurantRepository();

        restaurantRepository.save(new Restaurant(
                "R001", "Joe's Pizza", "Pizza", "New York", PriceCategory.BUDGET));
        restaurantRepository.save(new Restaurant(
                "R002", "Sushi Nakazawa", "Japanese", "New York", PriceCategory.EXPENSIVE));
        restaurantRepository.save(new Restaurant(
                "R003", "Pasta House", "Italian", "Boston", PriceCategory.MODERATE));

        new UserService(userRepository).register("sunny", "example");

        authService = new AuthService(userRepository);
        authService.login("sunny", "example");

        listService = new PersonalListService(
                userRepository, restaurantRepository, authService);
    }

    // -------------------------------------------------------------- adding

    @Test
    @DisplayName("A restaurant can be added to Want to Try")
    void restaurantCanBeAddedToWantToTry() {
        assertTrue(listService.addToWantToTry("R001"));

        assertEquals(1, listService.getWantToTryRestaurants().size());
        assertEquals("Joe's Pizza",
                listService.getWantToTryRestaurants().get(0).getName());
    }

    @Test
    @DisplayName("Adding the same restaurant to Want to Try twice changes nothing")
    void addingToWantToTryTwiceChangesNothing() {
        assertTrue(listService.addToWantToTry("R001"));
        assertFalse(listService.addToWantToTry("R001"));

        assertEquals(1, listService.getWantToTryRestaurants().size());
    }

    @Test
    @DisplayName("A restaurant can be marked as Visited")
    void restaurantCanBeMarkedAsVisited() {
        assertTrue(listService.markAsVisited("R001"));

        assertTrue(listService.hasVisited("R001"));
        assertEquals(1, listService.getVisitedRestaurants().size());
    }

    @Test
    @DisplayName("A restaurant that does not exist cannot be added to either list")
    void unknownRestaurantIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> listService.addToWantToTry("R999"));
        assertThrows(IllegalArgumentException.class,
                () -> listService.markAsVisited("R999"));

        assertTrue(listService.getWantToTryRestaurants().isEmpty());
        assertTrue(listService.getVisitedRestaurants().isEmpty());
    }

    @Test
    @DisplayName("Surrounding whitespace in an ID is ignored")
    void restaurantIdIsTrimmed() {
        assertTrue(listService.addToWantToTry("  R001  "));
        assertTrue(listService.wantsToTry("R001"));
    }

    // ----------------------------------------------------- the two rules

    @Test
    @DisplayName("Marking a restaurant Visited removes it from Want to Try")
    void visitedRemovesFromWantToTry() {
        listService.addToWantToTry("R001");
        listService.markAsVisited("R001");

        assertTrue(listService.hasVisited("R001"));
        assertFalse(listService.wantsToTry("R001"));
        assertTrue(listService.getWantToTryRestaurants().isEmpty());
    }

    @Test
    @DisplayName("An already visited restaurant cannot go back to Want to Try")
    void visitedRestaurantCannotReturnToWantToTry() {
        listService.markAsVisited("R001");

        assertFalse(listService.addToWantToTry("R001"));
        assertFalse(listService.wantsToTry("R001"));
    }

    @Test
    @DisplayName("A restaurant is never in both lists at once")
    void aRestaurantIsNeverInBothLists() {
        listService.addToWantToTry("R001");
        listService.addToWantToTry("R002");
        listService.markAsVisited("R001");

        List<String> visited = listService.getVisitedRestaurants()
                .stream().map(Restaurant::getId).toList();
        List<String> wantToTry = listService.getWantToTryRestaurants()
                .stream().map(Restaurant::getId).toList();

        assertEquals(List.of("R001"), visited);
        assertEquals(List.of("R002"), wantToTry);
    }

    // ------------------------------------------------------------ removing

    @Test
    @DisplayName("A restaurant can be removed from each list")
    void restaurantsCanBeRemoved() {
        listService.addToWantToTry("R002");
        listService.markAsVisited("R001");

        assertTrue(listService.removeFromWantToTry("R002"));
        assertTrue(listService.removeFromVisited("R001"));

        assertTrue(listService.getWantToTryRestaurants().isEmpty());
        assertTrue(listService.getVisitedRestaurants().isEmpty());
    }

    @Test
    @DisplayName("Removing a restaurant that is not in the list reports no change")
    void removingSomethingNotInTheListReportsNoChange() {
        assertFalse(listService.removeFromVisited("R001"));
        assertFalse(listService.removeFromWantToTry("R001"));
    }

    // --------------------------------------------------------- persistence

    @Test
    @DisplayName("List changes are saved, not just held in memory")
    void changesArePersisted() {
        listService.addToWantToTry("R002");
        listService.markAsVisited("R001");

        User stored = userRepository.findByUsername("sunny").orElseThrow();

        assertTrue(stored.hasVisited("R001"));
        assertTrue(stored.wantsToTry("R002"));
    }

    @Test
    @DisplayName("Lists keep the order restaurants were added in")
    void listsKeepInsertionOrder() {
        listService.addToWantToTry("R003");
        listService.addToWantToTry("R001");
        listService.addToWantToTry("R002");

        List<String> ids = listService.getWantToTryRestaurants()
                .stream().map(Restaurant::getId).toList();

        assertEquals(List.of("R003", "R001", "R002"), ids);
    }

    @Test
    @DisplayName("Nobody logged in means no list operations")
    void loggedOutUserCannotUseLists() {
        authService.logout();

        assertThrows(IllegalStateException.class,
                () -> listService.addToWantToTry("R001"));
        assertThrows(IllegalStateException.class,
                () -> listService.getVisitedRestaurants());
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
