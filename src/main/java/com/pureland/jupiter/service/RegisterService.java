package com.pureland.jupiter.service;

import com.pureland.jupiter.dao.RegisterDao;
import com.pureland.jupiter.entity.db.User;
import com.pureland.jupiter.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RegisterService {

    private RegisterDao registerDao;

    @Autowired
    public RegisterService(RegisterDao registerDao) {
        this.registerDao = registerDao;
    }

    public boolean register(User user) throws IOException {
        user.setPassword(Util.encryptPassword(user.getUserId(), user.getPassword()));
        return registerDao.register(user);
    }
}
