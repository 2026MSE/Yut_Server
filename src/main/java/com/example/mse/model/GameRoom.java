package com.example.mse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.mse.dto.JudgeResponse;
import com.example.mse.dto.TurnInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

// 찬미 내부 상태 보관용으로 변경
public class GameRoom {

    private String roomId;

    // 방에 들어온 플레이어 아이디 목록
    private List<String> playerIds = new ArrayList<>();

    // 방장 아이디
    private String hostId;

    // 게임로그 리스트
    private List<GameLog> logs = new ArrayList<>();

    private boolean started = false;
    // 턴 정보
    private TurnInfo turnInfo = new TurnInfo();
    // 턴 단계 대기
    private TurnPhase turnPhase = TurnPhase.WAITING;

    // 현재 윷 결과
    private YutResult currentYutResult;

    // 윷 정보
    private StickSide[] sticks = new StickSide[4];

    public StickSide[] getPrivateSticks() {
        return new StickSide[] {
                sticks[0],
                sticks[1]
        };
    }

    public StickSide[] getPublicSticks() {
        return new StickSide[] {
                sticks[2],
                sticks[3]
        };
    }

    private StickSide[] declaredPrivateSticks;

    // 챌린지 정보
    private String firstChallengerId;
    private List<String> challengeQueue = new ArrayList<>();
    private Map<String, Boolean> challengeVotes = new HashMap<>();

    // 윷 결과 누적(실제 이동 가능 결과)
    private List<YutResult> pendingYutResults = new ArrayList<>();

    // 영준 추가
    private Board board = new Board();

    private String winnerId;

    private JudgeResponse lastJudgeResponse;

    private long challengeDeadlineMillis;
    private boolean challengeResolved = false;
}