package com.example.mse.dto;

import com.example.mse.model.MoveType;
import com.example.mse.model.Piece;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MoveResponse {
    // status와 message는 ApiResponse에서 관리하므로 삭제합니다.
    private MoveType event;     // 이동 결과 (NORMAL, CATCH, PIGGYBACK, FINISH)
    private int arrivalNode;    // 도착한 노드 위치
    private boolean isFinished; // 완주 여부
    private Map<String, List<Piece>> allPieces; // 최신 윷판 전체 상태

    public MoveResponse(MoveType event, int arrivalNode, boolean isFinished, Map<String, List<Piece>> allPieces) {
        this.event = event;
        this.arrivalNode = arrivalNode;
        this.isFinished = isFinished;
        this.allPieces = allPieces;
    }
}