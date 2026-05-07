package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.service.PlayerService;
import com.example.mse.service.RoomService;
import com.example.mse.model.GameRoom;

@RestController
@RequestMapping("/state")
@CrossOrigin(origins = "*")

public class StateController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/playerInfo")
    public Object getPlayerInfo(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);

        return ApiResponse.ok(
                "Player info loaded.",
                playerService.getPlayerInfoByIds(room.getPlayerIds()));
    }

    @GetMapping("/turnInfo")
    public Object getTurnInfo(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);

        return ApiResponse.ok(
                "Turn info loaded.",
                room.getTurnInfo());
    }
}