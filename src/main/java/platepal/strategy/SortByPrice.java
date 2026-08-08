package platepal.strategy;

import java.util.Comparator;
import java.util.List;

import platepal.model.Restaurant;

public class SortByPrice implements RestaurantSortStrategy {

    @Override
    public List<Restaurant> sort(List<Restaurant> restaurants) {
        return restaurants.stream()
                .sorted(Comparator.comparing(Restaurant::getPriceCategory))
                .toList();
    }
}