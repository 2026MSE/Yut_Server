package com.example.mse;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/state")
public class StateController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TurnService turnService;

    @GetMapping("/playerInfo")
    public List<PlayerInfo> getPlayerInfo() {
        return playerService.getAllPlayerInfo();
    }

    @GetMapping("/turnInfo")
    public TurnInfo getTurnInfo() {
        return turnService.getTurnInfo();
    }
}