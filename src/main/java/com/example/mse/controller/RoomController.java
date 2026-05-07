package com.example.mse.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.PlayerInfo;
import com.example.mse.dto.RoomInfo;
import com.example.mse.model.GameRoom;
import com.example.mse.service.PlayerService;
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

    @Autowired
    private PlayerService playerService;

    // 찬미 RoomInfo 관련 코드 수정
    @GetMapping("/create")
    public RoomInfo createRoom(@RequestParam String playerId) {
        GameRoom room = roomService.createRoom(playerId);
        return roomService.toRoomInfo(room);
    }

    @GetMapping("/join")
    public RoomInfo joinRoom(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.joinRoom(roomId, playerId);
        return roomService.toRoomInfo(room);
    }

    @GetMapping("/state")
    public RoomInfo getRoomState(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);
        return roomService.toRoomInfo(room);
    }

    @GetMapping("/all")
    public Map<String, RoomInfo> getAllRooms() {
        Map<String, RoomInfo> result = new java.util.HashMap<>();

        for (Map.Entry<String, GameRoom> entry : roomService.getAllRooms().entrySet()) {
            result.put(entry.getKey(), roomService.toRoomInfo(entry.getValue()));
        }

        return result;
    }

    @GetMapping("/start")
    public RoomInfo startRoom(@RequestParam String roomId) {
        GameRoom room = roomService.startRoom(roomId);
        turnService.startGame(room);
        return roomService.toRoomInfo(room);
    }
    
    //찬미 플레이어 정보 확인 endpoint 추가
    @GetMapping("/players")
    public List<PlayerInfo> getRoomPlayers(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);
        return playerService.getPlayerInfoByIds(room.getPlayerIds());
    }
}