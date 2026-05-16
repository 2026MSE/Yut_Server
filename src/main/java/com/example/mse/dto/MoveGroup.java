package com.example.mse.dto;

import java.util.List;

import com.example.mse.model.YutName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveGroup {

    private int yutResultIndex;
    private YutName yutName;
    private int move;
    private List<MoveOption> movablePieces;

    public MoveGroup(
            int yutResultIndex,
            YutName yutName,
            int move,
            List<MoveOption> movablePieces) {

        this.yutResultIndex = yutResultIndex;
        this.yutName = yutName;
        this.move = move;
        this.movablePieces = movablePieces;
    }
}