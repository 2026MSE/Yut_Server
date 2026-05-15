package com.example.mse.dto;

import com.example.mse.model.MoveType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveResultResponse {

    private String pieceId;

    private int fromPosition;
    private int toPosition;

    private MoveType moveType;

    private boolean extraTurn;
    private boolean gameOver;

    private String winnerId;
}