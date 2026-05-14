package com.example.mse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.model.GameRoom;
import com.example.mse.model.Scene;
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
        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
        room.setChallengeResolved(false);
        room.setChallengeDeadlineMillis(System.currentTimeMillis() + 5000);
    }

    public void resolveChallengeTimeout(GameRoom room) {

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_CHALLENGE) {
            return;
        }

        if (room.isChallengeResolved()) {
            return;
        }

        if (System.currentTimeMillis() < room.getChallengeDeadlineMillis()) {
            return;
        }

        // 챌린지 없으면 바로 윷룸 이동
        if (room.getFirstChallengerId() == null) {
            room.setChallengeResolved(true);
            startMovePhase(room);
        }
    }

    public void startMovePhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.YUT_MOVE);

        room.getTurnInfo().setCurrentTurnPlayerRoom(Scene.YUT_ROOM);
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

        room.setChallengeResolved(false);
        room.setChallengeDeadlineMillis(0);

        room.setTurnPhase(TurnPhase.PRIVATE_THROW);
        room.getTurnInfo().setCurrentTurnPlayerRoom(Scene.PRIVATE_ROOM);
    }

    public void handleMoveResult(GameRoom room, boolean extraTurn) {

        if (extraTurn) {
            handleExtraTurn(room);
        } else {
            finishMovePhase(room);
        }
    }

    public void endGame(GameRoom room, String winnerId) {
        room.setWinnerId(winnerId);
        room.setTurnPhase(TurnPhase.GAME_OVER);
    }

}