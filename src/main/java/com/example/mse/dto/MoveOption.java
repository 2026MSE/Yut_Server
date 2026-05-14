package com.example.mse.dto;

import com.example.mse.model.MoveType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveOption {

    private String pieceId;
    private int currentPosition;
    private int targetPosition;
    private boolean finished;
    private MoveType moveType;
    private boolean willCatch;
    private boolean willPiggyback;

    public MoveOption(
            String pieceId,
            int currentPosition,
            int targetPosition,
            boolean finished,
            MoveType moveType,
            boolean willCatch,
            boolean willPiggyback) {

        this.pieceId = pieceId;
        this.currentPosition = currentPosition;
        this.targetPosition = targetPosition;
        this.finished = finished;
        this.moveType = moveType;
        this.willCatch = willCatch;
        this.willPiggyback = willPiggyback;
    }
}