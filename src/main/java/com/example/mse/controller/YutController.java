package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.model.GameRoom;
import com.example.mse.model.Scene;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

@RestController
@RequestMapping("/yut")
@CrossOrigin(origins = "*")

public class YutController {

    @Autowired
    private TurnService turnService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/end")
    public Object endTurn(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }

        // 찬미 현재 턴 플레이어가 YUT_ROOM에 있을 때만 턴 종료 가능하도록 확인
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.YUT_ROOM) {
            return "Not in YUT_ROOM.";
        }

        turnService.nextTurn(room);

        return room.getTurnInfo();
    }
}