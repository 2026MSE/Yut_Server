package com.example.mse.dto;

import java.util.List;

import com.example.mse.model.MoveType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveResultResponse {

    // 이동 이벤트 순번
    private long moveSequence;

    // 이 이동을 수행한 플레이어
    private String playerId;

    private String pieceId;

    private List<String> movedPieceIds;
    private List<String> caughtPieceIds;

    private int fromPosition;
    private int toPosition;

    private MoveType moveType;

    private boolean extraTurn;
    private boolean gameOver;

    private String winnerId;
}