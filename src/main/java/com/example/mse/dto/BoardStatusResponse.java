package com.example.mse.dto;

import com.example.mse.model.Piece;
import com.example.mse.model.TurnPhase;
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

    private TurnPhase turnPhase;

    public BoardStatusResponse() {
    }
}