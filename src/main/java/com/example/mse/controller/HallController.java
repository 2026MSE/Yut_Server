package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.DeclareRequest;
import com.example.mse.dto.DeclareResponse;
import com.example.mse.dto.HallInfoResponse;
import com.example.mse.dto.GameActionRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.HallState;
import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;
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

    @GetMapping("/state")
    public Object getState(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);

        return ApiResponse.ok(
                "Hall state loaded.",
                room.getHallState());
    }

    // 찬미 dto로 변경
    @GetMapping("/info")
    public Object info(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);

        HallInfoResponse response = new HallInfoResponse();

        response.setState(room.getHallState());
        response.setPublicSticks(room.getPublicSticks());
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());
        response.setFirstChallenger(room.getFirstChallengerId());
        response.setQueue(room.getChallengeQueue());

        return ApiResponse.ok("Hall info loaded.", response);
    }

    @PostMapping("/declare")
    public Object declare(@RequestBody DeclareRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        // 선언은 MAIN_HALL + DECLARE 상태에서만 가능하도록 확인
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.MAIN_HALL) {
            return ApiResponse.fail("Not in MAIN_HALL.");
        }

        if (room.getHallState() != HallState.DECLARE) {
            return ApiResponse.fail("Not in DECLARE phase.");
        }

        if (request.getS1() == StickSide.TAIL) {
            return ApiResponse.fail("Invalid declaration: first private stick cannot be TAIL.");
        }

        if (request.getS2() == StickSide.BACK) {
            return ApiResponse.fail("Invalid declaration: second private stick cannot be BACK.");
        }

        hallService.declarePrivateSticks(room, request.getS1(), request.getS2());

        // 찬미 Map.of-> HashMap으로 변경 -> dto로 변경
        DeclareResponse response = new DeclareResponse();

        response.setMessage("Declared private sticks");
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());
        response.setPublicSticks(room.getPublicSticks());
        response.setState(room.getHallState());
        
        turnService.moveCurrentTurnPlayerRoom(room, Scene.YUT_ROOM);

        return ApiResponse.ok("Declared private sticks.", response);
    }

    @PostMapping("/challenge")
    public Object challenge(@RequestBody GameActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Turn player cannot challenge");
        }

        // 도전은 MAIN_HALL + CHALLENGE 상태에서만 가능하도록 확인
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.MAIN_HALL) {
            return ApiResponse.fail("Not in MAIN_HALL.");
        }

        if (room.getHallState() != HallState.CHALLENGE) {
            return ApiResponse.fail("Not in CHALLENGE phase.");
        }

        return hallService.challenge(room, request.getPlayerId());
    }

    @PostMapping("/judge")
    public Object judge(@RequestBody GameActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (room.getCurrentYutResult() == null) {
            return ApiResponse.fail("No yut result yet.");
        }

        // 판정은 MAIN_HALL + CHALLENGE 상태에서만 가능하도록 확인
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.MAIN_HALL) {
            return ApiResponse.fail("Not in MAIN_HALL.");
        }

        if (room.getHallState() != HallState.CHALLENGE) {
            return ApiResponse.fail("Not in CHALLENGE phase.");
        }

        // 찬미 도전자가 없으면 판정 불가하도록 확인
        if (room.getFirstChallengerId() == null) {
            return ApiResponse.fail("No challenger.");
        }

        String judgeResult = hallService.judgeChallenge(room);

        turnService.moveCurrentTurnPlayerRoom(room, Scene.YUT_ROOM);

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