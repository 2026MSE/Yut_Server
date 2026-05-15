// 26.05.13 BoardController에서 윷 던지고 턴 종료 부분 분리해옴
package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameActionRequest;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.TurnPhase;
import com.example.mse.service.GameFlowService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.service.YutService;

@RestController
@RequestMapping("/turn")
@CrossOrigin(origins = "*")
public class TurnController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private YutService yutService;

    @Autowired
    private GameFlowService gameFlowService;

    @PostMapping("/throw")
    public ApiResponse<ThrowResponse> throwYut(@RequestBody GameActionRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.PRIVATE_THROW) {
            return ApiResponse.fail("Not in PRIVATE_THROW phase.");
        }

        ThrowResponse response = yutService.getThrowResponse(room);

        gameFlowService.startDeclarePhase(room);

        gameFlowService.addLog(
                room,
                "THROW",
                request.getPlayerId() + " threw yut: " + response.getYutResult().getResult());

        return ApiResponse.ok("Yut thrown.", response);
    }

    @PostMapping("/end")
    public ApiResponse<Void> endTurn(@RequestBody GameActionRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.YUT_MOVE_DONE) {
            return ApiResponse.fail("You must move before ending turn.");
        }

        gameFlowService.endTurn(room);

        return ApiResponse.ok("Turn ended.", null);
    }
}