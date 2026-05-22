package com.example.mse.dto;

import com.example.mse.model.EffectType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlayerEffectInfo {

    private EffectType type;
    private String targetPlayerId;
    private String sourcePlayerId;
    private int remainingTurns;
    private int value;
}