package com.example.mse.model;

import java.util.ArrayList;
import java.util.List;

import com.example.mse.dto.TurnInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

// 찬미 내부 상태 보관용으로 변경
public class GameRoom {

    private String roomId;
    
    //방에 들어온 플레이어 아이디 목록
    private List<String> playerIds = new ArrayList<>();

    private boolean started = false;
    // 턴 정보
    private TurnInfo turnInfo = new TurnInfo();
    // 홀 상태
    private HallState hallState = HallState.DECLARE;
    // 현재 윷 결과
    private YutResult currentYutResult;
    private boolean alreadyThrown = false;

    //윷 정보
    private StickSide[] sticks = new StickSide[4];
    private StickSide[] privateSticks = new StickSide[2];
    private StickSide[] publicSticks = new StickSide[2];
    private StickSide[] declaredPrivateSticks = new StickSide[2];

    // 챌린지 정보
    private String firstChallengerId;
    private List<String> challengeQueue = new ArrayList<>();

    // 영준 추가
    private Board board = new Board();
}