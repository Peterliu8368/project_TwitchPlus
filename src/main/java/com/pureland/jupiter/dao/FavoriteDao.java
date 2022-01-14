package com.pureland.jupiter.dao;

import com.pureland.jupiter.entity.db.Item;
import com.pureland.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

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

}
