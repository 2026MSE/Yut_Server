package com.example.mse.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.PlayerActionRequest;
import com.example.mse.dto.PlayerInfo;
import com.example.mse.dto.RoomCreateRequest;
import com.example.mse.dto.RoomInfo;
import com.example.mse.dto.RoomRequest;
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
    @PostMapping("/create")
    public RoomInfo createRoom(@RequestBody RoomCreateRequest request) {
        GameRoom room = roomService.createRoom(request.getPlayerId());
        return roomService.toRoomInfo(room);
    }

    @PostMapping("/join")
    public RoomInfo joinRoom(@RequestBody PlayerActionRequest request) {
        GameRoom room = roomService.joinRoom(
                request.getRoomId(),
                request.getPlayerId());
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

    @PostMapping("/start")
    public RoomInfo startRoom(@RequestBody RoomRequest request) {
        GameRoom room = roomService.startRoom(request.getRoomId());
        turnService.startGame(room);
        return roomService.toRoomInfo(room);
    }

    // 찬미 플레이어 정보 확인 endpoint 추가
    @GetMapping("/players")
    public List<PlayerInfo> getRoomPlayers(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);
        return playerService.getPlayerInfoByIds(room.getPlayerIds());
    }
}