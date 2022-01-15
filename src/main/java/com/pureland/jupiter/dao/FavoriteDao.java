package com.pureland.jupiter.dao;

import com.pureland.jupiter.entity.db.Item;
import com.pureland.jupiter.entity.db.ItemType;
import com.pureland.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FavoriteDao {

    private SessionFactory sessionFactory;

    @Autowired
    public FavoriteDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setFavoriteItem(String userId, Item item) {
        Session session = null; // provide CRUD ability

        try {
            session = sessionFactory.openSession();
            User user = session.get(User.class, userId);
            user.getItemSet().add(item);
            session.beginTransaction();
            session.save(user);
            session.getTransaction().commit();

        } catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();

        } finally {
            if (session != null) session.close();
        }

    }

    public void unsetFavoriteItem(String userId, String itemId) {
        Session session = null;

        try {
            session = sessionFactory.openSession();
            User user = session.get(User.class, userId);
            Item item = session.get(Item.class, itemId);
            user.getItemSet().remove(item);

            session.beginTransaction();
            session.update(user);
            session.getTransaction().commit();

        } catch (Exception ex) {
            ex.printStackTrace();
            session.getTransaction().rollback();

        } finally {
            if (session != null) session.close();
        }
    }

    public Set<Item> getFavoriteItems(String userId) {
        try (Session session = sessionFactory.openSession()) {

            return session.get(User.class, userId).getItemSet();

        } catch (Exception ex) {

            ex.printStackTrace();

        }

        return new HashSet<>();
    }

    public Set<String> getFavoriteItemIds(String userId) {

        Set<String> itemIds = new HashSet<>();
        try (Session session = sessionFactory.openSession()) {
            Set<Item> items = session.get(User.class, userId).getItemSet();

            for (Item item : items) {
                itemIds.add(item.getId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemIds;

    }

    // Get favorite items for the given user.
    // The returned map includes three entries like
    // {"Video": [gameId1, gameId2, gameId3], "Stream": [gameId4, gameId5, gameId6], "Clip": [gameId7, gameId8, ...]}
    public Map<String, List<String>> getFavoriteGameIds(Set<String> favoritedItemIds) {

        Map<String, List<String>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), new ArrayList<>());
        }

        try (Session session = sessionFactory.openSession()) {
            for (String itemId : favoritedItemIds) {
                Item item = session.get(Item.class, itemId);
                itemMap.get(item.getType().toString()).add(item.getGameId());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemMap;
    }

}
