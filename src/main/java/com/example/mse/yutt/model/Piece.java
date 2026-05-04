package com.example.yut_server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class Piece {

    private String playerId;
    private int pieceId;
    private int position;
    private boolean isFinished;

    public Piece(String playerId, int pieceId){
        this.playerId = playerId;
        this.pieceId = pieceId;
        this.position = 0;          //처음에는 무조건 0에서 출발
        this.isFinished = false; //아직 완주 안 함
    }

}
