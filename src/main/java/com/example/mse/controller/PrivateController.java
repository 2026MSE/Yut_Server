package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.service.PrivateService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Scene;

@RestController
@RequestMapping("/private")
@CrossOrigin(origins = "*")

public class PrivateController {

    @Autowired
    private TurnService turnService;

    @Autowired
    private PrivateService privateService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/result")
    public Object getPrivateResult(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }

        return privateService.getResult(room);
    }

    @GetMapping("/exit")
    public String exitPrivate(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }

        turnService.moveCurrentTurnPlayerRoom(room, Scene.MAIN_HALL);

        return "Moved to MAIN_HALL";
    }

    @GetMapping("/info")
    public Object privateInfo(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }
        // 찬미 Map.of-> HashMap으로 변경
        java.util.Map<String, Object> result = new java.util.HashMap<>();

        result.put("sticks", room.getSticks());
        result.put("privateSticks", room.getPrivateSticks());
        result.put("publicSticks", room.getPublicSticks());
        result.put("result", room.getCurrentYutResult());

        return result;
    }
}