package com.example.mse.model;

import java.util.ArrayList;
import java.util.List;

import com.example.mse.dto.TurnInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class GameRoom {

    private String roomId;
    private List<String> playerIds = new ArrayList<>();
    private boolean started = false;

    private TurnInfo turnInfo = new TurnInfo();

    private HallState hallState = HallState.DECLARE;

    private YutResult currentYutResult;
    private boolean alreadyThrown = false;

    private StickSide[] sticks = new StickSide[4];
    private StickSide[] privateSticks = new StickSide[2];
    private StickSide[] publicSticks = new StickSide[2];

    private StickSide[] declaredPrivateSticks = new StickSide[2];

    private String firstChallengerId;
    private List<String> challengeQueue = new ArrayList<>();

    //영준 추가함
    private Board board = new Board();
}