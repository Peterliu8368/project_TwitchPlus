package com.pureland.jupiter.controller;

import com.pureland.jupiter.entity.db.Item;
import com.pureland.jupiter.entity.request.FavoriteRequestBody;
import com.pureland.jupiter.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FavoriteController {

    private FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @RequestMapping(value = "/favorite", method = RequestMethod.POST)
    public void setFavoriteItem(@RequestBody FavoriteRequestBody favoriteRequestBody, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String userId = (String) session.getAttribute("user_id");
        favoriteService.setFavoriteItem(userId, favoriteRequestBody.getFavoriteItem());

    }

    @RequestMapping(value = "/favorite", method = RequestMethod.DELETE)
    public void unsetFavoriteItem(@RequestBody FavoriteRequestBody favoriteRequestBody, HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String userId = (String) session.getAttribute("user_id");
        favoriteService.unsetFavoriteItem(userId, favoriteRequestBody.getFavoriteItem().getId());

    }

    @RequestMapping(value = "/favorite", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<Item>> getFavoriteItem(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return new HashMap<>();
        }

        String userId = (String) session.getAttribute("user_id");
        return favoriteService.getFavoriteItem(userId);
    }
}
