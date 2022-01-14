package com.pureland.jupiter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pureland.jupiter.entity.response.Game;
import com.pureland.jupiter.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// indicate this class is a controller
// so when we run the project, the DispatcherServlet will register
// this class as controller and save it to the mapping
@Controller
public class GameController {

    private GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    // the annotation indicate the URL as well as the HTTP method
    @RequestMapping(value = "/game", method = RequestMethod.GET)
    public void getGame(@RequestParam(value = "game_name", required = false) String gameName, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        if (gameName != null) {
            Game game = gameService.searchGame(gameName);
            response.getWriter().print(new ObjectMapper().writeValueAsString(game));
        } else {
            List<Game> gameList = gameService.topGame(3);
            response.getWriter().print(new ObjectMapper().writeValueAsString(gameList));
        }
    }
}
