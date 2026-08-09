package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import platepal.model.PriceCategory;
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;

public class PersonalRankingServiceTest {

    private PersonalRankingService personalRankingService;

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
                        "Sushi Nakazawa",
                        "Japanese",
                        "New York",
                        PriceCategory.EXPENSIVE));

        restaurantRepository.save(
                new Restaurant(
                        "R003",
                        "Pasta House",
                        "Italian",
                        "Boston",
                        PriceCategory.MODERATE));

        FakeRatingRepository ratingRepository =
                new FakeRatingRepository();

        ratingRepository.add(
                new Rating("RT001", "U001", "R001", 8));

        ratingRepository.add(
                new Rating("RT002", "U001", "R002", 10));

        ratingRepository.add(
                new Rating("RT003", "U001", "R003", 6));

        // Ranking only reads ratings by user ID, so no session is involved and
        // the auth service is never touched.
        RatingService ratingService =
                new RatingService(ratingRepository, restaurantRepository, null);

        personalRankingService =
                new PersonalRankingService(
                        ratingService,
                        restaurantRepository);
    }

    @Test
    void personalRankingOrdersByUsersOwnScore() {
        List<Restaurant> ranking =
                personalRankingService.getPersonalRanking("U001");

        assertEquals(3, ranking.size());
        assertEquals("Sushi Nakazawa", ranking.get(0).getName());
        assertEquals("Joe's Pizza", ranking.get(1).getName());
        assertEquals("Pasta House", ranking.get(2).getName());
    }

    @Test
    void userWithNoRatingsGetsEmptyRanking() {
        List<Restaurant> ranking =
                personalRankingService.getPersonalRanking("U999");

        assertTrue(ranking.isEmpty());
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

    private static class FakeRatingRepository
            implements RatingRepository {

        private final List<Rating> ratings =
                new ArrayList<>();

        public void add(Rating rating) {
            ratings.add(rating);
        }

        @Override
        public List<Rating> findAll() {
            return new ArrayList<>(ratings);
        }

        @Override
        public List<Rating> findByUserId(String userId) {
            return ratings.stream()
                    .filter(rating -> rating.belongsTo(userId))
                    .toList();
        }

        @Override
        public List<Rating> findByRestaurantId(String restaurantId) {
            return ratings.stream()
                    .filter(rating -> rating.isFor(restaurantId))
                    .toList();
        }

        @Override
        public Optional<Rating> findById(String id) {
            return ratings.stream()
                    .filter(rating -> rating.getId().equals(id))
                    .findFirst();
        }

        @Override
        public Optional<Rating> findByUserAndRestaurant(
                String userId, String restaurantId) {

            return ratings.stream()
                    .filter(rating -> rating.belongsTo(userId)
                            && rating.isFor(restaurantId))
                    .findFirst();
        }

        @Override
        public void save(Rating rating) {
            ratings.removeIf(stored -> stored.getId().equals(rating.getId()));
            ratings.add(rating);
        }

        @Override
        public void deleteById(String id) {
            ratings.removeIf(rating -> rating.getId().equals(id));
        }

        @Override
        public void deleteByRestaurantId(String restaurantId) {
            ratings.removeIf(rating -> rating.isFor(restaurantId));
        }
    }
}