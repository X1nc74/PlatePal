package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import platepal.model.PriceCategory;
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.repository.RatingRepository;
import platepal.service.RatingService;
import platepal.strategy.SortByName;
import platepal.strategy.SortByPrice;
import platepal.strategy.SortByRating;

public class RestaurantSortStrategyTest {

    private List<Restaurant> restaurants;

    @BeforeEach
    void setUp() {
        restaurants = List.of(
                new Restaurant(
                        "R001",
                        "Sushi Place",
                        "Japanese",
                        "New York",
                        PriceCategory.EXPENSIVE),

                new Restaurant(
                        "R002",
                        "Burger House",
                        "American",
                        "Boston",
                        PriceCategory.BUDGET),

                new Restaurant(
                        "R003",
                        "Pasta Corner",
                        "Italian",
                        "Chicago",
                        PriceCategory.MODERATE)
        );
    }

    @Test
    void sortByNameOrdersAlphabetically() {
        List<Restaurant> sorted =
                new SortByName().sort(restaurants);

        assertEquals("Burger House", sorted.get(0).getName());
        assertEquals("Pasta Corner", sorted.get(1).getName());
        assertEquals("Sushi Place", sorted.get(2).getName());
    }

    @Test
    void sortByPriceOrdersFromLowToHigh() {
        List<Restaurant> sorted =
                new SortByPrice().sort(restaurants);

        assertEquals(
                PriceCategory.BUDGET,
                sorted.get(0).getPriceCategory());

        assertEquals(
                PriceCategory.MODERATE,
                sorted.get(1).getPriceCategory());

        assertEquals(
                PriceCategory.EXPENSIVE,
                sorted.get(2).getPriceCategory());
    }

    @Test
    void sortByRatingOrdersHighestAverageFirst() {
        FakeRatingRepository fakeRatingRepository =
        new FakeRatingRepository();

        fakeRatingRepository.add(
                new Rating("RT001", "U001", "R001", 7));

        fakeRatingRepository.add(
                new Rating("RT002", "U002", "R001", 9));

        fakeRatingRepository.add(
                new Rating("RT003", "U001", "R002", 10));

        fakeRatingRepository.add(
                new Rating("RT004", "U001", "R003", 6));

        RatingService ratingService =
                new RatingService(fakeRatingRepository);

        List<Restaurant> sorted =
                new SortByRating(ratingService).sort(restaurants);

        assertEquals("Burger House", sorted.get(0).getName());
        assertEquals("Sushi Place", sorted.get(1).getName());
        assertEquals("Pasta Corner", sorted.get(2).getName());
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
    }
}