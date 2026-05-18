package com.example.mse.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PieceInfo {

    private String pieceId;
    private String ownerId;
    private int currentPosition;

    private String carriedByPieceId;
    private List<String> carriedPieceIds;

    public PieceInfo(
            String pieceId,
            String ownerId,
            int currentPosition,
            String carriedByPieceId,
            List<String> carriedPieceIds) {

        this.pieceId = pieceId;
        this.ownerId = ownerId;
        this.currentPosition = currentPosition;
        this.carriedByPieceId = carriedByPieceId;
        this.carriedPieceIds = carriedPieceIds;
    }
}