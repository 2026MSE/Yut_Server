package com.example.mse.model;

public enum EffectType {

    // 패널티 계열
    ONE_PRIVATE_STICK,   // 다음 턴에 private stick 1개만 사용
    SKIP_TURN,           // 다음 턴 스킵
    MOVE_MINUS_ONE,      // 다음 이동 칸 수 -1

    // 보상/버프 계열
    EXTRA_THROW,         // 윷 한 번 더 던지기
    MOVE_PLUS_ONE,       // 다음 이동 칸 수 +1
    SHIELD,              // 패널티 1회 방어

    // 제한 계열
    NO_CHALLENGE,        // 다음 턴 챌린지 불가
    NO_CHANCE_CARD       // 다음 보상 획득 불가
}