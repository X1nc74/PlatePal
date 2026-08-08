package platepal.service;

import java.util.List;

import platepal.model.Rating;
import platepal.repository.RatingRepository;

public class RatingService {

    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public List<Rating> getRatingsByUser(String userId) {
        return ratingRepository.findByUserId(userId);
    }

    public double getAverageRating(String restaurantId) {
        List<Rating> ratings =
                ratingRepository.findByRestaurantId(restaurantId);

        if (ratings.isEmpty()) {
            return 0.0;
        }

        return ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);
    }
}