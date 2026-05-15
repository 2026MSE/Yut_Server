// 26.05.08 찬미 yutcontroller이랑 통합
// 26.05.13 TurnPhase 기반으로 변경
package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.MoveListResponse;
import com.example.mse.dto.MoveOption;
import com.example.mse.dto.MoveRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.MoveType;
import com.example.mse.model.Piece;
import com.example.mse.model.TurnPhase;
import com.example.mse.service.BoardService;
import com.example.mse.service.GameFlowService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/board")
@CrossOrigin(origins = "*") // 모든 도메인에서의 접속을 허용 (프론트엔드 통신용)
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private GameFlowService gameFlowService;

    // 찬미 /board/move는 말을 이동시키는 버튼 역할만 하게 변경
    // yut/move랑 통합, currentYutResult 기준으로 이동
    @PostMapping("/move")
    public ApiResponse<Void> movePiece(@RequestBody MoveRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.YUT_MOVE) {
            return ApiResponse.fail("Not in YUT_MOVE phase.");
        }

        if (room.getCurrentYutResult() == null) {
            return ApiResponse.fail("No yut result.");
        }

        Piece movingPiece = boardService.findPiece(
                room.getBoard(),
                request.getPlayerId(),
                request.getPieceId());

        if (movingPiece == null) {
            return ApiResponse.fail("Piece not found.");
        }

        if (movingPiece.getCarriedByPieceId() != null) {
            return ApiResponse.fail("Carried piece cannot move alone.");
        }

        if (movingPiece.getCurrentPosition() == 99) {
            return ApiResponse.fail("This piece already finished.");
        }

        int targetPos = boardService.calculateNextPath(
                room.getBoard(),
                movingPiece.getCurrentPosition(),
                room.getCurrentYutResult().getMove());

        MoveType moveType = boardService.movePieceAndCheckCatch(
                room.getBoard(),
                movingPiece,
                targetPos);

        if (boardService.isPlayerFinished(room.getBoard(), request.getPlayerId())) {
            gameFlowService.endGame(room, request.getPlayerId());
            return ApiResponse.ok("Game over. Winner: " + request.getPlayerId(), null);
        }

        boolean extraTurn = room.getCurrentYutResult().isExtraTurn()
                || moveType == MoveType.CATCH;

        gameFlowService.handleMoveResult(room, extraTurn);

        return ApiResponse.ok("Piece moved.", null);
    }

    @GetMapping("/moveList")
    public ApiResponse<MoveListResponse> getMoveList(
            @RequestParam String roomId,
            @RequestParam String playerId) {

        GameRoom room = roomService.requireRoom(roomId);

        if (!turnService.isTurnPlayer(room, playerId)) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.YUT_MOVE) {
            return ApiResponse.fail("Not in YUT_MOVE phase.");
        }

        if (room.getCurrentYutResult() == null) {
            return ApiResponse.fail("No yut result.");
        }

        List<Piece> pieces = room.getBoard().getPieces().get(playerId);

        if (pieces == null) {
            return ApiResponse.fail("Can not find the Player.");
        }

        List<MoveOption> options = new ArrayList<>();

        for (Piece piece : pieces) {
            if (piece.getCurrentPosition() == 99) {
                continue;
            }

            // 다른 말에게 업혀 있는 말은 단독 이동 불가
            if (piece.getCarriedByPieceId() != null) {
                continue;
            }

            int targetPosition = boardService.calculateNextPath(
                    room.getBoard(),
                    piece.getCurrentPosition(),
                    room.getCurrentYutResult().getMove());

            MoveType moveType = boardService.predictMoveType(
                    room.getBoard(),
                    piece,
                    targetPosition);

            MoveOption option = new MoveOption(
                    piece.getId(),
                    piece.getCurrentPosition(),
                    targetPosition,
                    targetPosition == 99,
                    moveType,
                    moveType == MoveType.CATCH,
                    moveType == MoveType.PIGGYBACK);

            options.add(option);
        }

        MoveListResponse response = new MoveListResponse(options);

        return ApiResponse.ok("Move list loaded.", response);
    }
}