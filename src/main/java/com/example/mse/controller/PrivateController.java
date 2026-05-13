// 26.05.13 TurnPhase 기반으로 변경
package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameActionRequest;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Scene;
import com.example.mse.model.TurnPhase;

@RestController
@RequestMapping("/private")
@CrossOrigin(origins = "*")

public class PrivateController {

    @Autowired
    private TurnService turnService;

    @Autowired
    private RoomService roomService;

    // YutRoom으로 이동하기 위하여 유지
    @PostMapping("/exit")
    public Object exitPrivate(@RequestBody GameActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_DECLARE) {
            return ApiResponse.fail("You must throw yut first.");
        }

        turnService.moveCurrentTurnPlayerRoom(room, Scene.MAIN_HALL);

        return ApiResponse.ok("Moved to MAIN_HALL", null);
    }

    // 찬미 불필요 데이터 삭제, 조회만 가능하도록 변경
    @GetMapping("/info")
    public Object privateInfo(
            @RequestParam String roomId,
            @RequestParam String playerId) {

        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return ApiResponse.fail("Not your turn.");
        }

        ThrowResponse response = new ThrowResponse();
        response.setSticks(room.getSticks());
        response.setPrivateSticks(room.getPrivateSticks());
        response.setPublicSticks(room.getPublicSticks());
        response.setYutResult(room.getCurrentYutResult());

        return ApiResponse.ok("Private info loaded.", response);
    }
}