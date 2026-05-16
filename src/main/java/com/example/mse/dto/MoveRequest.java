package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveRequest {

    private String roomId;
    private String playerId;
    private String pieceId;

    private int yutResultIndex;

    //영준 추가 5월7일
    //윷 결과(1~5)
    //클라이언트는 어떤 말을 움직일지만 결정. 칸수 계산은 서버가
    // private int moveAmount;
}