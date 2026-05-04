package com.example.mse.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.model.GameRoom;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

@RestController
@RequestMapping("/room")
@CrossOrigin(origins = "*")

public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private TurnService turnService;

    @GetMapping("/create")
    public GameRoom createRoom(@RequestParam String playerId) {
        return roomService.createRoom(playerId);
    }

    @GetMapping("/join")
    public GameRoom joinRoom(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        return roomService.joinRoom(roomId, playerId);
    }

    @GetMapping("/state")
    public GameRoom getRoomState(@RequestParam String roomId) {
        return roomService.getRoom(roomId);
    }

    @GetMapping("/all")
    public Map<String, GameRoom> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/start")
    public GameRoom startRoom(@RequestParam String roomId) {
        GameRoom room = roomService.startRoom(roomId);
        turnService.startGame(room);
        return room;
    }
}