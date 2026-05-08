// 26.05.08 찬미 /board/move 반환값 없애는 대신 보드 상태 정보를 여기에 추가
package com.example.mse.dto;

import com.example.mse.model.HallState;
import com.example.mse.model.Piece;
import com.example.mse.model.Scene;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BoardStatusResponse {

    private Map<String, List<Piece>> allPieces;

    private boolean extraTurn;
    private ThrowResponse throwResult;

    private String currentTurnPlayerId;
    private Scene currentRoom;

    private boolean alreadyThrown;
    private boolean alreadyMoved;

    private HallState hallState;

    public BoardStatusResponse() {
    }
}