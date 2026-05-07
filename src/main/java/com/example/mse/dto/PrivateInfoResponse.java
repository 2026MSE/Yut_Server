package com.example.mse.dto;

import com.example.mse.model.StickSide;
import com.example.mse.model.YutResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrivateInfoResponse {

    private StickSide[] sticks;

    private StickSide[] privateSticks;
    private StickSide[] publicSticks;

    private YutResult result;
}