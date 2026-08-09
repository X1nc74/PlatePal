package platepal.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import platepal.model.Review;
import platepal.persistence.JsonDataStore;
import platepal.persistence.PlatePalData;

public class JsonReviewRepository implements ReviewRepository {

    private final JsonDataStore dataStore;

    public JsonReviewRepository() {
        this.dataStore = new JsonDataStore();
    }

    /** Lets tests point the repository at a temporary file. */
    public JsonReviewRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<Review> findAll() {
        PlatePalData data = dataStore.load();
        return new ArrayList<>(data.getReviews());
    }

    @Override
    public Optional<Review> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return findAll()
                .stream()
                .filter(review -> review.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Review> findByUserId(String userId) {
        return findAll()
                .stream()
                .filter(review -> review.belongsTo(userId))
                .toList();
    }

    @Override
    public List<Review> findByRestaurantId(String restaurantId) {
        return findAll()
                .stream()
                .filter(review -> review.isFor(restaurantId))
                .toList();
    }

    @Override
    public Optional<Review> findByUserAndRestaurant(String userId, String restaurantId) {
        if (userId == null || restaurantId == null) {
            return Optional.empty();
        }

        return findAll()
                .stream()
                .filter(review -> review.belongsTo(userId) && review.isFor(restaurantId))
                .findFirst();
    }

    @Override
    public void save(Review review) {
        PlatePalData data = dataStore.load();

        List<Review> reviews = data.getReviews();

        Optional<Review> existing = reviews.stream()
                .filter(r -> r.getId().equals(review.getId()))
                .findFirst();

        if (existing.isPresent()) {
            reviews.set(reviews.indexOf(existing.get()), review);
        } else {
            reviews.add(review);
        }

        dataStore.save(data);
    }

    @Override
    public void deleteById(String id) {
        PlatePalData data = dataStore.load();

        data.getReviews()
                .removeIf(review -> review.getId().equals(id));

        dataStore.save(data);
    }

    @Override
    public void deleteByRestaurantId(String restaurantId) {
        PlatePalData data = dataStore.load();

        data.getReviews()
                .removeIf(review -> review.isFor(restaurantId));

        dataStore.save(data);
    }
}
