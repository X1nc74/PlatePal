package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import platepal.controller.RestaurantController;
import platepal.model.PriceCategory;
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;
import platepal.service.RestaurantService;

public class RestaurantControllerTest {

    private RestaurantController controller;

    @BeforeEach
    void setUp() {
        FakeRestaurantRepository restaurantRepository =
                new FakeRestaurantRepository();

        restaurantRepository.save(
                new Restaurant(
                        "R001",
                        "Joe's Pizza",
                        "Pizza",
                        "New York",
                        PriceCategory.BUDGET));

        restaurantRepository.save(
                new Restaurant(
                        "R002",
                        "Sushi Place",
                        "Japanese",
                        "New York",
                        PriceCategory.EXPENSIVE));

        FakeRatingRepository ratingRepository =
                new FakeRatingRepository();

        ratingRepository.add(
                new Rating(
                        "RT001",
                        "U001",
                        "R001",
                        8));

        ratingRepository.add(
                new Rating(
                        "RT002",
                        "U001",
                        "R002",
                        10));

        RestaurantService restaurantService =
                new RestaurantService(
                        restaurantRepository);

        // Only read paths are exercised in these tests,
        // so no logged-in session is required.
        RatingService ratingService =
                new RatingService(
                        ratingRepository,
                        restaurantRepository,
                        null);

        PersonalRankingService personalRankingService =
                new PersonalRankingService(
                        ratingService,
                        restaurantRepository);

        controller =
                new RestaurantController(
                        restaurantService,
                        personalRankingService,
                        ratingService);
    }

    @Test
    void controllerSearchesRestaurants() {
        List<Restaurant> results =
                controller.searchByName("pizza");

        assertEquals(
                1,
                results.size());

        assertEquals(
                "Joe's Pizza",
                results.get(0).getName());
    }

    @Test
    void controllerReturnsPersonalRanking() {
        List<Restaurant> ranking =
                controller.getPersonalRanking("U001");

        assertEquals(
                2,
                ranking.size());

        assertEquals(
                "Sushi Place",
                ranking.get(0).getName());

        assertEquals(
                "Joe's Pizza",
                ranking.get(1).getName());
    }

    @Test
    void controllerReturnsRestaurantById() {
        Optional<Restaurant> restaurant =
                controller.getRestaurantById("R001");

        assertEquals(
                true,
                restaurant.isPresent());

        assertEquals(
                "Joe's Pizza",
                restaurant.get().getName());

        assertEquals(
                "Pizza",
                restaurant.get().getCuisine());

        assertEquals(
                "New York",
                restaurant.get().getLocation());

        assertEquals(
                PriceCategory.BUDGET,
                restaurant.get().getPriceCategory());
    }

    private static class FakeRestaurantRepository
            implements RestaurantRepository {

        private final List<Restaurant> restaurants =
                new ArrayList<>();

        @Override
        public List<Restaurant> findAll() {
            return new ArrayList<>(
                    restaurants);
        }

        @Override
        public Optional<Restaurant> findById(
                String id) {

            return restaurants.stream()
                    .filter(
                            restaurant ->
                                    restaurant
                                            .getId()
                                            .equals(id))
                    .findFirst();
        }

        @Override
        public void save(
                Restaurant restaurant) {

            restaurants.removeIf(
                    stored ->
                            stored
                                    .getId()
                                    .equals(
                                            restaurant
                                                    .getId()));

            restaurants.add(
                    restaurant);
        }

        @Override
        public void deleteById(
                String id) {

            restaurants.removeIf(
                    restaurant ->
                            restaurant
                                    .getId()
                                    .equals(id));
        }
    }

    private static class FakeRatingRepository
            implements RatingRepository {

        private final List<Rating> ratings =
                new ArrayList<>();

        public void add(
                Rating rating) {

            ratings.add(
                    rating);
        }

        @Override
        public List<Rating> findAll() {
            return new ArrayList<>(
                    ratings);
        }

        @Override
        public List<Rating> findByUserId(
                String userId) {

            return ratings.stream()
                    .filter(
                            rating ->
                                    rating.belongsTo(
                                            userId))
                    .toList();
        }

        @Override
        public List<Rating> findByRestaurantId(
                String restaurantId) {

            return ratings.stream()
                    .filter(
                            rating ->
                                    rating.isFor(
                                            restaurantId))
                    .toList();
        }

        @Override
        public Optional<Rating> findById(
                String id) {

            return ratings.stream()
                    .filter(
                            rating ->
                                    rating
                                            .getId()
                                            .equals(id))
                    .findFirst();
        }

        @Override
        public Optional<Rating> findByUserAndRestaurant(
                String userId,
                String restaurantId) {

            return ratings.stream()
                    .filter(
                            rating ->
                                    rating.belongsTo(
                                            userId)
                                            && rating.isFor(
                                                    restaurantId))
                    .findFirst();
        }

        @Override
        public void save(
                Rating rating) {

            ratings.removeIf(
                    stored ->
                            stored
                                    .getId()
                                    .equals(
                                            rating
                                                    .getId()));

            ratings.add(
                    rating);
        }

        @Override
        public void deleteById(
                String id) {

            ratings.removeIf(
                    rating ->
                            rating
                                    .getId()
                                    .equals(id));
        }

        @Override
        public void deleteByRestaurantId(
                String restaurantId) {

            ratings.removeIf(
                    rating ->
                            rating.isFor(
                                    restaurantId));
        }
    }
}