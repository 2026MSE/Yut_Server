package com.example.mse.dto;

import com.example.mse.model.HallState;
import com.example.mse.model.StickSide;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeclareResponse {

    private String message;

    private StickSide[] declaredPrivateSticks;
    private StickSide[] publicSticks;

    private HallState state;
}