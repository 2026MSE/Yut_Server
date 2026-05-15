package com.example.mse.dto;

import java.util.*;
import lombok.*;

@Getter
@Setter

public class TurnInfo {

private String currentTurnPlayerId;

    private List<String> turnOrder = new ArrayList<>();
    private int currentTurnIndex;
    
}
