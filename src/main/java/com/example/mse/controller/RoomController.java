package com.example.mse.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameActionRequest;
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
    @PostMapping("/create")
    public ApiResponse<RoomInfo> createRoom(@RequestBody GameActionRequest request) {

        if (playerService.get(request.getPlayerId()) == null) {
            return ApiResponse.fail("Player not found.");
        }

        GameRoom room = roomService.createRoom(request.getPlayerId());

        return ApiResponse.ok(
                "Room created.",
                roomService.toRoomInfo(room));
    }

    @PostMapping("/join")
    public ApiResponse<RoomInfo> joinRoom(@RequestBody GameActionRequest request) {

        if (playerService.get(request.getPlayerId()) == null) {
            return ApiResponse.fail("Player not found.");
        }

        GameRoom room = roomService.joinRoom(
                request.getRoomId(),
                request.getPlayerId());

        return ApiResponse.ok(
                "Joined room.",
                roomService.toRoomInfo(room));
    }

    @GetMapping("/all")
    public ApiResponse<Map<String, RoomInfo>> getAllRooms() {

        Map<String, RoomInfo> result = new java.util.HashMap<>();

        for (Map.Entry<String, GameRoom> entry : roomService.getAllRooms().entrySet()) {
            result.put(entry.getKey(), roomService.toRoomInfo(entry.getValue()));
        }

        return ApiResponse.ok(
                "All rooms loaded.",
                result);
    }

    @PostMapping("/start")
    public ApiResponse<RoomInfo> startRoom(@RequestBody GameActionRequest request) {

        GameRoom roomBeforeStart = roomService.requireRoom(request.getRoomId());

        if (!request.getPlayerId().equals(roomBeforeStart.getHostId())) {
            return ApiResponse.fail("Only host can start the game.");
        }

        GameRoom room = roomService.startRoom(request.getRoomId());
        turnService.startGame(room);

        return ApiResponse.ok(
                "Game started.",
                roomService.toRoomInfo(room));
    }

    // 찬미 플레이어 정보 확인 endpoint 추가
    @GetMapping("/players")
    public ApiResponse<List<PlayerInfo>> getRoomPlayers(
            @RequestParam String roomId) {

        GameRoom room = roomService.requireRoom(roomId);

        return ApiResponse.ok(
                "Room players loaded.",
                playerService.getPlayerInfoByIds(room.getPlayerIds()));
    }

    @PostMapping("/leave")
    public ApiResponse<RoomInfo> leaveRoom(@RequestBody GameActionRequest request) {
        GameRoom room = roomService.leaveRoom(
                request.getRoomId(),
                request.getPlayerId());

        return ApiResponse.ok(
                "Left room.",
                roomService.toRoomInfo(room));
    }
}