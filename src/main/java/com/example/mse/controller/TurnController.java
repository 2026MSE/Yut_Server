package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameActionRequest;
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

    // 찬미 dto 전환
    @PostMapping("/next")
    public Object nextTurn(@RequestBody GameActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        turnService.nextTurn(room);

        return ApiResponse.ok(
                "Turn moved to next player.",
                room.getTurnInfo());
    }

}