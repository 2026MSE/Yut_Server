package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BoardStatusResponse {

    private Map<String, List<PieceInfo>> allPieces;

    public BoardStatusResponse() {
    }
}