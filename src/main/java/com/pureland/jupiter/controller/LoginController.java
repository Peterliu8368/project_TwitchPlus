package com.pureland.jupiter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pureland.jupiter.entity.request.LoginRequestBody;
import com.pureland.jupiter.entity.response.LoginResponseBody;
import com.pureland.jupiter.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public void login(@RequestBody LoginRequestBody requestBody, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String firstname = loginService.verifyLogin(requestBody.getUserId(), requestBody.getPassword());

        if (!firstname.isEmpty()) {
            HttpSession session = request.getSession(); // get the current session or create one if not exist
            session.setAttribute("user_id", requestBody.getUserId());
            session.setMaxInactiveInterval(600);

            LoginResponseBody LoginResponseBody = new LoginResponseBody(requestBody.getUserId(), firstname);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(new ObjectMapper().writeValueAsString(LoginResponseBody));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
}
