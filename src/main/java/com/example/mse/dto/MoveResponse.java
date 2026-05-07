package com.example.mse.dto;

import com.example.mse.model.MoveType;
import com.example.mse.model.YutResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveResponse {

    private String pieceId;

    private int from;
    private int to;

    private MoveType moveType;

    private YutResult yutResult;

    private boolean extraTurn;

    private String nextAction;
}