package platepal.ui;

import java.util.List;
import java.util.Scanner;

import platepal.controller.RestaurantController;
import platepal.model.PriceCategory;
import platepal.model.Restaurant;
import platepal.service.RatingService;
import platepal.strategy.SortByName;
import platepal.strategy.SortByPrice;
import platepal.strategy.SortByRating;

public class RestaurantMenu {

    private final RestaurantController controller;
    private final RatingService ratingService;
    private final Scanner scanner;

    public RestaurantMenu(
            RestaurantController controller,
            RatingService ratingService,
            Scanner scanner) {

        this.controller = controller;
        this.ratingService = ratingService;
        this.scanner = scanner;
    }

    public void show() {
        boolean running = true;

        while (running) {
            printMenu();

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    searchByName();
                    break;

                case "2":
                    searchByCuisine();
                    break;

                case "3":
                    searchByLocation();
                    break;

                case "4":
                    searchByPrice();
                    break;

                case "5":
                    sortByName();
                    break;

                case "6":
                    sortByPrice();
                    break;

                case "7":
                    sortByRating();
                    break;

                case "8":
                    viewPersonalRanking();
                    break;

                case "9":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== Restaurant Discovery ===");
        System.out.println("1. Search by name");
        System.out.println("2. Search by cuisine");
        System.out.println("3. Search by location");
        System.out.println("4. Search by price");
        System.out.println("5. Sort all restaurants by name");
        System.out.println("6. Sort all restaurants by price");
        System.out.println("7. Sort all restaurants by average rating");
        System.out.println("8. View personal ranking");
        System.out.println("9. Back");
        System.out.print("Choose an option: ");
    }

    private void searchByName() {
        System.out.print("Enter restaurant name: ");
        String query = scanner.nextLine();

        printRestaurants(
                controller.searchByName(query));
    }

    private void searchByCuisine() {
        System.out.print("Enter cuisine: ");
        String cuisine = scanner.nextLine();

        printRestaurants(
                controller.searchByCuisine(cuisine));
    }

    private void searchByLocation() {
        System.out.print("Enter location: ");
        String location = scanner.nextLine();

        printRestaurants(
                controller.searchByLocation(location));
    }

    private void searchByPrice() {
        System.out.print(
                "Enter price (BUDGET, MODERATE, EXPENSIVE, LUXURY): ");

        String input = scanner.nextLine();

        try {
            PriceCategory price =
                    PriceCategory.fromInput(input);

            printRestaurants(
                    controller.searchByPrice(price));

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sortByName() {
        List<Restaurant> restaurants =
                controller.getAllRestaurants();

        printRestaurants(
                controller.sortRestaurants(
                        restaurants,
                        new SortByName()));
    }

    private void sortByPrice() {
        List<Restaurant> restaurants =
                controller.getAllRestaurants();

        printRestaurants(
                controller.sortRestaurants(
                        restaurants,
                        new SortByPrice()));
    }

    private void sortByRating() {
        List<Restaurant> restaurants =
                controller.getAllRestaurants();

        printRestaurants(
                controller.sortRestaurants(
                        restaurants,
                        new SortByRating(ratingService)));
    }

    private void viewPersonalRanking() {
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();

        printRestaurants(
                controller.getPersonalRanking(userId));
    }

    private void printRestaurants(
        List<Restaurant> restaurants) {

    if (restaurants.isEmpty()) {
        System.out.println("No restaurants found.");
        pause();
        return;
    }

    System.out.println();

    for (int i = 0; i < restaurants.size(); i++) {
        System.out.println(
                (i + 1) + ". " + restaurants.get(i));
    }

    pause();
}

    private void pause() {
        System.out.println();
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}