// 26.05.13 TurnPhase 기반으로 변경
package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.ChallengeVoteRequest;
import com.example.mse.dto.DeclareRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.StickSide;
import com.example.mse.model.TurnPhase;
import com.example.mse.service.GameFlowService;
import com.example.mse.service.HallService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

@RestController
@RequestMapping("/hall")
@CrossOrigin(origins = "*")
public class HallController {

    @Autowired
    private HallService hallService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private GameFlowService gameFlowService;

    @PostMapping("/declare")
    public ApiResponse<Void> declare(@RequestBody DeclareRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_DECLARE) {
            return ApiResponse.fail("Not in MAIN_HALL_DECLARE phase.");
        }

        if (room.getCurrentYutResult() == null) {
            return ApiResponse.fail("No yut result yet.");
        }

        if (request.getS1() == null || request.getS2() == null) {
            return ApiResponse.fail("Declared sticks are required.");
        }

        if (request.getS1() == StickSide.TAIL) {
            return ApiResponse.fail("Invalid declaration: first private stick cannot be TAIL.");
        }

        if (request.getS2() == StickSide.BACK) {
            return ApiResponse.fail("Invalid declaration: second private stick cannot be BACK.");
        }

        hallService.declarePrivateSticks(room, request.getS1(), request.getS2());

        gameFlowService.startChallengePhase(room);

        gameFlowService.addLog(
                room,
                "DECLARE",
                request.getPlayerId() + " declared private sticks.");

        return ApiResponse.ok("Declared private sticks.", null);
    }

    @PostMapping("/challenge")
    public ApiResponse<Void> challenge(@RequestBody ChallengeVoteRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Turn player cannot vote challenge.");
        }

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_CHALLENGE) {
            return ApiResponse.fail("Not in MAIN_HALL_CHALLENGE phase.");
        }

        String result = hallService.voteChallenge(
                room,
                request.getPlayerId(),
                request.isChallenge());

        gameFlowService.resolveChallengeIfReady(room);

        return ApiResponse.ok(result, null);
    }
}