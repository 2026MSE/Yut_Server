// 26.05.08 찬미 yutcontroller이랑 통합
package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.GameActionRequest;
import com.example.mse.dto.MoveRequest;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Piece;
import com.example.mse.model.Scene;
import com.example.mse.service.BoardService;
import com.example.mse.service.PrivateService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/board")
@CrossOrigin(origins = "*") // 모든 도메인에서의 접속을 허용 (프론트엔드 통신용)
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private PrivateService privateService;

    @Autowired
    private TurnService turnService;

    // 찬미 /board/move는 말을 이동시키는 버튼 역할만 하게 변경
    @PostMapping("/move")
    public ApiResponse<Void> movePiece(@RequestBody MoveRequest moveRequest) {

        GameRoom room = roomService.requireRoom(moveRequest.getRoomId());

        List<Piece> playerPieces = room.getBoard().getPieces().get(moveRequest.getPlayerId());

        if (playerPieces == null) {
            return ApiResponse.fail("Can not find the Player.");
        }

        Piece targetPiece = playerPieces.stream()
                .filter(p -> p.getId().equals(moveRequest.getPieceId()))
                .findFirst()
                .orElse(null);

        if (targetPiece == null) {
            return ApiResponse.fail("Can not find the Piece to move.");
        }

        int nextPos = boardService.calculateNextPath(
                room.getBoard(),
                targetPiece.getCurrentPosition(),
                moveRequest.getMoveAmount());

        boardService.movePieceAndCheckCatch(
                room.getBoard(),
                targetPiece,
                nextPos);

        return ApiResponse.ok("complete to move", null);
    }

    @GetMapping("/state")
    public ApiResponse<BoardStatusResponse> getBoardState(@RequestParam String roomId) {

        GameRoom room = roomService.requireRoom(roomId);

        BoardStatusResponse status = new BoardStatusResponse();

        status.setAllPieces(room.getBoard().getPieces());

        status.setExtraTurn(
                room.getCurrentYutResult() != null &&
                        room.getCurrentYutResult().isExtraTurn());

        ThrowResponse throwResponse = new ThrowResponse();
        throwResponse.setSticks(room.getSticks());
        throwResponse.setPrivateSticks(room.getPrivateSticks());
        throwResponse.setPublicSticks(room.getPublicSticks());
        throwResponse.setYutResult(room.getCurrentYutResult());

        status.setThrowResult(throwResponse);

        status.setCurrentTurnPlayerId(
                room.getTurnInfo().getCurrentTurnPlayerId());

        status.setCurrentRoom(
                room.getTurnInfo().getCurrentTurnPlayerRoom());

        status.setAlreadyThrown(room.isAlreadyThrown());
        status.setAlreadyMoved(room.isAlreadyMoved());

        status.setHallState(room.getHallState());

        return ApiResponse.ok("Board state loaded.", status);
    }

    @PostMapping("/throw")
    public ApiResponse<Void> throwYut(@RequestBody GameActionRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        if (!turnService.isTurnPlayer(room, request.getPlayerId())) {
            return ApiResponse.fail("Not your turn.");
        }

        if (room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.YUT_ROOM
                && room.getTurnInfo().getCurrentTurnPlayerRoom() != Scene.PRIVATE_ROOM) {
            return ApiResponse.fail("Not in throwable room.");
        }

        if (room.isAlreadyThrown()) {
            return ApiResponse.fail("Already thrown.");
        }

        privateService.getThrowResponse(room);

        return ApiResponse.ok("Yut thrown.", null);
    }
}