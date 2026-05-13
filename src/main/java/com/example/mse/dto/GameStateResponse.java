package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameStateResponse {

    private RoomInfo roomInfo;

    private TurnInfo turnInfo;

    private BoardStatusResponse boardStatus;

    private HallInfoResponse hallInfo;

    private List<PlayerInfo> players;
}