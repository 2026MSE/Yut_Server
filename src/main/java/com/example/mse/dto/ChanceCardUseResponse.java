package com.example.mse.dto;

import com.example.mse.model.ChanceCard;
import com.example.mse.model.YutResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChanceCardUseResponse {

    private ChanceCard usedCard;

    private YutResult addedYutResult;

    private int addedYutResultIndex;
}