package platepal.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import platepal.model.Rating;
import platepal.persistence.JsonDataStore;
import platepal.persistence.PlatePalData;

public class JsonRatingRepository implements RatingRepository {

    private final JsonDataStore dataStore;

    public JsonRatingRepository() {
        this.dataStore = new JsonDataStore();
    }

    public JsonRatingRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<Rating> findAll() {
        PlatePalData data = dataStore.load();
        return new ArrayList<>(data.getRatings());
    }

    @Override
    public Optional<Rating> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return findAll()
                .stream()
                .filter(rating -> rating.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Rating> findByUserId(String userId) {
        return findAll()
                .stream()
                .filter(rating -> rating.belongsTo(userId))
                .toList();
    }

    @Override
    public List<Rating> findByRestaurantId(String restaurantId) {
        return findAll()
                .stream()
                .filter(rating -> rating.isFor(restaurantId))
                .toList();
    }

    @Override
    public Optional<Rating> findByUserAndRestaurant(String userId, String restaurantId) {
        if (userId == null || restaurantId == null) {
            return Optional.empty();
        }

        return findAll()
                .stream()
                .filter(rating -> rating.belongsTo(userId) && rating.isFor(restaurantId))
                .findFirst();
    }

    @Override
    public void save(Rating rating) {
        PlatePalData data = dataStore.load();

        List<Rating> ratings = data.getRatings();

        Optional<Rating> existing = ratings.stream()
                .filter(r -> r.getId().equals(rating.getId()))
                .findFirst();

        if (existing.isPresent()) {
            ratings.set(ratings.indexOf(existing.get()), rating);
        } else {
            ratings.add(rating);
        }

        dataStore.save(data);
    }

    @Override
    public void deleteById(String id) {
        PlatePalData data = dataStore.load();

        data.getRatings()
                .removeIf(rating -> rating.getId().equals(id));

        dataStore.save(data);
    }

    @Override
    public void deleteByRestaurantId(String restaurantId) {
        PlatePalData data = dataStore.load();

        data.getRatings()
                .removeIf(rating -> rating.isFor(restaurantId));

        dataStore.save(data);
    }
}
