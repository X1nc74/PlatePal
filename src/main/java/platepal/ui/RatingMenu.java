package platepal.ui;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import platepal.controller.RatingController;
import platepal.controller.RestaurantController;
import platepal.controller.ReviewController;
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.model.Review;

/**
 * Leaving and managing the logged-in user's own ratings and reviews.
 *
 * <p>Ratings and reviews are edited on the same screen because a user usually
 * wants to do both at once, but each is stored on its own: rating a restaurant
 * never requires writing about it, and a review can be deleted while the score
 * stays.
 */
public class RatingMenu {

    private final RatingController ratingController;
    private final ReviewController reviewController;
    private final RestaurantController restaurantController;
    private final Scanner scanner;

    public RatingMenu(RatingController ratingController,
                      ReviewController reviewController,
                      RestaurantController restaurantController,
                      Scanner scanner) {

        this.ratingController = ratingController;
        this.reviewController = reviewController;
        this.restaurantController = restaurantController;
        this.scanner = scanner;
    }

    public void show() {
        boolean running = true;

        while (running) {
            printMenu();

            String choice = readLine();

            if (choice == null) {
                return;
            }

            switch (choice) {
                case "1":
                    rateRestaurant();
                    break;

                case "2":
                    writeReview();
                    break;

                case "3":
                    viewMyRatings();
                    break;

                case "4":
                    viewMyReviews();
                    break;

                case "5":
                    viewRestaurantFeedback();
                    break;

                case "6":
                    deleteMyRating();
                    break;

                case "7":
                    deleteMyReview();
                    break;

                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Ratings & Reviews ===");
        System.out.println("1. Rate a restaurant");
        System.out.println("2. Write or update a review");
        System.out.println("3. View my ratings");
        System.out.println("4. View my reviews");
        System.out.println("5. View ratings and reviews for a restaurant");
        System.out.println("6. Delete my rating");
        System.out.println("7. Delete my review");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    }

    private void rateRestaurant() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        // Asked before saving, so the confirmation can say whether this replaced
        // an earlier score.
        boolean hadRating = hasExistingRating(id);

        System.out.print("Score (" + Rating.MIN_SCORE + "-" + Rating.MAX_SCORE + "): ");
        String input = readLine();

        if (input == null) {
            return;
        }

        int score;

        try {
            score = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("A rating must be a whole number.");
            return;
        }

        try {
            Rating rating = ratingController.rateRestaurant(id, score);

            System.out.println(hadRating
                    ? "Updated your rating to " + rating.getScore() + "/10."
                    : "Saved your rating of " + rating.getScore() + "/10.");

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void writeReview() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        boolean hadReview = hasExistingReview(id);

        if (hadReview) {
            System.out.println("You have already reviewed this restaurant. "
                    + "Writing again replaces it.");
        }

        System.out.print("Your review: ");
        String content = readLine();

        if (content == null) {
            return;
        }

        try {
            reviewController.writeReview(id, content);

            System.out.println(hadReview
                    ? "Updated your review."
                    : "Saved your review.");

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewMyRatings() {
        List<Rating> ratings = ratingController.getMyRatings();

        if (ratings.isEmpty()) {
            System.out.println("You have not rated anything yet.");
            return;
        }

        System.out.println();
        System.out.println("--- My Ratings ---");

        for (Rating rating : ratings) {
            System.out.println(
                    rating.getScore() + "/10  "
                            + describeRestaurant(rating.getRestaurantId()));
        }
    }

    private void viewMyReviews() {
        List<Review> reviews = reviewController.getMyReviews();

        if (reviews.isEmpty()) {
            System.out.println("You have not written any reviews yet.");
            return;
        }

        System.out.println();
        System.out.println("--- My Reviews ---");

        for (Review review : reviews) {
            System.out.println(describeRestaurant(review.getRestaurantId()));
            System.out.println("  " + review.getContent());
        }
    }

    /** Everyone's ratings and reviews for one restaurant, not just the user's. */
    private void viewRestaurantFeedback() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        Optional<Restaurant> restaurant = restaurantController.getRestaurantById(id);

        if (restaurant.isEmpty()) {
            System.out.println("No restaurant found with id " + id + ".");
            return;
        }

        System.out.println();
        System.out.println("--- " + restaurant.get().getName() + " ---");

        printRatingSummary(id);
        printReviews(id);
    }

    private void deleteMyRating() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        if (ratingController.removeMyRating(id)) {
            System.out.println("Removed your rating for " + id + ".");
        } else {
            System.out.println("You have not rated " + id + ".");
        }
    }

    private void deleteMyReview() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        if (reviewController.removeMyReview(id)) {
            System.out.println("Removed your review of " + id + ".");
        } else {
            System.out.println("You have not reviewed " + id + ".");
        }
    }

    /**
     * Prints the average score and how many people it is based on. Shared with
     * {@link RestaurantMenu} in spirit but not in code, because the two screens
     * format the surrounding output differently.
     */
    private void printRatingSummary(String restaurantId) {
        int count = ratingController.getRatingCount(restaurantId);

        if (count == 0) {
            System.out.println("No ratings yet.");
            return;
        }

        System.out.printf(
                "Average rating: %.1f/10 from %d %s%n",
                ratingController.getAverageRating(restaurantId),
                count,
                count == 1 ? "rating" : "ratings");
    }

    private void printReviews(String restaurantId) {
        List<Review> reviews = reviewController.getReviewsForRestaurant(restaurantId);

        if (reviews.isEmpty()) {
            System.out.println("No reviews yet.");
            return;
        }

        System.out.println();
        System.out.println("Reviews:");

        for (Review review : reviews) {
            System.out.println("  " + reviewController.getAuthorName(review)
                    + " (" + review.getUpdatedAt().toLocalDate() + "):");
            System.out.println("    " + review.getContent());
        }
    }

    /**
     * A rating stores only a restaurant ID. A restaurant that no longer exists
     * should still print something readable rather than a blank line.
     */
    private String describeRestaurant(String restaurantId) {
        return restaurantController.getRestaurantById(restaurantId)
                .map(restaurant -> restaurant.getName() + "  (" + restaurantId + ")")
                .orElse(restaurantId);
    }

    /** @return false if nobody is logged in or the restaurant is unknown */
    private boolean hasExistingRating(String restaurantId) {
        return ratingController.getMyRating(restaurantId).isPresent();
    }

    private boolean hasExistingReview(String restaurantId) {
        return reviewController.getMyReview(restaurantId).isPresent();
    }

    private String askForRestaurantId() {
        System.out.print("Restaurant ID: ");
        return readLine();
    }

    /** @return the next line, or null when the input has run out */
    private String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine().trim();
    }
}
