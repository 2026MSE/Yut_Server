package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.model.GameRoom;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

@RestController
@RequestMapping("/turn")
@CrossOrigin(origins = "*")

public class TurnController {

    @Autowired
    private TurnService turnService;

    @Autowired
    private RoomService roomService;

    @PostMapping("/next")
    public Object nextTurn(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }

        turnService.nextTurn(room);

        return room.getTurnInfo();
    }

}