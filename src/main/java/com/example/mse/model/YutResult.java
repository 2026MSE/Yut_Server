package com.example.mse.model;

import lombok.*;

@Getter
@Setter
public class YutResult {

    private YutName result;
    private int move;
    private boolean extraTurn;

    // 이 이동권이 어디서 생겼는지 구분
    // THROW: 일반 윷 던지기
    // CHANCE_CARD: 찬스카드로 추가된 이동권
    // CATCH_BONUS: 잡기 보너스로 추가된 이동권
    private String source;

    // 찬스카드로 생긴 이동권일 때 어떤 카드였는지 표시
    // 예: BONUS_DO, BONUS_GAE, BONUS_GEOL, BONUS_YUT, BONUS_MO
    private String sourceCard;
}