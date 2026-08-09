package platepal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import platepal.model.Restaurant;
import platepal.model.User;
import platepal.repository.RestaurantRepository;
import platepal.repository.UserRepository;

/**
 * The logged-in user's Visited and Want to Try lists.
 */
public class PersonalListService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuthService authService;

    public PersonalListService(UserRepository userRepository,
                               RestaurantRepository restaurantRepository,
                               AuthService authService) {

        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.authService = authService;
    }

    /**
     * Marks a restaurant as visited.
     *
     * <p>Business rules 3 and 4 are enforced by {@link User#markAsVisited}, which
     * drops the restaurant from Want to Try so it is never in both lists. This
     * method only adds what the model cannot check for itself: that the
     * restaurant actually exists.
     *
     * @return true if it was not already in the Visited list
     * @throws IllegalArgumentException if no such restaurant exists
     * @throws IllegalStateException    if nobody is logged in
     */
    public boolean markAsVisited(String restaurantId) {
        String id = requireRestaurant(restaurantId);
        User user = authService.requireCurrentUser();

        boolean changed = user.markAsVisited(id);
        persist(user);

        return changed;
    }

    /**
     * Adds a restaurant to Want to Try. An already visited restaurant is not
     * added back, so the two lists stay disjoint.
     *
     * @return true if it was added
     * @throws IllegalArgumentException if no such restaurant exists
     * @throws IllegalStateException    if nobody is logged in
     */
    public boolean addToWantToTry(String restaurantId) {
        String id = requireRestaurant(restaurantId);
        User user = authService.requireCurrentUser();

        boolean changed = user.addToWantToTry(id);
        persist(user);

        return changed;
    }

    /** @return true if the restaurant was in the Visited list and was removed */
    public boolean removeFromVisited(String restaurantId) {
        User user = authService.requireCurrentUser();

        boolean changed = user.removeFromVisited(restaurantId);
        persist(user);

        return changed;
    }

    /** @return true if the restaurant was in Want to Try and was removed */
    public boolean removeFromWantToTry(String restaurantId) {
        User user = authService.requireCurrentUser();

        boolean changed = user.removeFromWantToTry(restaurantId);
        persist(user);

        return changed;
    }

    public List<Restaurant> getVisitedRestaurants() {
        return resolve(authService.requireCurrentUser().getVisitedRestaurantIds());
    }

    public List<Restaurant> getWantToTryRestaurants() {
        return resolve(authService.requireCurrentUser().getWantToTryRestaurantIds());
    }

    public boolean hasVisited(String restaurantId) {
        return authService.requireCurrentUser().hasVisited(restaurantId);
    }

    public boolean wantsToTry(String restaurantId) {
        return authService.requireCurrentUser().wantsToTry(restaurantId);
    }

    /**
     * Turns stored IDs into restaurants, in the order they were added.
     *
     * <p>An ID with no matching restaurant is skipped rather than reported. That
     * should not happen, because deleting a restaurant clears it from every
     * list, but a hand-edited data file should not crash the menu.
     */
    private List<Restaurant> resolve(Set<String> restaurantIds) {
        List<Restaurant> restaurants = new ArrayList<>();

        for (String id : restaurantIds) {
            restaurantRepository.findById(id).ifPresent(restaurants::add);
        }

        return restaurants;
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
     * Saves the change and refreshes the session, so the object the user is
     * holding matches what is now on disk.
     */
    private void persist(User user) {
        userRepository.save(user);
        authService.refreshCurrentUser();
    }
}
