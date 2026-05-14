package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.example.mse.model.StickSide;

@Getter
@Setter
public class GameStateResponse {

    private RoomInfo roomInfo;
    private TurnInfo turnInfo;
    private BoardStatusResponse boardStatus;
    private List<PlayerInfo> players;

    private StickSide[] publicSticks;
    private StickSide[] declaredPrivateSticks;
    private String firstChallenger;
    private List<String> challengeQueue;

    private long challengeDeadlineMillis;
    private long serverTimeMillis;

    private String winnerId;
}