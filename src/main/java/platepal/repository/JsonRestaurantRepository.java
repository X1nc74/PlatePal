package platepal.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import platepal.model.Restaurant;
import platepal.persistence.JsonDataStore;
import platepal.persistence.PlatePalData;

public class JsonRestaurantRepository implements RestaurantRepository {

    private final JsonDataStore dataStore;

    public JsonRestaurantRepository() {
        this.dataStore = new JsonDataStore();
    }

    public JsonRestaurantRepository(JsonDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public List<Restaurant> findAll() {
        PlatePalData data = dataStore.load();
        return new ArrayList<>(data.getRestaurants());
    }

    @Override
    public Optional<Restaurant> findById(String id) {
        return findAll()
                .stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public void save(Restaurant restaurant) {
        PlatePalData data = dataStore.load();

        List<Restaurant> restaurants = data.getRestaurants();

        Optional<Restaurant> existing = restaurants.stream()
                .filter(r -> r.getId().equals(restaurant.getId()))
                .findFirst();

        if (existing.isPresent()) {
            int index = restaurants.indexOf(existing.get());
            restaurants.set(index, restaurant);
        } else {
            restaurants.add(restaurant);
        }

        dataStore.save(data);
    }

    @Override
    public void deleteById(String id) {
        PlatePalData data = dataStore.load();

        data.getRestaurants()
                .removeIf(r -> r.getId().equals(id));

        dataStore.save(data);
    }
}