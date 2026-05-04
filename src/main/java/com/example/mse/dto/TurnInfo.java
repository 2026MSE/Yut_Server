package com.example.mse.dto;

import java.util.*;

import com.example.mse.model.Scene;

import lombok.*;

@Getter
@Setter

public class TurnInfo {

private String currentTurnPlayerId;
    private Scene currentTurnPlayerRoom;

    private List<String> turnOrder = new ArrayList<>();
    private int currentTurnIndex;
    
}
