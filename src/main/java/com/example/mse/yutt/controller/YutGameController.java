package com.example.yut_server.controller;

import com.example.yut_server.model.*;
import com.example.yut_server.service.YutService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/yut")
@CrossOrigin(origins = "*")
public class YutGameController {

    private final YutService yutService;
    private Map<Integer, Tile> board = new HashMap<>();
    private Map<Integer, Piece> pieces = new HashMap<>();

    public YutGameController(YutService yutService) {
        this.yutService = yutService;
        initializeBoard();
        // 플레이어의 말 4개 미리 생성 (ID: 1, 2, 3, 4)
        for (int i = 1; i <= 4; i++) {
            pieces.put(i, new Piece("Player1", i));
        }
        // 테스트용 적군 말 배치 (7번 위치)
        Piece enemy = new Piece("Enemy", 99);
        enemy.setPosition(7);
        board.get(7).addPiece(enemy);
    }

    private void initializeBoard() {
        for (int i = 0; i <= 29; i++) {
            board.put(i, new Tile(i, "NORMAL", (i + 1) % 21, (i > 0 ? i - 1 : null), null));
        }
        board.get(5).setShortcut(21);
        board.get(10).setShortcut(26);
        board.get(20).setNext(0);
    }

    @PostMapping("/move")
    public MoveResult movePiece(@RequestBody Map<String, Object> request) {
        int pieceId = Integer.parseInt(request.get("pieceId").toString());
        int rollResult = Integer.parseInt(request.get("rollResult").toString());

        Piece targetPiece = pieces.get(pieceId);
        if (targetPiece == null) return null;

        // 기존 위치에서 말 제거
        if (targetPiece.getPosition() != 0) {
            board.get(targetPiece.getPosition()).removePiece(targetPiece);
        }

        MoveResult result = yutService.calculateMove(board, targetPiece, rollResult);

        // 결과에 따른 위치 업데이트
        if (!result.isFinished()) {
            int lastPos = result.getPath().get(result.getPath().size() - 1);
            targetPiece.setPosition(lastPos);
            board.get(lastPos).addPiece(targetPiece);
        } else {
            targetPiece.setFinished(true);
            targetPiece.setPosition(0);
        }

        return result;
    }
}