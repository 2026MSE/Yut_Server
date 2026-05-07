//26.05.07 찬미 String 반환 방식에서 ApiResponse 객체 반환 방식으로 변경, requestbody DTO로 변경
package com.example.mse.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.model.Board;
import com.example.mse.model.GameRoom;
import com.example.mse.model.MoveType;
import com.example.mse.model.Piece;
import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;
import com.example.mse.model.YutResult;
import com.example.mse.service.BoardService;
import com.example.mse.service.PrivateService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.MoveRequest;
import com.example.mse.dto.PlayerActionRequest;

@RestController
@RequestMapping("/yut")
@CrossOrigin(origins = "*")

public class YutController {

    @Autowired
    private TurnService turnService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private PrivateService privateService;

    @PostMapping("/end")
    public Object endTurn(
            @RequestBody PlayerActionRequest request) {
        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        // 찬미 현재 턴 플레이어가 YUT_ROOM에 있을 때만 턴 종료 가능하도록 확인
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.YUT_ROOM) {
            return ApiResponse.fail("Not in YUT_ROOM.");
        }

        turnService.nextTurn(room);

        return ApiResponse.ok("Turn ended.", room.getTurnInfo());
    }

    // 찬미 윷 이동 api 추가, 추가턴 여부 반환
    @PostMapping("/move")
    public Object movePiece(@RequestBody MoveRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.YUT_ROOM) {
            return ApiResponse.fail("Not in YUT_ROOM.");
        }

        YutResult yutResult = room.getCurrentYutResult();

        if (room.isAlreadyMoved()) {
            return ApiResponse.fail("Already moved this turn.");
        }

        if (yutResult == null) {
            return ApiResponse.fail("No yut result.");
        }

        Board board = room.getBoard();

        Piece movingPiece = boardService.findPiece(board, request.getPlayerId(), request.getPieceId());

        if (movingPiece == null) {
            return ApiResponse.fail("Piece not found.");
        }

        if (movingPiece.getCurrentPosition() == 99) {
            return ApiResponse.fail("This piece already finished.");
        }

        int fromPos = movingPiece.getCurrentPosition();

        int targetPos = boardService.calculateNextPath(
                board,
                fromPos,
                yutResult.getMove());

        MoveType moveType = boardService.movePieceAndCheckCatch(
                board,
                movingPiece,
                targetPos);

        Map<String, Object> result = new HashMap<>();

        boolean extraTurn = yutResult.isExtraTurn() || moveType == MoveType.CATCH;

        if (extraTurn) {
            room.setAlreadyThrown(false);
            room.setAlreadyMoved(false);
            room.setCurrentYutResult(null);

            room.setSticks(new StickSide[4]);
            room.setPrivateSticks(new StickSide[2]);
            room.setPublicSticks(new StickSide[2]);
            room.setDeclaredPrivateSticks(new StickSide[2]);

            result.put("extraTurn", true);
            result.put("nextAction", "THROW_AGAIN_IN_YUT_ROOM");
        } else {
            room.setAlreadyMoved(true);

            result.put("extraTurn", false);
            result.put("nextAction", "END_TURN");
        }

        result.put("message", "Piece moved");
        result.put("pieceId", request.getPieceId());
        result.put("from", fromPos);
        result.put("to", targetPos);
        result.put("moveType", moveType);
        result.put("yutResult", yutResult);

        return ApiResponse.ok("Piece moved.", result);
    }

    @PostMapping("/throw")
    public Object throwAgain(
            @RequestBody PlayerActionRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.YUT_ROOM) {
            return ApiResponse.fail("Not in YUT_ROOM.");
        }

        if (room.isAlreadyThrown()) {
            return ApiResponse.fail("Already thrown.");
        }

        return privateService.getResult(room);
    }
}