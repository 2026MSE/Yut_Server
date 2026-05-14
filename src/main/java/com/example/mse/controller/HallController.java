// 26.05.13 TurnPhase 기반으로 변경
package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.DeclareRequest;
import com.example.mse.dto.GameActionRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Piece;
import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;
import com.example.mse.model.TurnPhase;
import com.example.mse.service.BoardService;
import com.example.mse.service.GameFlowService;
import com.example.mse.service.HallService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.dto.JudgeResponse;
import com.example.mse.dto.JudgeResponse.JudgeResult;

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

    @Autowired
    private BoardService boardService;

    @PostMapping("/declare")
    public Object declare(@RequestBody DeclareRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_DECLARE) {
            return ApiResponse.fail("Not in MAIN_HALL_DECLARE phase.");
        }

        if (request.getS1() == StickSide.TAIL) {
            return ApiResponse.fail("Invalid declaration: first private stick cannot be TAIL.");
        }

        if (request.getS2() == StickSide.BACK) {
            return ApiResponse.fail("Invalid declaration: second private stick cannot be BACK.");
        }

        hallService.declarePrivateSticks(room, request.getS1(), request.getS2());

        gameFlowService.startChallengePhase(room);

        return ApiResponse.ok("Declared private sticks.", null);
    }

    @PostMapping("/challenge")
    public Object challenge(@RequestBody GameActionRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Turn player cannot challenge");
        }

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_CHALLENGE) {
            return ApiResponse.fail("Not in MAIN_HALL_CHALLENGE phase.");
        }

        String result = hallService.challenge(room, request.getPlayerId());

        return ApiResponse.ok(result, null);
    }

    @PostMapping("/judge")
    public Object judge(@RequestBody GameActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (room.getCurrentYutResult() == null) {
            return ApiResponse.fail("No yut result yet.");
        }

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_CHALLENGE) {
            return ApiResponse.fail("Not in MAIN_HALL_CHALLENGE phase.");
        }

        if (room.getFirstChallengerId() == null) {
            return ApiResponse.fail("No challenger.");
        }

        if (!request.getPlayerId().equals(room.getFirstChallengerId())) {
            return ApiResponse.fail("Only first challenger can judge.");
        }

        JudgeResult judgeResult = hallService.judgeChallenge(room);

        JudgeResponse response = new JudgeResponse();
        response.setJudgeResult(judgeResult);

        if (judgeResult == JudgeResult.CHALLENGE_SUCCESS) {
            response.setRewardChanceCard(true);
            response.setPenaltyApplied(false);
        } else {
            Piece penaltyPiece = boardService.sendFirstPieceToStart(
                    room.getBoard(),
                    room.getFirstChallengerId());

            response.setRewardChanceCard(false);
            response.setPenaltyApplied(penaltyPiece != null);

            if (penaltyPiece != null) {
                response.setPenaltyPieceId(penaltyPiece.getId());
            }
        }

        room.setChallengeResolved(true);
        gameFlowService.startMovePhase(room);

        response.setActualPrivateSticks(room.getPrivateSticks());
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());
        response.setPublicSticks(room.getPublicSticks());
        response.setActualResult(room.getCurrentYutResult());
        response.setNextRoom(Scene.YUT_ROOM);

        return ApiResponse.ok("Challenge judged.", response);
    }
}