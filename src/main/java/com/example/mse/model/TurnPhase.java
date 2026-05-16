package com.example.mse.model;

public enum TurnPhase {
    WAITING, // 방 대기 중
    PRIVATE_THROW, // 턴 플레이어가 Private Room에서 윷 던지는 단계
    MAIN_HALL_DECLARE, // Main Hall에서 결과를 선언하는 단계
    MAIN_HALL_CHALLENGE, // 다른 플레이어들이 챌린지할 수 있는 단계
    CATCH_BONUS_THROW,
    YUT_MOVE, // 윷판에서 말을 이동하는 단계
    YUT_MOVE_DONE, // 말을 이동한 후 추가 행동이 필요한지 판단하는 단계
    TURN_END, // 턴 종료 처리 단계
    GAME_OVER // 게임 종료
}