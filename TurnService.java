package com.example.mse;

import org.springframework.stereotype.Service;

@Service
public class TurnService {
    
    private TurnInfo turnInfo = new TurnInfo();

    public TurnService() {
        // 초기값 설정
        turnInfo.setCurrentTurnPlayerId("");
        turnInfo.setCurrentTurnPlayerRoom("MAIN_HALL");
    }

    public TurnInfo getTurnInfo(){
        return turnInfo;
    }

    public void setCurrentTurnPlayer(String playerId) {
        turnInfo.setCurrentTurnPlayerId(playerId);
    }

    public void moveCurrentTurnPlayerRoom(String room) {
        turnInfo.setCurrentTurnPlayerRoom(room);
    }

    public boolean isTurnPlayer(String playerId) {
        return turnInfo.getCurrentTurnPlayerId().equals(playerId);
    }
}
