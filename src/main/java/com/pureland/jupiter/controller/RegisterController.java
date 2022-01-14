package com.pureland.jupiter.controller;

import com.pureland.jupiter.entity.db.User;
import com.pureland.jupiter.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class RegisterController {

    private final RegisterService registerService;

    @Autowired
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }


    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@RequestBody User user, HttpServletResponse response) throws IOException {
        if (!registerService.register(user)) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }
}
