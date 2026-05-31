package com.example.mse.dto;

import com.example.mse.model.ChanceCard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChanceCardUseRequest {

    private String roomId;
    private String playerId;

    private ChanceCard card;
}