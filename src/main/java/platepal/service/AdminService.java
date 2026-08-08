package platepal.service;

import platepal.exception.PermissionDeniedException;
import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.model.User;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.repository.UserRepository;

/**
 * Restaurant management, restricted to administrators.
 */
public class AdminService {

    private static final String ID_PREFIX = "R";

    private final RestaurantRepository restaurantRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminService(RestaurantRepository restaurantRepository,
                        RatingRepository ratingRepository,
                        UserRepository userRepository,
                        AuthService authService) {

        this.restaurantRepository = restaurantRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /**
     * Adds a restaurant and returns it with its generated ID.
     *
     * <p>The {@link Restaurant} constructor rejects empty names, cuisines and
     * locations, so this method only has to deal with permission and the ID.
     *
     * @throws PermissionDeniedException if the current user is not an administrator
     * @throws IllegalArgumentException  if any detail is invalid
     */
    public Restaurant addRestaurant(String name,
                                    String cuisine,
                                    String location,
                                    PriceCategory priceCategory,
                                    String description) {

        requireAdministrator();

        Restaurant restaurant = new Restaurant(
                nextRestaurantId(), name, cuisine, location, priceCategory);

        restaurant.setDescription(description);
        restaurantRepository.save(restaurant);

        return restaurant;
    }

    /**
     * Replaces the details of an existing restaurant. The ID never changes,
     * because ratings, reviews and personal lists all refer to it.
     *
     * @throws PermissionDeniedException if the current user is not an administrator
     * @throws IllegalArgumentException  if no such restaurant exists, or a
     *                                   detail is invalid
     */
    public Restaurant updateRestaurant(String restaurantId,
                                       String name,
                                       String cuisine,
                                       String location,
                                       PriceCategory priceCategory,
                                       String description) {

        requireAdministrator();

        Restaurant restaurant = requireRestaurant(restaurantId);

        restaurant.setName(name);
        restaurant.setCuisine(cuisine);
        restaurant.setLocation(location);
        restaurant.setPriceCategory(priceCategory);
        restaurant.setDescription(description);

        restaurantRepository.save(restaurant);

        return restaurant;
    }

    /**
     * Deletes a restaurant and everything that pointed at it.
     *
     * <p>Ratings for the restaurant are deleted, and it is removed from every
     * user's Visited and Want to Try lists. 
     *
     * @throws PermissionDeniedException if the current user is not an administrator
     * @throws IllegalArgumentException  if no such restaurant exists
     */
    public void removeRestaurant(String restaurantId) {
        requireAdministrator();

        Restaurant restaurant = requireRestaurant(restaurantId);
        String id = restaurant.getId();

        restaurantRepository.deleteById(id);
        ratingRepository.deleteByRestaurantId(id);

        for (User user : userRepository.findAll()) {
            if (user.hasVisited(id) || user.wantsToTry(id)) {
                user.forgetRestaurant(id);
                userRepository.save(user);
            }
        }

        // The session holds a copy loaded at login; without this the admin's own
        // lists would still show the restaurant until they log in again.
        authService.refreshCurrentUser();
    }

    private void requireAdministrator() {
        if (!authService.isCurrentUserAdministrator()) {
            throw new PermissionDeniedException(
                    "Only administrators may manage restaurants.");
        }
    }

    private Restaurant requireRestaurant(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No restaurant found with id " + restaurantId + "."));
    }

    /**
     * Produces the next ID in the {@code R001} format, from the highest existing
     * ID rather than the number of restaurants, so a deleted entry's ID is not
     * given to a different restaurant later.
     */
    private String nextRestaurantId() {
        int highest = 0;

        for (Restaurant restaurant : restaurantRepository.findAll()) {
            highest = Math.max(highest, numericSuffix(restaurant.getId()));
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
