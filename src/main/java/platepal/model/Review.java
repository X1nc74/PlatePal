package platepal.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An optional written review. One user may write one review per restaurant
 * (business rule 2), and may update only their own (rules 3 and 4).
 *
 * <p>A review is a separate object rather than a field on {@link Rating}
 * because a review is optional and can be edited independently of the score.
 * Keeping them separate also matches the JSON design, which stores "ratings"
 * and "reviews" as two arrays.
 */
public class Review {

    /** Guards against a single review filling the whole CLI screen. */
    public static final int MAX_CONTENT_LENGTH = 1000;

    private String id;
    private String userId;
    private String restaurantId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Required by Gson. Do not use directly in application code. */
    protected Review() {
    }

    public Review(String id, String userId, String restaurantId, String content) {
        this.id = requireText(id, "Review id");
        this.userId = requireText(userId, "User id");
        this.restaurantId = requireText(restaurantId, "Restaurant id");
        setContentInternal(content);
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

    public String getContent() {
        return content;
    }

    /**
     * The review text itself is required once a user chooses to write one.
     * "Reviews are optional" means the user may skip creating a Review object,
     * not that an empty Review may exist.
     */
    private void setContentInternal(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Review content must not be empty.");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Review must be at most " + MAX_CONTENT_LENGTH + " characters.");
        }
        this.content = trimmed;
    }

    /** Business rule 3: a user may update their own review. */
    public void updateContent(String newContent) {
        setContentInternal(newContent);
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
        if (!(other instanceof Review)) {
            return false;
        }
        return Objects.equals(id, ((Review) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Review " + id + " by " + userId + " for " + restaurantId;
    }
}
