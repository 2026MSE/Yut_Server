package com.example.mse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/private")
public class PrivateController {

    @Autowired
    private TurnService turnService;

    @Autowired
    private PrivateService privateService;

    @GetMapping("/result")
    public Object getPrivateResult(
        @RequestParam String playerId
    ){

        if(!turnService.isTurnPlayer(playerId)){
            return "Not your turn.";
        }

        return privateService.getResult();

    }

}