package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.repository.RestaurantRepository;
import platepal.service.RestaurantService;

public class RestaurantServiceTest {

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        RestaurantRepository fakeRepository = new FakeRestaurantRepository();

        fakeRepository.save(
                new Restaurant(
                        "R001",
                        "Joe's Pizza",
                        "Pizza",
                        "New York",
                        PriceCategory.BUDGET));

        fakeRepository.save(
                new Restaurant(
                        "R002",
                        "Sushi Nakazawa",
                        "Japanese",
                        "New York",
                        PriceCategory.EXPENSIVE));

        fakeRepository.save(
                new Restaurant(
                        "R003",
                        "Pasta House",
                        "Italian",
                        "Boston",
                        PriceCategory.MODERATE));

        restaurantService = new RestaurantService(fakeRepository);
    }

    @Test
    void searchByNameReturnsMatchingRestaurant() {
        List<Restaurant> results =
                restaurantService.searchByName("pizza");

        assertEquals(1, results.size());
        assertEquals("Joe's Pizza", results.get(0).getName());
    }

    @Test
    void searchByCuisineIgnoresCase() {
        List<Restaurant> results =
                restaurantService.searchByCuisine("japanese");

        assertEquals(1, results.size());
        assertEquals("Sushi Nakazawa", results.get(0).getName());
    }

    @Test
    void searchByLocationSupportsPartialMatch() {
        List<Restaurant> results =
                restaurantService.searchByLocation("New");

        assertEquals(2, results.size());
    }

    @Test
    void searchByPriceReturnsMatchingRestaurants() {
        List<Restaurant> results =
                restaurantService.searchByPrice(PriceCategory.MODERATE);

        assertEquals(1, results.size());
        assertEquals("Pasta House", results.get(0).getName());
    }

    @Test
    void searchWithNoMatchReturnsEmptyList() {
        List<Restaurant> results =
                restaurantService.searchByName("McDonald's");

        assertTrue(results.isEmpty());
    }

    private static class FakeRestaurantRepository
            implements RestaurantRepository {

        private final List<Restaurant> restaurants =
                new ArrayList<>();

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
            restaurants.removeIf(
                    r -> r.getId().equals(restaurant.getId()));
            restaurants.add(restaurant);
        }

        @Override
        public void deleteById(String id) {
            restaurants.removeIf(r -> r.getId().equals(id));
        }
    }
}