package platepal.ui;

import java.util.List;
import java.util.Scanner;

import platepal.controller.PersonalListController;
import platepal.model.Restaurant;

/**
 * The logged-in user's Visited and Want to Try lists.
 */
public class PersonalListMenu {

    private final PersonalListController controller;
    private final Scanner scanner;

    public PersonalListMenu(PersonalListController controller, Scanner scanner) {
        this.controller = controller;
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
                    printRestaurants("Visited", controller.getVisitedRestaurants());
                    break;

                case "2":
                    printRestaurants("Want to Try", controller.getWantToTryRestaurants());
                    break;

                case "3":
                    addToWantToTry();
                    break;

                case "4":
                    markAsVisited();
                    break;

                case "5":
                    removeFromVisited();
                    break;

                case "6":
                    removeFromWantToTry();
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
        System.out.println("=== My Lists ===");
        System.out.println("1. View Visited list");
        System.out.println("2. View Want to Try list");
        System.out.println("3. Add a restaurant to Want to Try");
        System.out.println("4. Mark a restaurant as Visited");
        System.out.println("5. Remove a restaurant from Visited");
        System.out.println("6. Remove a restaurant from Want to Try");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    }

    private void addToWantToTry() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        try {
            if (controller.addToWantToTry(id)) {
                System.out.println("Added " + id + " to your Want to Try list.");
            } else {
                System.out.println(
                        "That restaurant is already in your Visited "
                                + "or Want to Try list.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void markAsVisited() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        try {
            if (controller.markAsVisited(id)) {
                System.out.println(
                        "Marked " + id + " as Visited. It is no longer in "
                                + "your Want to Try list.");
            } else {
                System.out.println("That restaurant is already in your Visited list.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void removeFromVisited() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        if (controller.removeFromVisited(id)) {
            System.out.println("Removed " + id + " from your Visited list.");
        } else {
            System.out.println("That restaurant is not in your Visited list.");
        }
    }

    private void removeFromWantToTry() {
        String id = askForRestaurantId();

        if (id == null) {
            return;
        }

        if (controller.removeFromWantToTry(id)) {
            System.out.println("Removed " + id + " from your Want to Try list.");
        } else {
            System.out.println("That restaurant is not in your Want to Try list.");
        }
    }

    private void printRestaurants(String listName, List<Restaurant> restaurants) {
        System.out.println();

        if (restaurants.isEmpty()) {
            System.out.println("Your " + listName + " list is empty.");
            return;
        }

        System.out.println("--- " + listName + " ---");
        for (Restaurant restaurant : restaurants) {
            System.out.println(restaurant.getId() + "  " + restaurant);
        }
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
