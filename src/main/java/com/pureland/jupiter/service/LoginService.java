package com.pureland.jupiter.service;

import com.pureland.jupiter.dao.LoginDao;
import com.pureland.jupiter.util.Util;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LoginService {

    private final LoginDao loginDao;

    public LoginService(LoginDao loginDao) {
        this.loginDao = loginDao;
    }

    public String verifyLogin(String userId, String password) throws IOException {
        password = Util.encryptPassword(userId, password);
        return loginDao.verifyLogin(userId, password);
    }
}
