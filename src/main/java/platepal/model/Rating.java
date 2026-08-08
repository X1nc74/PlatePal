package platepal.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single user's score for a single restaurant.
 *
 * <p>Business rules 1 and 2: the score is an integer from 1 to 10, and a user
 * has at most one active rating per restaurant. The uniqueness rule cannot be
 * enforced by one Rating object on its own, so RatingService looks for an
 * existing rating first and calls {@link #updateScore(int)} instead of creating
 * a second one.
 *
 * <p>The user and restaurant are referenced by ID rather than by object, so that
 * Gson never has to serialize a Rating inside a Restaurant inside a Rating.
 */
public class Rating {

    /** Lowest score a user may give. */
    public static final int MIN_SCORE = 1;

    /** Highest score a user may give. */
    public static final int MAX_SCORE = 10;

    private String id;
    private String userId;
    private String restaurantId;
    private int score;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Required by Gson. Do not use directly in application code. */
    protected Rating() {
    }

    public Rating(String id, String userId, String restaurantId, int score) {
        this.id = requireText(id, "Rating id");
        this.userId = requireText(userId, "User id");
        this.restaurantId = requireText(restaurantId, "Restaurant id");
        setScore(score);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public int getScore() {
        return score;
    }

    /**
     * Validates and stores the score. Private so that the only public way to
     * change an existing rating is {@link #updateScore(int)}, which also
     * refreshes the timestamp.
     *
     * @throws IllegalArgumentException if the score is outside 1..10
     */
    private void setScore(int score) {
        if (score < MIN_SCORE || score > MAX_SCORE) {
            throw new IllegalArgumentException(
                    "Rating must be between " + MIN_SCORE + " and " + MAX_SCORE + ".");
        }
        this.score = score;
    }

    /**
     * Business rule 3: rating the same restaurant again updates the existing
     * rating rather than creating a new one.
     *
     * <p>Business rule 4 (a user may only modify their own rating) is a
     * permission question and is checked in RatingService, which knows who is
     * currently logged in.
     */
    public void updateScore(int newScore) {
        setScore(newScore);
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt == null ? createdAt : updatedAt;
    }

    public boolean belongsTo(String candidateUserId) {
        return userId.equals(candidateUserId);
    }

    public boolean isFor(String candidateRestaurantId) {
        return restaurantId.equals(candidateRestaurantId);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be empty.");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Rating)) {
            return false;
        }
        return Objects.equals(id, ((Rating) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return score + "/10 by " + userId + " for " + restaurantId;
    }
}
