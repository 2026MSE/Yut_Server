package com.example.mse.dto;

import com.example.mse.model.StickSide;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeclareRequest {

    private String roomId;
    private String playerId;

    private StickSide s1;
    private StickSide s2;
}