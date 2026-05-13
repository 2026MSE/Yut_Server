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

    // 방장 아이디
    private String hostId;

    private boolean started = false;
    // 턴 정보
    private TurnInfo turnInfo = new TurnInfo();
    // 턴 단계 대기
    private TurnPhase turnPhase = TurnPhase.WAITING;
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
    //찬미 한 턴에 한 번만 이동하도록 체크
    private boolean alreadyMoved = false;
}