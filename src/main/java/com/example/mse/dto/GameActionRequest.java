package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameActionRequest {

    private String roomId;
    private String playerId;
}