package platepal.controller;

import java.util.List;
import java.util.Optional;

import platepal.model.Rating;
import platepal.model.Restaurant;
import platepal.model.User;
import platepal.service.SocialService;

/**
 * Routes social actions from the UI to the service.
 */
public class SocialController {

    private final SocialService socialService;

    public SocialController(SocialService socialService) {
        this.socialService = socialService;
    }

    public boolean follow(String username) {
        return socialService.follow(username);
    }

    public boolean unfollow(String username) {
        return socialService.unfollow(username);
    }

    public boolean isFollowing(String username) {
        return socialService.isFollowing(username);
    }

    public List<User> getFollowing() {
        return socialService.getFollowing();
    }

    public List<User> getFollowers() {
        return socialService.getFollowers();
    }

    public List<User> getOtherUsers() {
        return socialService.getOtherUsers();
    }

    public Optional<User> findByUsername(String username) {
        return socialService.findByUsername(username);
    }

    public List<Restaurant> getVisitedRestaurants(String username) {
        return socialService.getVisitedRestaurants(username);
    }

    public List<Restaurant> getWantToTryRestaurants(String username) {
        return socialService.getWantToTryRestaurants(username);
    }

    public List<Restaurant> getHighestRatedRestaurants(String username) {
        return socialService.getHighestRatedRestaurants(username);
    }

    public List<Rating> getRecentActivityFromFollowedUsers() {
        return socialService.getRecentActivityFromFollowedUsers();
    }

    public String getUsername(String userId) {
        return socialService.getUsername(userId);
    }
}
