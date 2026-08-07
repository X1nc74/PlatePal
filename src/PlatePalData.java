package platepal.persistence;

import java.util.ArrayList;
import java.util.List;

import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.model.Review;
import platepal.model.User;

/**
 * The in-memory mirror of {@code data/platepal-data.json}. Gson serializes this
 * one object to produce the whole file, which is why the field names must match
 * the agreed JSON keys exactly.
 *
 * <p>Every list is initialized and re-checked in {@link #repair()} so that a
 * missing or empty data file produces empty lists instead of a
 * NullPointerException (final testing checklist, last item).
 */
public class PlatePalData {

    private List<User> users = new ArrayList<>();
    private List<Restaurant> restaurants = new ArrayList<>();
    private List<Rating> ratings = new ArrayList<>();
    private List<Review> reviews = new ArrayList<>();

    public List<User> getUsers() {
        return users;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    /**
     * Called immediately after loading. Gson leaves a field null when the JSON
     * key is absent, so the initializers above are not enough on their own.
     */
    public void repair() {
        if (users == null) {
            users = new ArrayList<>();
        }
        if (restaurants == null) {
            restaurants = new ArrayList<>();
        }
        if (ratings == null) {
            ratings = new ArrayList<>();
        }
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        users.forEach(User::repairAfterDeserialization);
    }
}
