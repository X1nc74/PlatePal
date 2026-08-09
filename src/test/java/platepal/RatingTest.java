package platepal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import platepal.model.Rating;
import platepal.model.Review;

/**
 * Covers the rating and review items from the final testing checklist.
 */
class RatingTest {

    @Test
    @DisplayName("A score inside 1..10 is accepted")
    void acceptsValidScore() {
        Rating rating = new Rating("RT001", "U001", "R001", 7);
        assertEquals(7, rating.getScore());
    }

    @Test
    @DisplayName("Ratings below 1 are rejected")
    void rejectsScoreBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rating("RT001", "U001", "R001", 0));
    }

    @Test
    @DisplayName("Ratings above 10 are rejected")
    void rejectsScoreAboveMaximum() {
        assertThrows(IllegalArgumentException.class,
                () -> new Rating("RT001", "U001", "R001", 11));
    }

    @Test
    @DisplayName("An existing rating can be updated in place")
    void updatesExistingScore() {
        Rating rating = new Rating("RT001", "U001", "R001", 4);
        rating.updateScore(9);
        assertEquals(9, rating.getScore());
    }

    @Test
    @DisplayName("An invalid update leaves the old score untouched")
    void keepsOldScoreWhenUpdateIsInvalid() {
        Rating rating = new Rating("RT001", "U001", "R001", 4);
        assertThrows(IllegalArgumentException.class, () -> rating.updateScore(99));
        assertEquals(4, rating.getScore());
    }

    @Test
    @DisplayName("A rating knows which user it belongs to")
    void identifiesOwner() {
        Rating rating = new Rating("RT001", "U001", "R001", 6);
        assertTrue(rating.belongsTo("U001"));
        assertFalse(rating.belongsTo("U002"));
    }

    @Test
    @DisplayName("An empty review is rejected")
    void rejectsEmptyReview() {
        assertThrows(IllegalArgumentException.class,
                () -> new Review("RV001", "U001", "R001", "   "));
    }

    @Test
    @DisplayName("A review can be updated by its author")
    void updatesReviewContent() {
        Review review = new Review("RV001", "U001", "R001", "Good.");
        review.updateContent("Actually excellent.");
        assertEquals("Actually excellent.", review.getContent());
    }
}
