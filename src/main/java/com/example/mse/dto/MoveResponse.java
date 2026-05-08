package com.example.mse.dto;

import com.example.mse.model.MoveType;
import com.example.mse.model.Piece;
import com.example.mse.model.YutResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MoveResponse {

    // 이동 결과
    private MoveType event;
    private MoveType moveType;

    // 이동 위치
    private int arrivalNode;
    private int from;
    private int to;

    // 말 정보
    private String pieceId;

    // 완주 여부
    private boolean isFinished;

    // 추가 턴 여부
    private boolean extraTurn;

    // 다음 행동 안내
    private String nextAction;

    // 윷 결과
    private YutResult yutResult;

    // 전체 보드 상태
    private Map<String, List<Piece>> allPieces;

    public MoveResponse() {
    }

    public MoveResponse(
            MoveType event,
            int arrivalNode,
            boolean isFinished,
            Map<String, List<Piece>> allPieces) {
        this.event = event;
        this.moveType = event;
        this.arrivalNode = arrivalNode;
        this.to = arrivalNode;
        this.isFinished = isFinished;
        this.allPieces = allPieces;
    }
}