package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.service.PrivateService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.PlayerActionRequest;
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

    @PostMapping("/result")
    public Object getPrivateResult(@RequestBody PlayerActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");

            // 찬미 현재 턴 플레이어가 PRIVATE_ROOM에 있을 때만 윷 던지기 가능하도록 확인
        }
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.PRIVATE_ROOM) {
            return ApiResponse.fail("Not in PRIVATE_ROOM.");
        }

        return privateService.getResult(room);
    }

    @PostMapping("/exit")
    public Object exitPrivate(
            @RequestBody PlayerActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        // 찬미 현재 턴 플레이어가 PRIVATE_ROOM에 있을 때만, 윷을 던진 후에만 퇴장 가능하도록 확인
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.PRIVATE_ROOM) {
            return ApiResponse.fail("Not in PRIVATE_ROOM.");
        }

        if (!room.isAlreadyThrown()) {
            return ApiResponse.fail("You must throw yut first.");
        }

        turnService.moveCurrentTurnPlayerRoom(room, Scene.MAIN_HALL);

        return ApiResponse.ok("Moved to MAIN_HALL",null);
    }

    @GetMapping("/info")
    public Object privateInfo(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return ApiResponse.fail("Not your turn.");
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