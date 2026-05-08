package com.example.mse.dto;

import java.util.List;

import com.example.mse.model.HallState;
import com.example.mse.model.StickSide;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HallInfoResponse {

    private HallState state;

    private StickSide[] publicSticks;
    private StickSide[] declaredPrivateSticks;

    private String firstChallenger;
    private List<String> queue;
}