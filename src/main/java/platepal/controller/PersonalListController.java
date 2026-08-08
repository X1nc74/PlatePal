package platepal.controller;

import java.util.List;

import platepal.model.Restaurant;
import platepal.service.PersonalListService;

/**
 * Routes personal list actions from the UI to the service.
 */
public class PersonalListController {

    private final PersonalListService personalListService;

    public PersonalListController(PersonalListService personalListService) {
        this.personalListService = personalListService;
    }

    public boolean markAsVisited(String restaurantId) {
        return personalListService.markAsVisited(restaurantId);
    }

    public boolean addToWantToTry(String restaurantId) {
        return personalListService.addToWantToTry(restaurantId);
    }

    public boolean removeFromVisited(String restaurantId) {
        return personalListService.removeFromVisited(restaurantId);
    }

    public boolean removeFromWantToTry(String restaurantId) {
        return personalListService.removeFromWantToTry(restaurantId);
    }

    public List<Restaurant> getVisitedRestaurants() {
        return personalListService.getVisitedRestaurants();
    }

    public List<Restaurant> getWantToTryRestaurants() {
        return personalListService.getWantToTryRestaurants();
    }
}
