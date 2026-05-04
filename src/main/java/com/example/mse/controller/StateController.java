package com.example.mse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.PlayerInfo;
import com.example.mse.dto.TurnInfo;
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
    public List<PlayerInfo> getPlayerInfo(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);
        return playerService.getPlayerInfoByIds(room.getPlayerIds());
    }

    @GetMapping("/turnInfo")
    public TurnInfo getTurnInfo(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);
        return room.getTurnInfo();
    }
}