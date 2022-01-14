package com.pureland.jupiter.dao;

import com.pureland.jupiter.entity.db.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.PersistenceException;

@Repository
public class RegisterDao {

    private SessionFactory sessionFactory;

    @Autowired
    public RegisterDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public boolean register(User user) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction(); // used to ensure consistency
            session.save(user);
            session.getTransaction().commit();
        } catch (PersistenceException | IllegalStateException ex) {
            // if hibernate throws this exception, it means the user already be register
            ex.printStackTrace();
            session.getTransaction().rollback();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return true;
    }
}
