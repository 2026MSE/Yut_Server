package com.example.mse.dto;

import com.example.mse.model.Piece;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BoardStatusResponse {
    // 윷판 위의 모든 말들의 위치 정보만 담습니다.
    private Map<String, List<Piece>> allPieces;

    public BoardStatusResponse(Map<String, List<Piece>> allPieces) {
        this.allPieces = allPieces;
    }
}