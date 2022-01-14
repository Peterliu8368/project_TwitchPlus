package com.pureland.jupiter.entity.db;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Items")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
// when we store obj from memory to hard drive, we need to serialize the data first
// then we convert data from hard drive to memory, we need to deserialize the data
public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("url")
    private String url;

    @JsonProperty("thumbnail_url")
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;


    @Column(name = "broadcaster_name")
    @JsonProperty("broadcaster_name")
    @JsonAlias({"user_name"})
    private String broadcasterName;

    @JsonProperty("game_id")
    @Column(name = "game_id")
    private String gameId;

    @Enumerated(value = EnumType.STRING)
    @JsonProperty("item_type")
    private ItemType type;


    @JsonIgnore
    @ManyToMany(mappedBy = "itemSet")
    private Set<User> users = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getBroadcasterName() {
        return broadcasterName;
    }

    public void setBroadcasterName(String broadcasterName) {
        this.broadcasterName = broadcasterName;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}
