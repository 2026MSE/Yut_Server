package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

import com.example.mse.model.GameLog;
import com.example.mse.model.StickSide;
import com.example.mse.model.YutResult;

@Getter
@Setter
public class GameStateResponse {

    private List<GameLog> logs;

    private RoomInfo roomInfo;
    private TurnInfo turnInfo;
    private BoardStatusResponse boardStatus;
    private List<PlayerInfo> players;

    private StickSide[] privateSticks;
    private StickSide[] publicSticks;
    private StickSide[] declaredPrivateSticks;
    private String firstChallenger;
    private List<String> challengeQueue;
    private Map<String, Boolean> challengeVotes;

    private YutResult currentYutResult;
    private List<YutResult> pendingYutResults;

    private JudgeResponse lastJudgeResponse;

    private long challengeDeadlineMillis;
    private long serverTimeMillis;

    private String winnerId;
}