package com.example.mse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/turn")
public class TurnController {

    @Autowired
    private TurnService turnService;

    @GetMapping("/start") //Unity로 연결할때는 POST로 바꿔야할듯
    public String startTurn(
        @RequestParam String playerId
    ){
        turnService.setCurrentTurnPlayer(playerId);
        return "Turn started for " + playerId;
    }

}