package platepal.strategy;

import java.util.List;
import platepal.model.Restaurant;

public interface RestaurantSortStrategy {

    List<Restaurant> sort(List<Restaurant> restaurants);
}