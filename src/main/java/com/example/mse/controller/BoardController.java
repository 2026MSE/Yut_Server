// 26.05.08 찬미 yutcontroller이랑 통합
// 26.05.13 TurnPhase 기반으로 변경
package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.MoveGroup;
import com.example.mse.dto.MoveListResponse;
import com.example.mse.dto.MoveOption;
import com.example.mse.dto.MoveRequest;
import com.example.mse.dto.MoveResultResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.MoveType;
import com.example.mse.model.Piece;
import com.example.mse.model.TurnPhase;
import com.example.mse.model.YutResult;
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
    public ApiResponse<MoveResultResponse> movePiece(@RequestBody MoveRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.YUT_MOVE) {
            return ApiResponse.fail("Not in YUT_MOVE phase.");
        }

        if (room.getPendingYutResults() == null || room.getPendingYutResults().isEmpty()) {
            return ApiResponse.fail("No pending yut results.");
        }

        if (request.getYutResultIndex() < 0 ||
                request.getYutResultIndex() >= room.getPendingYutResults().size()) {
            return ApiResponse.fail("Invalid yut result index.");
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

        YutResult selectedResult = room.getPendingYutResults().get(request.getYutResultIndex());

        int fromPosition = movingPiece.getCurrentPosition();

        int targetPos = boardService.calculateNextPath(
                room.getBoard(),
                movingPiece.getCurrentPosition(),
                selectedResult.getMove());

        // 실제 이동 전에 이동할 말 목록 저장
        List<String> movedPieceIds = new ArrayList<>();
        movedPieceIds.add(movingPiece.getId());

        for (Piece carried : movingPiece.getCarriedPieces()) {
            movedPieceIds.add(carried.getId());
        }

        // 실제 이동 전에 잡힐 말 목록 저장
        List<String> caughtPieceIds = new ArrayList<>();

        if (targetPos != -1 && targetPos != 99 && movingPiece.getCurrentPosition() != targetPos) {
            List<Piece> targetPieces = room.getBoard().getNodePiecesMap().get(targetPos);

            if (targetPieces != null && !targetPieces.isEmpty()) {
                Piece targetPiece = targetPieces.get(0);

                if (!targetPiece.getOwnerId().equals(movingPiece.getOwnerId())) {
                    caughtPieceIds.add(targetPiece.getId());

                    for (Piece carried : targetPiece.getCarriedPieces()) {
                        caughtPieceIds.add(carried.getId());
                    }
                }
            }
        }

        MoveType moveType = boardService.movePieceAndCheckCatch(
                room.getBoard(),
                movingPiece,
                targetPos);

        MoveResultResponse response = new MoveResultResponse();
        response.setPieceId(movingPiece.getId());
        response.setMovedPieceIds(movedPieceIds);
        response.setCaughtPieceIds(caughtPieceIds);
        response.setFromPosition(fromPosition);
        response.setToPosition(targetPos);
        response.setMoveType(moveType);

        room.getPendingYutResults().remove(request.getYutResultIndex());

        if (boardService.isPlayerFinished(room.getBoard(), request.getPlayerId())) {
            gameFlowService.endGame(room, request.getPlayerId());

            response.setExtraTurn(false);
            response.setGameOver(true);
            response.setWinnerId(request.getPlayerId());

            gameFlowService.recordMoveResult(room, response, request.getPlayerId());

            return ApiResponse.ok("Game over. Winner: " + request.getPlayerId(), response);
        }

        if (moveType == MoveType.CATCH) {
            gameFlowService.startCatchBonusThrow(room);

            response.setExtraTurn(true);
            response.setGameOver(false);
            response.setWinnerId(null);

            gameFlowService.recordMoveResult(room, response, request.getPlayerId());

            return ApiResponse.ok("Piece moved. Catch bonus throw.", response);
        }

        response.setExtraTurn(false);
        response.setGameOver(false);
        response.setWinnerId(null);

        gameFlowService.handleMoveResult(room);

        gameFlowService.recordMoveResult(room, response, request.getPlayerId());

        return ApiResponse.ok("Piece moved.", response);
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

        List<Piece> pieces = room.getBoard().getPieces().get(playerId);

        if (pieces == null) {
            return ApiResponse.fail("Can not find the Player.");
        }

        if (room.getPendingYutResults() == null || room.getPendingYutResults().isEmpty()) {
            return ApiResponse.fail("No pending yut results.");
        }

        List<MoveGroup> moveGroups = new ArrayList<>();

        for (int i = 0; i < room.getPendingYutResults().size(); i++) {
            YutResult yutResult = room.getPendingYutResults().get(i);

            List<MoveOption> options = new ArrayList<>();

            for (Piece piece : pieces) {
                if (piece.getCurrentPosition() == 99) {
                    continue;
                }

                if (piece.getCarriedByPieceId() != null) {
                    continue;
                }

                int targetPosition = boardService.calculateNextPath(
                        room.getBoard(),
                        piece.getCurrentPosition(),
                        yutResult.getMove());

                MoveType moveType = boardService.predictMoveType(
                        room.getBoard(),
                        piece,
                        targetPosition);

                MoveOption option = new MoveOption(
                        piece.getId(),
                        piece.getCurrentPosition(),
                        targetPosition,
                        targetPosition == 99,
                        moveType);

                options.add(option);
            }

            MoveGroup group = new MoveGroup(
                    i,
                    yutResult.getResult(),
                    yutResult.getMove(),
                    options);

            moveGroups.add(group);
        }

        MoveListResponse response = new MoveListResponse(moveGroups);

        return ApiResponse.ok("Move list loaded.", response);
    }
}