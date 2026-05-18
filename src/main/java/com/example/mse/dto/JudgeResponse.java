package com.example.mse.dto;

import com.example.mse.model.StickSide;
import com.example.mse.model.YutResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JudgeResponse {

    // 챌린지 결과 enum
    public enum JudgeResult {
        CHALLENGE_SUCCESS,
        CHALLENGE_FAIL
    }

    private JudgeResult judgeResult;

    private String challengerId;
    private String turnPlayerId;
    
    private StickSide[] actualPrivateSticks;
    private StickSide[] declaredPrivateSticks;
    private StickSide[] publicSticks;

    private YutResult actualResult;

    // 챌린지 성공 시
    private boolean rewardChanceCard;

    // 챌린지 실패 시
    private boolean penaltyApplied;

    // 되돌아간 말 ID
    private String penaltyPieceId;
}