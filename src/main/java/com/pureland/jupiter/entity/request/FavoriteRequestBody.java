package com.pureland.jupiter.entity.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pureland.jupiter.entity.db.Item;

public class FavoriteRequestBody {

    private final Item favoriteItem;

    @JsonCreator
    public FavoriteRequestBody(@JsonProperty("favorite") Item favoriteItem) { // this favorite is the key of JSON data we get
        this.favoriteItem = favoriteItem;
    }

    public Item getFavoriteItem() {
        return favoriteItem;
    }

}
