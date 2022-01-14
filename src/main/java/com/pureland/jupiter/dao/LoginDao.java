package com.pureland.jupiter.dao;

import com.pureland.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository //also a component that will be created by Spring
public class LoginDao {

    private SessionFactory sessionFactory; // use to operate database

    @Autowired
    public LoginDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public String verifyLogin(String userId, String password) {
        String name = "";

        try (Session session = sessionFactory.openSession()) { // open session here so it will be auto-closed
            User user = session.get(User.class, userId); // basically SELECT from DB
            if (user != null && user.getPassword().equals(password)) {
                name = user.getFirstName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return name;
    }
}
