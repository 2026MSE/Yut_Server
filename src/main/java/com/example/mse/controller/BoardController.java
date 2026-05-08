package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.MoveRequest;
import com.example.mse.dto.MoveResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.MoveType;
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

    @PostMapping("/move")
    // 반환 타입을 Object에서 ApiResponse<MoveResponse>로 변경했습니다.
    public ApiResponse<MoveResponse> movePiece(@RequestBody MoveRequest moveRequest) {

        GameRoom room = roomService.requireRoom(moveRequest.getRoomId());
        if (room == null) {
            // 에러가 났을 때는 ApiResponse.fail()을 사용합니다.
            return ApiResponse.fail("The room does not exist.");
        }

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
                moveRequest.getMoveAmount()
        );

        MoveType moveEvent = boardService.movePieceAndCheckCatch(room.getBoard(), targetPiece, nextPos);

        // 1. 순수하게 윷놀이 이동 '결과 데이터'만 MoveResponse 상자에 담습니다.
        MoveResponse moveData = new MoveResponse(
                moveEvent,
                nextPos,
                nextPos == 99,
                room.getBoard().getPieces()
        );

        // 2. ApiResponse.ok()를 써서 "성공 상태 + 메시지 + 데이터"를 한 번에 포장해서 보냅니다.
        return ApiResponse.ok("complete to move", moveData);
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