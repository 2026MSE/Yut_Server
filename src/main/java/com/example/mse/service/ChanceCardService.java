package com.example.mse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.ChanceCardUseResponse;
import com.example.mse.model.ChanceCard;
import com.example.mse.model.GameRoom;
import com.example.mse.model.TurnPhase;
import com.example.mse.model.YutName;
import com.example.mse.model.YutResult;

@Service
public class ChanceCardService {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private GameFlowService gameFlowService;

    public ChanceCardUseResponse useChanceCard(GameRoom room, String playerId, ChanceCard card) {

        if (!turnService.isTurnPlayer(room, playerId)) {
            throw new RuntimeException("Not your turn.");
        }

        if (room.getTurnPhase() != TurnPhase.YUT_MOVE) {
            throw new RuntimeException("Chance card can only be used in YUT_MOVE phase.");
        }

        if (card == null) {
            throw new RuntimeException("Chance card is required.");
        }

        if (!playerService.hasChanceCard(playerId, card)) {
            throw new RuntimeException("You do not have this chance card.");
        }

        YutResult bonusResult = createBonusYutResult(card);

        room.getPendingYutResults().add(bonusResult);

        int addedIndex = room.getPendingYutResults().size() - 1;

        playerService.removeChanceCard(playerId, card);

        gameFlowService.addLog(
                room,
                "CHANCE_CARD",
                playerId + " used chance card: " + card.name());

        ChanceCardUseResponse response = new ChanceCardUseResponse();
        response.setUsedCard(card);
        response.setAddedYutResult(bonusResult);
        response.setAddedYutResultIndex(addedIndex);

        return response;
    }

    private YutResult createBonusYutResult(ChanceCard card) {
        YutResult result = new YutResult();

        result.setExtraTurn(false);
        result.setSource("CHANCE_CARD");
        result.setSourceCard(card.name());

        switch (card) {
            case BONUS_DO:
                result.setResult(YutName.DO);
                result.setMove(1);
                break;

            case BONUS_GAE:
                result.setResult(YutName.GAE);
                result.setMove(2);
                break;

            case BONUS_GEOL:
                result.setResult(YutName.GEOL);
                result.setMove(3);
                break;

            case BONUS_YUT:
                result.setResult(YutName.YUT);
                result.setMove(4);
                break;

            case BONUS_MO:
                result.setResult(YutName.MO);
                result.setMove(5);
                break;

            default:
                throw new RuntimeException("Unsupported chance card.");
        }

        return result;
    }
}