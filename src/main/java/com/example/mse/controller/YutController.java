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
import com.example.mse.model.YutResult;
import com.example.mse.service.BoardService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

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

    @GetMapping("/end")
    public Object endTurn(
            @RequestParam String roomId,
            @RequestParam String playerId) {
        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }

        // 찬미 현재 턴 플레이어가 YUT_ROOM에 있을 때만 턴 종료 가능하도록 확인
        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.YUT_ROOM) {
            return "Not in YUT_ROOM.";
        }

        turnService.nextTurn(room);

        return room.getTurnInfo();
    }

    // 찬미 윷 이동 api 추가
    @GetMapping("/move")
    public Object movePiece(
            @RequestParam String roomId,
            @RequestParam String playerId,
            @RequestParam String pieceId) {

        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return "Not your turn.";
        }

        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.YUT_ROOM) {
            return "Not in YUT_ROOM.";
        }

        YutResult yutResult = room.getCurrentYutResult();

        if (yutResult == null) {
            return "No yut result.";
        }

        Board board = room.getBoard();

        Piece movingPiece = boardService.findPiece(board, playerId, pieceId);

        if (movingPiece == null) {
            return "Piece not found.";
        }

        if (movingPiece.getCurrentPosition() == 99) {
            return "This piece already finished.";
        }

        int fromPos = movingPiece.getCurrentPosition();
        int targetPos = boardService.calculateNextPath(
                board,
                movingPiece.getCurrentPosition(),
                yutResult.getMove());

        MoveType moveType = boardService.movePieceAndCheckCatch(
                board,
                movingPiece,
                targetPos);

        Map<String, Object> result = new HashMap<>();

        result.put("message", "Piece moved");
        result.put("pieceId", pieceId);
        result.put("from", fromPos);
        result.put("to", targetPos);
        result.put("moveType", moveType);
        result.put("yutResult", yutResult);

        return result;
    }
}