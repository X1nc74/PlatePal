package platepal.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.repository.RestaurantRepository;

public class PersonalRankingService {

    private final RatingService ratingService;
    private final RestaurantRepository restaurantRepository;

    public PersonalRankingService(
            RatingService ratingService,
            RestaurantRepository restaurantRepository) {

        this.ratingService = ratingService;
        this.restaurantRepository = restaurantRepository;
    }

    public List<Restaurant> getPersonalRanking(String userId) {

        List<Rating> ratings =
                new ArrayList<>(ratingService.getRatingsByUser(userId));

        ratings.sort(
                Comparator.comparingInt(Rating::getScore)
                        .reversed());

        List<Restaurant> rankedRestaurants =
                new ArrayList<>();

        for (Rating rating : ratings) {

            Optional<Restaurant> restaurant =
                    restaurantRepository.findById(
                            rating.getRestaurantId());

            restaurant.ifPresent(rankedRestaurants::add);
        }

        return rankedRestaurants;
    }
}