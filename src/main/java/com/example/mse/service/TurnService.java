package com.example.mse.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mse.dto.TurnInfo;
import com.example.mse.model.GameRoom;
import com.example.mse.model.HallState;
import com.example.mse.model.Scene;
import com.example.mse.model.StickSide;

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
        turnInfo.setCurrentTurnPlayerRoom(Scene.PRIVATE_ROOM);
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
        turnInfo.setCurrentTurnPlayerRoom(Scene.PRIVATE_ROOM);

        room.setAlreadyThrown(false);
        room.setCurrentYutResult(null);
        room.setHallState(HallState.DECLARE);
        //찬미 declare 윷 초기화 방식 수정
        room.setDeclaredPrivateSticks(new StickSide[2]);
        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
    }

    public boolean isTurnPlayer(GameRoom room, String playerId) {
        return room.getTurnInfo().getCurrentTurnPlayerId().equals(playerId);
    }

    public void moveCurrentTurnPlayerRoom(GameRoom room, Scene scene) {
        room.getTurnInfo().setCurrentTurnPlayerRoom(scene);
    }
}