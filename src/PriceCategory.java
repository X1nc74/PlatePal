package platepal.model;

/**
 * Price bracket of a restaurant, used by "search by price category"
 * and by the SortByPrice strategy.
 */
public enum PriceCategory {
    BUDGET("$"),
    MODERATE("$$"),
    EXPENSIVE("$$$"),
    LUXURY("$$$$");

    private final String symbol;

    PriceCategory(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Parses user input from the CLI. Accepts either the name ("BUDGET")
     * or the symbol ("$"), case-insensitively.
     *
     * @throws IllegalArgumentException if the input matches no category
     */
    public static PriceCategory fromInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Price category must not be empty.");
        }
        String cleaned = input.trim();
        for (PriceCategory category : values()) {
            if (category.name().equalsIgnoreCase(cleaned) || category.symbol.equals(cleaned)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown price category: " + input);
    }
}
