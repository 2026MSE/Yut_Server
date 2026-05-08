package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveRequest {

    private String roomId;
    private String playerId;
    private String pieceId;

    //영준 추가 5월7일
    //윷 결과(1~5)
    private int moveAmount;
}