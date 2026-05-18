package com.example.mse.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerEffect {

    // 효과 종류
    private EffectType type;

    // 효과를 받는 플레이어
    private String targetPlayerId;

    // 효과를 발생시킨 플레이어
    // 예: 챌린지 실패 패널티라면 source = 턴 플레이어
    // 시스템 효과라면 null 가능
    private String sourcePlayerId;

    // 몇 번 적용될 수 있는지
    // 예: 다음 턴 1회 적용이면 1
    private int remainingTurns;

    // 수치형 효과에 사용
    // 예: MOVE_PLUS_ONE이면 1, MOVE_MINUS_ONE이면 -1
    // ONE_PRIVATE_STICK처럼 수치가 필요 없는 효과는 0
    private int value;

    public PlayerEffect(
            EffectType type,
            String targetPlayerId,
            String sourcePlayerId,
            int remainingTurns,
            int value) {

        this.type = type;
        this.targetPlayerId = targetPlayerId;
        this.sourcePlayerId = sourcePlayerId;
        this.remainingTurns = remainingTurns;
        this.value = value;
    }
}