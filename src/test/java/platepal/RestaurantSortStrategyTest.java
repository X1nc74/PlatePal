package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.strategy.SortByName;
import platepal.strategy.SortByPrice;

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

        assertEquals(PriceCategory.BUDGET,
                sorted.get(0).getPriceCategory());

        assertEquals(PriceCategory.MODERATE,
                sorted.get(1).getPriceCategory());

        assertEquals(PriceCategory.EXPENSIVE,
                sorted.get(2).getPriceCategory());
    }
}