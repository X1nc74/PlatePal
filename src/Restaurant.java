package platepal.model;

import java.util.Objects;

/**
 * A restaurant that users can search for, list and rate.
 *
 * <p>Note that there is deliberately no {@code averageRating} field. Business
 * rule 5 says the average is calculated from the current Rating objects, and
 * rule 6 says users must not modify it directly. Storing it here would mean two
 * sources of truth that can drift apart; RatingService computes it on demand
 * instead.
 *
 * <p>All setters are guarded so that an administrator cannot save a restaurant
 * with an empty name or cuisine.
 */
public class Restaurant {

    private String id;
    private String name;
    private String cuisine;
    private String location;
    private PriceCategory priceCategory;
    private String description;

    /** Required by Gson. Do not use directly in application code. */
    protected Restaurant() {
    }

    public Restaurant(String id,
                      String name,
                      String cuisine,
                      String location,
                      PriceCategory priceCategory) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Restaurant id must not be empty.");
        }
        this.id = id.trim();
        setName(name);
        setCuisine(cuisine);
        setLocation(location);
        setPriceCategory(priceCategory);
        this.description = "";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "Restaurant name");
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = requireText(cuisine, "Cuisine");
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = requireText(location, "Location");
    }

    public PriceCategory getPriceCategory() {
        return priceCategory;
    }

    public void setPriceCategory(PriceCategory priceCategory) {
        this.priceCategory = Objects.requireNonNull(
                priceCategory, "Price category must not be null.");
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    /** Optional free text, so an empty value is allowed here. */
    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    /**
     * Case-insensitive match used by the search feature. Kept in the model so
     * that RestaurantService does not need to know how a field is stored.
     */
    public boolean matchesName(String query) {
        return query != null
                && name.toLowerCase().contains(query.trim().toLowerCase());
    }

    public boolean matchesCuisine(String query) {
        return query != null && cuisine.equalsIgnoreCase(query.trim());
    }

    public boolean matchesLocation(String query) {
        return query != null
                && location.toLowerCase().contains(query.trim().toLowerCase());
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
        if (!(other instanceof Restaurant)) {
            return false;
        }
        return Objects.equals(id, ((Restaurant) other).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " [" + cuisine + ", " + location + ", "
                + priceCategory.getSymbol() + "]";
    }
}
