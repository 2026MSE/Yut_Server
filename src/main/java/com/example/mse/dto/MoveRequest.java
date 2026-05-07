package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveRequest {

    private String roomId;
    private String playerId;
    private String pieceId;
}