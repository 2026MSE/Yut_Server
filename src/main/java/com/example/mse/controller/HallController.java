package com.example.mse.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.model.GameRoom;
import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;
import com.example.mse.service.HallService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

@RestController
@RequestMapping("/hall")
@CrossOrigin(origins = "*")
public class HallController {

    @Autowired
    private HallService hallService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/state")
    public Object getState(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);
        return room.getHallState();
    }

    @GetMapping("/info")
    public Object info(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);

        Map<String, Object> result = new java.util.HashMap<>();

        result.put("state", room.getHallState());
        result.put("publicSticks", room.getPublicSticks());
        result.put("declaredPrivateSticks", room.getDeclaredPrivateSticks());
        result.put("firstChallenger", room.getFirstChallengerId());
        result.put("queue", room.getChallengeQueue());

        return result;
    }

    @GetMapping("/declare")
    public Object declare(
            @RequestParam String roomId,
            @RequestParam String playerId,
            @RequestParam StickSide s1,
            @RequestParam StickSide s2) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }

        if (s1 == StickSide.TAIL) {
            return "Invalid declaration: first private stick cannot be TAIL.";
        }

        if (s2 == StickSide.BACK) {
            return "Invalid declaration: second private stick cannot be BACK.";
        }

        hallService.declarePrivateSticks(room, s1, s2);

        return Map.of(
                "message", "Declared private sticks",
                "declaredPrivateSticks", room.getDeclaredPrivateSticks(),
                "publicSticks", room.getPublicSticks(),
                "state", room.getHallState());
    }

    @GetMapping("/challenge")
    public Object challenge(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (turnService.isTurnPlayer(room, playerId)) {
            return "Turn player cannot challenge";
        }

        return hallService.challenge(room, playerId);
    }

    @GetMapping("/judge")
    public Object judge(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (room.getCurrentYutResult() == null) {
            return "No yut result yet.";
        }

        String judgeResult = hallService.judgeChallenge(room);

        turnService.moveCurrentTurnPlayerRoom(room, Scene.YUT_ROOM);

        return Map.of(
                "judgeResult", judgeResult,
                "actualPrivateSticks", room.getPrivateSticks(),
                "declaredPrivateSticks", room.getDeclaredPrivateSticks(),
                "publicSticks", room.getPublicSticks(),
                "actualResult", room.getCurrentYutResult(),
                "nextRoom", "YUT_ROOM");
    }

}