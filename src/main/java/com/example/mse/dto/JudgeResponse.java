package com.example.mse.dto;

import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;
import com.example.mse.model.YutResult;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JudgeResponse {

    private String judgeResult;

    private StickSide[] actualPrivateSticks;
    private StickSide[] declaredPrivateSticks;
    private StickSide[] publicSticks;

    private YutResult actualResult;

    private Scene nextRoom;
}