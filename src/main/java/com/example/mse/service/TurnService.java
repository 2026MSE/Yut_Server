package com.example.mse.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mse.model.GameRoom;
import com.example.mse.model.TurnInfo;
import com.example.mse.model.TurnPhase;

@Service
public class TurnService {

    public void startGame(GameRoom room) {
        List<String> order = new ArrayList<>(room.getPlayerIds());

        if (order.isEmpty()) {
            throw new RuntimeException("No players in room");
        }

        Collections.shuffle(order);

        TurnInfo turnInfo = room.getTurnInfo();

        turnInfo.setTurnOrder(order);
        turnInfo.setCurrentTurnIndex(0);
        turnInfo.setCurrentTurnPlayerId(order.get(0));

        // 게임 진행 판단은 TurnPhase를 기준으로 한다.
        room.setTurnPhase(TurnPhase.PRIVATE_THROW);
    }

    public void nextTurn(GameRoom room) {
        TurnInfo turnInfo = room.getTurnInfo();
        List<String> order = turnInfo.getTurnOrder();

        if (order == null || order.isEmpty()) {
            throw new RuntimeException("Turn order is empty");
        }

        int nextIndex = (turnInfo.getCurrentTurnIndex() + 1) % order.size();

        turnInfo.setCurrentTurnIndex(nextIndex);
        turnInfo.setCurrentTurnPlayerId(order.get(nextIndex));
    }

    public boolean isTurnPlayer(GameRoom room, String playerId) {
        if (playerId == null) {
            return false;
        }

        String currentTurnPlayerId = room.getTurnInfo().getCurrentTurnPlayerId();

        return currentTurnPlayerId != null
                && currentTurnPlayerId.equals(playerId);
    }
}