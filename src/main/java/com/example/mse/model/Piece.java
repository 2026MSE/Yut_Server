package com.example.mse.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Piece {
    private String id;  //말의 고유
    private String ownerId;
    private int currentPosition; //현재 말판 위의 노드 위치(-1d은 대기석, 99는 완주)

    private List<Piece> carriedPieces = new ArrayList<>();

    public Piece(String id, String ownerId){
        this.id = id;
        this.ownerId = ownerId;
        this.currentPosition = -1;
    }

}
