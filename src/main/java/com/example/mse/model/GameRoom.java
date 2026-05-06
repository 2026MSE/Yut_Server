package com.example.mse.model;

import java.util.List;

import com.example.mse.dto.TurnInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

// 찬미 내부 상태 보관용으로 변경
public class GameRoom {
    private String roomId;
    private List<String> playerIds;
    private boolean started;

    private TurnInfo turnInfo;
    private HallState hallState;

    private YutResult currentYutResult;
    private boolean alreadyThrown;

    private StickSide[] sticks;
    private StickSide[] privateSticks;
    private StickSide[] publicSticks;
    private StickSide[] declaredPrivateSticks;

    private String firstChallengerId;
    private List<String> challengeQueue;

    // 영준 추가
    private Board board;
}