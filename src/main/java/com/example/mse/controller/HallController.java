// 26.05.13 TurnPhase 기반으로 변경
package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.DeclareRequest;
import com.example.mse.dto.DeclareResponse;
import com.example.mse.dto.GameActionRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;
import com.example.mse.model.TurnPhase;
import com.example.mse.service.HallService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.dto.JudgeResponse;

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

        room.setTurnPhase(TurnPhase.MAIN_HALL_CHALLENGE);

        DeclareResponse response = new DeclareResponse();

        response.setMessage("Declared private sticks");
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());
        response.setPublicSticks(room.getPublicSticks());

        return ApiResponse.ok("Declared private sticks.", response);
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

        // 찬미 도전자가 없으면 판정 불가하도록 확인
        if (room.getFirstChallengerId() == null) {
            return ApiResponse.fail("No challenger.");
        }

        String judgeResult = hallService.judgeChallenge(room);

        turnService.moveCurrentTurnPlayerRoom(room, Scene.YUT_ROOM);

        room.setTurnPhase(TurnPhase.YUT_MOVE);

        // 찬미 Map.of-> HashMap으로 변경 -> JudgeResponse DTO로 변경
        JudgeResponse response = new JudgeResponse();

        response.setJudgeResult(judgeResult);
        response.setActualPrivateSticks(room.getPrivateSticks());
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());
        response.setPublicSticks(room.getPublicSticks());
        response.setActualResult(room.getCurrentYutResult());
        response.setNextRoom("YUT_ROOM");

        return ApiResponse.ok("Challenge judged.", response);
    }
}