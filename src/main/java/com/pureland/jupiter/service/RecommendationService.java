package com.pureland.jupiter.service;

import org.springframework.stereotype.Service;
import com.pureland.jupiter.dao.FavoriteDao;
import com.pureland.jupiter.entity.db.Item;
import com.pureland.jupiter.entity.db.ItemType;
import com.pureland.jupiter.entity.response.Game;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;


@Service
public class RecommendationService {

    private static final int DEFAULT_GAME_LIMIT = 3;
    private static final int DEFAULT_PER_GAME_RECOMMENDATION_LIMIT = 10;
    private static final int DEFAULT_TOTAL_RECOMMENDATION_LIMIT = 30;

    private GameService gameService;

    private FavoriteDao favoriteDao;

    @Autowired
    public RecommendationService(GameService gameService, FavoriteDao favoriteDao) {
        this.gameService = gameService;
        this.favoriteDao = favoriteDao;
    }

    public Map<String, List<Item>> recommendItemsByDefault() throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        List<Game> topGames;

        try {
            topGames = gameService.topGame(DEFAULT_GAME_LIMIT);
        } catch (TwitchException ex) {
            throw new RecommendationException("Failed to get game data for recommendation");
        }

        for (ItemType type : ItemType.values()) {
            recommendedItemMap.put(type.toString(), recommendByTopGames(type, topGames));
        }

        return recommendedItemMap;
    }

    // Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip].
    // Add items are related to the top games provided in the argument
    private List<Item> recommendByTopGames(ItemType type, List<Game> topGames) {
        List<Item> recommendedItems = new ArrayList<>();
        for (Game game : topGames) {
            List<Item> items;
            try {
                items = gameService.searchByType(game.getId(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
            } catch (TwitchException e) {
                throw new RecommendationException("Failed to get recommendation result");
            }
            for (Item item : items) {
                if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                    return recommendedItems;
                }
                recommendedItems.add(item);
            }
        }
        return recommendedItems;

    }

    public Map<String, List<Item>> recommendItemsByUser(String userId) throws RecommendationException {
        Map<String, List<Item>> recommendedItemMap = new HashMap<>();
        Set<String> favoritedItemIds;
        Map<String, List<String>> favoritedGameIds;

        favoritedItemIds = favoriteDao.getFavoriteItemIds(userId);
        favoritedGameIds = favoriteDao.getFavoriteGameIds(favoritedItemIds);

        for (Map.Entry<String, List<String>> entry : favoritedGameIds.entrySet()) {
            if (entry.getValue().size() == 0) {
                List<Game> topGames;
                try {
                    topGames = gameService.topGame((DEFAULT_GAME_LIMIT));
                } catch (TwitchException ex) {
                    throw new RecommendationException("Failed to get game data for recommendation");
                }

                recommendedItemMap.put(entry.getKey(), recommendByTopGames(ItemType.valueOf((entry.getKey())),topGames));
            } else {
                recommendedItemMap.put(entry.getKey(), recommendedByFavoriteHistory(favoritedItemIds, entry.getValue(), ItemType.valueOf((entry.getKey()))));
            }
        }

        return recommendedItemMap;
    }

    // Return a list of Item objects for the given type. Types are one of [Stream, Video, Clip].
    // All items are related to the items previously favorited by the user.
    // E.g., if a user favorited some videos about game "Just Chatting", then it will return some other videos about the same game.
    private List<Item> recommendedByFavoriteHistory(Set<String> favoritedItemIds, List<String> favoritedGameIds, ItemType type) throws RecommendationException {
            // Count the favorite game IDs from the database for the given user.
            // E.g. if the favorited game ID list is ["1234", "2345", "2345", "3456"], the returned Map is {"1234": 1, "2345": 2, "3456": 1}
            Map<String, Integer> favoriteGameIdByCount = new HashMap<>();
            for (String gameId : favoritedGameIds) {
                favoriteGameIdByCount.put(gameId, favoriteGameIdByCount.getOrDefault(gameId, 0) + 1);
            }

            // Sort the game Id by count. E.g. if the input is {"1234": 1, "2345": 2, "3456": 1}, the returned Map is {"2345": 2, "1234": 1, "3456": 1}
            List<Map.Entry<String, Integer>> sortedFavoriteGameIdListByCount = new ArrayList<>(
                    favoriteGameIdByCount.entrySet());
            sortedFavoriteGameIdListByCount.sort((Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) -> Integer
                    .compare(e2.getValue(), e1.getValue()));


            if (sortedFavoriteGameIdListByCount.size() > DEFAULT_GAME_LIMIT) {
                sortedFavoriteGameIdListByCount = sortedFavoriteGameIdListByCount.subList(0, DEFAULT_GAME_LIMIT);
            }


            List<Item> recommendedItems = new ArrayList<>();


            // Search Twitch based on the favorite game IDs returned in the last step.
            for (Map.Entry<String, Integer> favoriteGame : sortedFavoriteGameIdListByCount) {
                List<Item> items;
                try {
                    items = gameService.searchByType(favoriteGame.getKey(), type, DEFAULT_PER_GAME_RECOMMENDATION_LIMIT);
                } catch (TwitchException e) {
                    throw new RecommendationException("Failed to get recommendation result");
                }


                for (Item item : items) {
                    if (recommendedItems.size() == DEFAULT_TOTAL_RECOMMENDATION_LIMIT) {
                        return recommendedItems;
                    }
                    // dedup
                    if (!favoritedItemIds.contains(item.getId())) {
                        recommendedItems.add(item);
                    }
                }
            }
            return recommendedItems;



        }
}
