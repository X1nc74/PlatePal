package platepal.ui;

import java.util.List;
import java.util.Scanner;

import platepal.controller.RestaurantController;
import platepal.controller.SocialController;
import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.model.User;

/**
 * Following other users, viewing their lists, and seeing what the people you
 * follow have been rating.
 */
public class SocialMenu {

    private final SocialController controller;
    private final RestaurantController restaurantController;
    private final Scanner scanner;

    public SocialMenu(SocialController controller,
                      RestaurantController restaurantController,
                      Scanner scanner) {
        this.controller = controller;
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
                    browseUsers();
                    break;

                case "2":
                    follow();
                    break;

                case "3":
                    unfollow();
                    break;

                case "4":
                    printUsers("Following", controller.getFollowing());
                    break;

                case "5":
                    printUsers("Followers", controller.getFollowers());
                    break;

                case "6":
                    viewProfile();
                    break;

                case "7":
                    viewFriendActivity();
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
        System.out.println("=== Users ===");
        System.out.println("1. Browse all users");
        System.out.println("2. Follow a user");
        System.out.println("3. Unfollow a user");
        System.out.println("4. View who I follow");
        System.out.println("5. View my followers");
        System.out.println("6. View a user's profile and lists");
        System.out.println("7. View friend activity (recent ratings)");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    }

    private void browseUsers() {
        List<User> users = controller.getOtherUsers();

        if (users.isEmpty()) {
            System.out.println("There are no other users yet.");
            return;
        }

        System.out.println();
        for (User user : users) {
            String marker = controller.isFollowing(user.getUsername())
                    ? "  (following)"
                    : "";

            System.out.println(user.getUsername() + marker);
        }
    }

    private void follow() {
        System.out.print("Username to follow: ");
        String username = readLine();

        if (username == null) {
            return;
        }

        try {
            if (controller.follow(username)) {
                System.out.println("You are now following " + username + ".");
            } else {
                System.out.println("You already follow " + username + ".");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void unfollow() {
        System.out.print("Username to unfollow: ");
        String username = readLine();

        if (username == null) {
            return;
        }

        try {
            if (controller.unfollow(username)) {
                System.out.println("You no longer follow " + username + ".");
            } else {
                System.out.println("You were not following " + username + ".");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void viewProfile() {
        System.out.print("Username: ");
        String username = readLine();

        if (username == null) {
            return;
        }

        controller.findByUsername(username).ifPresentOrElse(
                user -> {
                    System.out.println();
                    System.out.println("--- " + user.getUsername() + " ---");
                    System.out.println("ID: " + user.getId());
                    System.out.println("Role: " + user.getRole());
                    System.out.println("Following: "
                            + user.getFollowingUserIds().size() + " user(s)");
                    System.out.println("You follow them: "
                            + (controller.isFollowing(user.getUsername()) ? "yes" : "no"));

                    printRestaurants("Visited",
                            controller.getVisitedRestaurants(username));
                    printRestaurants("Want to Try",
                            controller.getWantToTryRestaurants(username));
                    printRestaurants("Highest Rated",
                            controller.getHighestRatedRestaurants(username));
                },
                () -> System.out.println(
                        "No user found with username " + username + "."));
    }

    /**
     * Recent ratings from every user the current user follows, newest first.
     * Each rating stores only IDs, so the restaurant name is resolved through
     * {@link RestaurantController} the same way {@code RatingMenu} does.
     */
    private void viewFriendActivity() {
        List<Rating> activity = controller.getRecentActivityFromFollowedUsers();

        if (activity.isEmpty()) {
            System.out.println("No recent activity from the people you follow.");
            return;
        }

        System.out.println();
        System.out.println("--- Friend Activity ---");

        for (Rating rating : activity) {
            System.out.println(
                    controller.getUsername(rating.getUserId())
                            + " rated " + describeRestaurant(rating.getRestaurantId())
                            + "  " + rating.getScore() + "/10"
                            + "  (" + rating.getUpdatedAt().toLocalDate() + ")");
        }
    }

    /**
     * A rating stores only a restaurant ID. A restaurant that no longer exists
     * should still print something readable rather than a blank line.
     */
    private String describeRestaurant(String restaurantId) {
        return restaurantController.getRestaurantById(restaurantId)
                .map(Restaurant::getName)
                .orElse(restaurantId);
    }

    private void printUsers(String heading, List<User> users) {
        System.out.println();

        if (users.isEmpty()) {
            System.out.println("No users in " + heading + ".");
            return;
        }

        System.out.println("--- " + heading + " ---");
        for (User user : users) {
            System.out.println(user.getUsername());
        }
    }

    private void printRestaurants(String listName, List<Restaurant> restaurants) {
        System.out.println();

        if (restaurants.isEmpty()) {
            System.out.println(listName + ": empty");
            return;
        }

        System.out.println(listName + ":");
        for (Restaurant restaurant : restaurants) {
            System.out.println("  " + restaurant.getId() + "  " + restaurant);
        }
    }

    /** @return the next line, or null when the input has run out */
    private String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine().trim();
    }
}
