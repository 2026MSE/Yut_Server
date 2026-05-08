package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveOption {

    private String pieceId;
    private int currentPosition;
    private int targetPosition;
    private boolean finished;

    public MoveOption(String pieceId, int currentPosition, int targetPosition, boolean finished) {
        this.pieceId = pieceId;
        this.currentPosition = currentPosition;
        this.targetPosition = targetPosition;
        this.finished = finished;
    }
}