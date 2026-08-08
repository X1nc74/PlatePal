package platepal.repository;

import java.util.ArrayList;
import java.util.List;

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
    public void deleteByRestaurantId(String restaurantId) {
        PlatePalData data = dataStore.load();

        data.getRatings()
                .removeIf(rating -> rating.isFor(restaurantId));

        dataStore.save(data);
    }
}