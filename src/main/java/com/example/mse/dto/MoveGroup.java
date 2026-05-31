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

    // 이동권 출처 구분
    private String source; // THROW, CHANCE_CARD, CATCH_BONUS
    private String sourceCard; // BONUS_DO 등, 찬스카드일 때만 사용

    private List<MoveOption> movablePieces;

    public MoveGroup(
            int yutResultIndex,
            YutName yutName,
            int move,
            String source,
            String sourceCard,
            List<MoveOption> movablePieces) {

        this.yutResultIndex = yutResultIndex;
        this.yutName = yutName;
        this.move = move;
        this.source = source;
        this.sourceCard = sourceCard;
        this.movablePieces = movablePieces;
    }
}
