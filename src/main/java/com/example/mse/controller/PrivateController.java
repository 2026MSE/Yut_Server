package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameActionRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.TurnPhase;
import com.example.mse.service.GameFlowService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

@RestController
@RequestMapping("/private")
@CrossOrigin(origins = "*")

public class PrivateController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private GameFlowService gameFlowService;

    @PostMapping("/exit")
    public ApiResponse<Void> exitPrivateRoom(@RequestBody GameActionRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getCurrentYutResult() == null) {
            return ApiResponse.fail("No yut result to confirm.");
        }

        if (room.getTurnPhase() == TurnPhase.PRIVATE_THROW_RESULT) {
            gameFlowService.exitPrivateThrowResult(room);

            gameFlowService.addLog(
                    room,
                    "PRIVATE_EXIT",
                    request.getPlayerId() + " exited private room.");

            return ApiResponse.ok("Exited private room.", null);
        }

        if (room.getTurnPhase() == TurnPhase.CATCH_BONUS_THROW_RESULT) {
            gameFlowService.exitCatchBonusThrowResult(room);

            gameFlowService.addLog(
                    room,
                    "PRIVATE_EXIT",
                    request.getPlayerId() + " exited catch bonus result room.");

            return ApiResponse.ok("Exited catch bonus result room.", null);
        }

        return ApiResponse.fail("You can exit only after checking the throw result.");
    }
}