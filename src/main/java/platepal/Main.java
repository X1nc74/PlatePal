package platepal;

import java.util.Scanner;

import platepal.controller.RestaurantController;
import platepal.repository.JsonRatingRepository;
import platepal.repository.JsonRestaurantRepository;
import platepal.repository.RatingRepository;
import platepal.repository.RestaurantRepository;
import platepal.service.PersonalRankingService;
import platepal.service.RatingService;
import platepal.service.RestaurantService;
import platepal.ui.RestaurantMenu;

public class Main {

    public static void main(String[] args) {

        RestaurantRepository restaurantRepository =
                new JsonRestaurantRepository();

        RatingRepository ratingRepository =
                new JsonRatingRepository();

        RestaurantService restaurantService =
                new RestaurantService(restaurantRepository);

        RatingService ratingService =
                new RatingService(ratingRepository);

        PersonalRankingService personalRankingService =
                new PersonalRankingService(
                        ratingService,
                        restaurantRepository);

        RestaurantController restaurantController =
                new RestaurantController(
                        restaurantService,
                        personalRankingService);

        try (Scanner scanner = new Scanner(System.in)) {

            RestaurantMenu restaurantMenu =
                    new RestaurantMenu(
                            restaurantController,
                            ratingService,
                            scanner);

            System.out.println("Welcome to PlatePal!");

            restaurantMenu.show();

            System.out.println("Goodbye!");
        }
    }
}