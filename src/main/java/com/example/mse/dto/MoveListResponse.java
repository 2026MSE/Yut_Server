package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MoveListResponse {

    private List<MoveOption> movablePieces;

    public MoveListResponse(List<MoveOption> movablePieces) {
        this.movablePieces = movablePieces;
    }
}