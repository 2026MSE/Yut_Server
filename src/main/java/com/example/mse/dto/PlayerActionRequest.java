package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerActionRequest {

    private String roomId;
    private String playerId;
}