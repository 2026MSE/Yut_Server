package com.example.mse.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.DeclareRequest;
import com.example.mse.dto.RoomRequest;
import com.example.mse.dto.PlayerActionRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.HallState;
import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;
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

    @GetMapping("/state")
    public Object getState(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);
        return room.getHallState();
    }

    @GetMapping("/info")
    public Object info(@RequestParam String roomId) {
        GameRoom room = roomService.requireRoom(roomId);

        Map<String, Object> result = new java.util.HashMap<>();

        result.put("state", room.getHallState());
        result.put("publicSticks", room.getPublicSticks());
        result.put("declaredPrivateSticks", room.getDeclaredPrivateSticks());
        result.put("firstChallenger", room.getFirstChallengerId());
        result.put("queue", room.getChallengeQueue());

        return result;
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

        // 찬미 Map.of-> HashMap으로 변경
        Map<String, Object> result = new java.util.HashMap<>();

        result.put("message", "Declared private sticks");
        result.put("declaredPrivateSticks", room.getDeclaredPrivateSticks());
        result.put("publicSticks", room.getPublicSticks());
        result.put("state", room.getHallState());

        return result;
    }

    @PostMapping("/challenge")
    public Object challenge(@RequestBody PlayerActionRequest request) {
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
    public Object judge(@RequestBody RoomRequest request) {
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

        String judgeResult = hallService.judgeChallenge(room);

        turnService.moveCurrentTurnPlayerRoom(room, Scene.YUT_ROOM);

        // 찬미 Map.of-> HashMap으로 변경
        Map<String, Object> result = new java.util.HashMap<>();

        result.put("judgeResult", judgeResult);
        result.put("actualPrivateSticks", room.getPrivateSticks());
        result.put("declaredPrivateSticks", room.getDeclaredPrivateSticks());
        result.put("publicSticks", room.getPublicSticks());
        result.put("actualResult", room.getCurrentYutResult());
        result.put("nextRoom", "YUT_ROOM");

        return result;
    }
}