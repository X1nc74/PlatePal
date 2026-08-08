package platepal.ui;

import java.util.List;
import java.util.Scanner;

import platepal.controller.AdminController;
import platepal.exception.PermissionDeniedException;
import platepal.model.PriceCategory;
import platepal.model.Restaurant;

/**
 * Restaurant management screen for administrators.
 */
public class AdminMenu {

    private final AdminController controller;
    private final Scanner scanner;

    public AdminMenu(AdminController controller, Scanner scanner) {
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
                    addRestaurant();
                    break;

                case "2":
                    updateRestaurant();
                    break;

                case "3":
                    removeRestaurant();
                    break;

                case "4":
                    listRestaurants();
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
        System.out.println("=== Manage Restaurants ===");
        System.out.println("1. Add a restaurant");
        System.out.println("2. Update a restaurant");
        System.out.println("3. Remove a restaurant");
        System.out.println("4. List all restaurants");
        System.out.println("0. Back");
        System.out.print("Choose an option: ");
    }

    private void addRestaurant() {
        System.out.print("Name: ");
        String name = readLine();

        System.out.print("Cuisine: ");
        String cuisine = readLine();

        System.out.print("Location: ");
        String location = readLine();

        System.out.print("Price (BUDGET, MODERATE, EXPENSIVE, LUXURY): ");
        String price = readLine();

        System.out.print("Description (optional): ");
        String description = readLine();

        if (name == null || cuisine == null || location == null
                || price == null || description == null) {
            return;
        }

        try {
            Restaurant added = controller.addRestaurant(
                    name, cuisine, location,
                    PriceCategory.fromInput(price), description);

            System.out.println(
                    "Added " + added.getName() + " (" + added.getId() + ").");

        } catch (PermissionDeniedException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateRestaurant() {
        System.out.print("ID of the restaurant to update: ");
        String id = readLine();

        System.out.print("New name: ");
        String name = readLine();

        System.out.print("New cuisine: ");
        String cuisine = readLine();

        System.out.print("New location: ");
        String location = readLine();

        System.out.print("New price (BUDGET, MODERATE, EXPENSIVE, LUXURY): ");
        String price = readLine();

        System.out.print("New description (optional): ");
        String description = readLine();

        if (id == null || name == null || cuisine == null
                || location == null || price == null || description == null) {
            return;
        }

        try {
            Restaurant updated = controller.updateRestaurant(
                    id, name, cuisine, location,
                    PriceCategory.fromInput(price), description);

            System.out.println("Updated " + updated.getId() + ".");

        } catch (PermissionDeniedException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void removeRestaurant() {
        System.out.print("ID of the restaurant to remove: ");
        String id = readLine();

        if (id == null) {
            return;
        }

        try {
            controller.removeRestaurant(id);

            System.out.println(
                    "Removed " + id + ", along with its ratings and list entries.");

        } catch (PermissionDeniedException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listRestaurants() {
        List<Restaurant> restaurants = controller.getAllRestaurants();

        if (restaurants.isEmpty()) {
            System.out.println("There are no restaurants yet.");
            return;
        }

        System.out.println();
        for (Restaurant restaurant : restaurants) {
            System.out.println(restaurant.getId() + "  " + restaurant);
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
