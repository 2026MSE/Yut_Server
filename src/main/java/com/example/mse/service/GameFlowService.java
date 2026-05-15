package com.example.mse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.JudgeResponse.JudgeResult;
import com.example.mse.model.GameLog;
import com.example.mse.model.GameRoom;
import com.example.mse.model.StickSide;
import com.example.mse.model.TurnPhase;

@Service
public class GameFlowService {

    @Autowired
    private TurnService turnService;

    @Autowired
    private YutService yutService;

    @Autowired
    private HallService hallService;

    @Autowired
    private BoardService boardService;

    public void addLog(GameRoom room, String type, String message) {
    room.getLogs().add(new GameLog(type, message));
}

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

        // 챌린지가 없으면 바로 이동 단계로 전환
        if (room.getFirstChallengerId() == null) {
            room.setChallengeResolved(true);
            startMovePhase(room);
            return;
        }

        // 챌린지가 있으면 서버가 자동 판정
        JudgeResult judgeResult = hallService.judgeChallenge(room);

        addLog(room, "JUDGE", "Challenge result: " + judgeResult);

        if (judgeResult == JudgeResult.CHALLENGE_FAIL) {
            // 챌린저가 틀렸으므로 챌린저의 말 하나를 시작점으로 되돌림
            boardService.sendFirstPieceToStart(
                    room.getBoard(),
                    room.getFirstChallengerId());
        }

        // CHALLENGE_SUCCESS인 경우는 이후 ChanceCard에서 보상 처리 예정
        room.setChallengeResolved(true);
        startMovePhase(room);
    }

    public void startMovePhase(GameRoom room) {
        room.setTurnPhase(TurnPhase.YUT_MOVE);
    }

    public void finishMovePhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.YUT_MOVE_DONE);
    }

    public void endTurn(GameRoom room) {
        room.setTurnPhase(TurnPhase.TURN_END);

        resetForNextTurn(room);

        turnService.nextTurn(room);

        room.setTurnPhase(TurnPhase.PRIVATE_THROW);
    }

    public void handleExtraTurn(GameRoom room) {
        resetForExtraTurn(room);

        room.setTurnPhase(TurnPhase.PRIVATE_THROW);
    }

    public void handleMoveResult(GameRoom room, boolean extraTurn) {

        if (extraTurn) {
            handleExtraTurn(room);
        } else {
            finishMovePhase(room);
        }
    }

    public void endGame(GameRoom room, String winnerId) {
        // 승리자 저장
        room.setWinnerId(winnerId);

        // 게임 종료 상태
        room.setTurnPhase(TurnPhase.GAME_OVER);

        // 같은 방에서 다시 시작할 수 있도록 게임 진행 상태 해제
        room.setStarted(false);

        // 턴 정보 초기화
        room.getTurnInfo().setCurrentTurnPlayerId(null);
        room.getTurnInfo().setCurrentTurnIndex(0);
        room.getTurnInfo().getTurnOrder().clear();

        addLog(room, "GAME_OVER", "Winner: " + winnerId);
    }

    private void resetThrowState(GameRoom room) {
        yutService.resetTurn(room);
        room.setDeclaredPrivateSticks(new StickSide[2]);
    }

    private void resetChallengeState(GameRoom room) {
        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
        room.setChallengeResolved(false);
        room.setChallengeDeadlineMillis(0);
    }

    public void resetForNextTurn(GameRoom room) {
        resetThrowState(room);
        resetChallengeState(room);
    }

    public void resetForExtraTurn(GameRoom room) {
        resetThrowState(room);
        resetChallengeState(room);
    }
}