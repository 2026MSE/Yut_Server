package com.example.mse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.model.GameRoom;
import com.example.mse.model.TurnPhase;

@Service
public class GameFlowService {

    @Autowired
    private TurnService turnService;

    @Autowired
    private YutService yutService;

    public void startPrivateThrowPhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.PRIVATE_THROW);
    }

    public void startDeclarePhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.MAIN_HALL_DECLARE);
    }

    public void startChallengePhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.MAIN_HALL_CHALLENGE);
    }

    public void startMovePhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.YUT_MOVE);
    }

    public void finishMovePhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.YUT_MOVE_DONE);
    }

    public void endTurn(GameRoom room) {

        room.setTurnPhase(TurnPhase.TURN_END);

        turnService.nextTurn(room);
    }

    public void handleExtraTurn(GameRoom room) {

        yutService.resetTurn(room);

        room.setDeclaredPrivateSticks(new com.example.mse.model.StickSide[2]);
        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
        room.setTurnPhase(TurnPhase.PRIVATE_THROW);
    }

    public void handleMoveResult(GameRoom room, boolean extraTurn) {

        if (extraTurn) {
            handleExtraTurn(room);
        } else {
            finishMovePhase(room);
        }
    }
}