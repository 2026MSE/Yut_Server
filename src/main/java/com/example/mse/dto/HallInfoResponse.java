package com.example.mse.dto;

import java.util.List;

import com.example.mse.model.StickSide;
import com.example.mse.model.TurnPhase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HallInfoResponse {

    private TurnPhase turnPhase;

    private StickSide[] publicSticks;
    private StickSide[] declaredPrivateSticks;

    private String firstChallenger;
    private List<String> queue;
}