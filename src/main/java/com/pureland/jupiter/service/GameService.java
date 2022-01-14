package com.pureland.jupiter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pureland.jupiter.entity.db.Item;
import com.pureland.jupiter.entity.db.ItemType;
import com.pureland.jupiter.entity.response.Game;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Service
public class GameService { // use to get info from twitch api
    private static final String TOKEN = "Bearer uhxoaepfcq1z19vfha6ar68r28exjw";
    private static final String CLIENT_ID = "nklol14oz698q694spt9yliuv4pdpz";
    private static final String TOP_GAME_URL = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;
    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    // Build the request URL which will be used when calling Twitch APIs,
    // e.g. https://api.twitch.tv/helix/games/top when trying to get top games.

    private String buildGameUrl(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            return String.format(url, limit);
        } else {
            try {
                // in case users send space, so we need to replace the space
                // Encode special characters in URL, e.g. riot game -> riot%20game
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
            return String.format(url, gameName);
        }
    }

    // Similar to buildGameURL, build Search URL that will be used when calling Twitch API.
    // e.g. https://api.twitch.tv/helix/clips?game_id=12924
    private String buildSearchUrl(String url, String gameId, int limit) {
        try {
            gameId = URLEncoder.encode(gameId, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return String.format(url, gameId, limit);
    }

    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // custom response handler for our response
        ResponseHandler<String> responseHandler = response -> {
            int responseCode = response.getStatusLine().getStatusCode();
            // check if request did not work
            if (responseCode != 200) {
                System.out.println("Response status: " + response.getStatusLine().getReasonPhrase());
                throw new TwitchException("Failed to get result from Twitch API");
            }

            HttpEntity entity = response.getEntity();
            // check if we get nothing
            if (entity == null) {
                throw new TwitchException("Failed to get result from Twitch API");
            }

            // convert result to a JSON obj
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            // then get the JSON array from data key and return as String
            return obj.getJSONArray("data").toString();
        };

        try {
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", TOKEN);
            request.addHeader("Client-Id", CLIENT_ID);

            return httpclient.execute(request, responseHandler);
        } catch (IOException ex) { // connection is aborted
            ex.printStackTrace();
            throw new TwitchException("Failed to get result from Twitch API");
        } finally {
            try {
                httpclient.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private List<Game> getGameList(String data) throws TwitchException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Arrays.asList(objectMapper.readValue(data, Game[].class));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            throw new TwitchException("Failed to parse game data frm Twitch API");
        }
    }



    public List<Game> topGame(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        String url = buildGameUrl(TOP_GAME_URL, "", limit);
        return getGameList(searchTwitch(url));
    }

    public Game searchGame(String gameName) throws TwitchException {
        String url = buildGameUrl(GAME_SEARCH_URL_TEMPLATE, gameName, 0);
        List<Game> gameList = getGameList(searchTwitch(url));
        if (gameList.size() != 0) {
            return gameList.get(0);
        }
        return null;
    }

    private List<Item> getItemList(String data) throws TwitchException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            throw new TwitchException("Failed to parse item data frm Twitch API");
        }
    }

    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        String url = buildSearchUrl(STREAM_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(url);
        List<Item> streams = getItemList(data);

        for (Item item : streams) {
            item.setType(ItemType.STREAM);
            item.setUrl(TWITCH_BASE_URL + item.getBroadcasterName());
        }

        return streams;
    }

    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        String url = buildSearchUrl(CLIP_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(url);
        List<Item> clips = getItemList(data);

        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }

        return clips;
    }

    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        String url = buildSearchUrl(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(url);
        List<Item> videos = getItemList(data);

        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }

        return videos;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> res = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            res.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return res;
    }

    private List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = new ArrayList<>();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;

            case VIDEO:
                items = searchVideos(gameId, limit);
                break;

            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for(Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }
}