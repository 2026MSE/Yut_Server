package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.MoveRequest;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Piece;
import com.example.mse.service.BoardService;
import com.example.mse.service.RoomService;
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

        if (room == null) {
            return ApiResponse.fail("존재하지 않는 방입니다.");
        }

        // 현재 윷판 상태만 담아서 응답
        BoardStatusResponse status = new BoardStatusResponse(room.getBoard().getPieces());
        return ApiResponse.ok("윷판 상태 조회 성공", status);
    }

}