package platepal.strategy;

import java.util.Comparator;
import java.util.List;

import platepal.model.Restaurant;
import platepal.service.RatingService;

public class SortByRating implements RestaurantSortStrategy {

    private final RatingService ratingService;

    public SortByRating(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Override
    public List<Restaurant> sort(List<Restaurant> restaurants) {
        return restaurants.stream()
                .sorted(Comparator.comparingDouble(
                        (Restaurant restaurant) ->
                                ratingService.getAverageRating(
                                        restaurant.getId()))
                        .reversed())
                .toList();
    }
}