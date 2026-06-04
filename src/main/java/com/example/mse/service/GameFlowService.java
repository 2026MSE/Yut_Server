package com.example.mse.service;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.JudgeResponse;
import com.example.mse.dto.JudgeResponse.JudgeResult;
import com.example.mse.dto.MoveResultResponse;
import com.example.mse.model.ChanceCard;
import com.example.mse.model.EffectType;
import com.example.mse.model.GameLog;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Piece;
import com.example.mse.model.StickSide;
import com.example.mse.model.TurnPhase;
import com.example.mse.model.YutName;
import com.example.mse.model.YutResult;

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

    @Autowired
    private EffectService effectService;

    @Autowired
    private PlayerService playerService;

    public void addLog(GameRoom room, String type, String message) {
        room.getLogs().add(new GameLog(type, message));
    }

    public void recordMoveResult(GameRoom room, MoveResultResponse response, String playerId) {
        room.setMoveSequence(room.getMoveSequence() + 1);

        response.setMoveSequence(room.getMoveSequence());
        response.setPlayerId(playerId);

        room.getMoveHistory().add(response);
    }

    public void startPrivateThrowResultPhase(GameRoom room) {
        room.setTurnPhase(TurnPhase.PRIVATE_THROW_RESULT);
    }

    public void startCatchBonusThrowResultPhase(GameRoom room) {
        room.setTurnPhase(TurnPhase.CATCH_BONUS_THROW_RESULT);
    }

    public void exitPrivateThrowResult(GameRoom room) {
        if (room.getTurnPhase() != TurnPhase.PRIVATE_THROW_RESULT) {
            throw new RuntimeException("Not in PRIVATE_THROW_RESULT phase.");
        }

        if (room.getCurrentYutResult() == null) {
            throw new RuntimeException("No yut result to confirm.");
        }

        startDeclarePhase(room);
    }

    public void exitCatchBonusThrowResult(GameRoom room) {
        if (room.getTurnPhase() != TurnPhase.CATCH_BONUS_THROW_RESULT) {
            throw new RuntimeException("Not in CATCH_BONUS_THROW_RESULT phase.");
        }

        if (room.getCurrentYutResult() == null) {
            throw new RuntimeException("No catch bonus yut result to confirm.");
        }

        resolveCatchBonusThrow(room);
    }

    public void startDeclarePhase(GameRoom room) {

        room.setTurnPhase(TurnPhase.MAIN_HALL_DECLARE);
    }

    public void startChallengePhase(GameRoom room) {
        room.setTurnPhase(TurnPhase.MAIN_HALL_CHALLENGE);

        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
        room.getChallengeVotes().clear();

        room.setLastJudgeResponse(null);

        room.setChallengeResolved(false);
        room.setChallengeDeadlineMillis(System.currentTimeMillis() + 60000);
    }

    private boolean allNonTurnPlayersVotedX(GameRoom room) {
        String turnPlayerId = room.getTurnInfo().getCurrentTurnPlayerId();

        for (String playerId : room.getPlayerIds()) {
            if (playerId.equals(turnPlayerId)) {
                continue;
            }

            Boolean vote = room.getChallengeVotes().get(playerId);

            if (vote == null || vote) {
                return false;
            }
        }

        return true;
    }

    public void resolveChallengeIfReady(GameRoom room) {

        if (room.getTurnPhase() != TurnPhase.MAIN_HALL_CHALLENGE) {
            return;
        }

        if (room.isChallengeResolved()) {
            return;
        }

        boolean timeOver = System.currentTimeMillis() >= room.getChallengeDeadlineMillis();

        boolean challengerExists = room.getFirstChallengerId() != null;

        boolean allVotedX = allNonTurnPlayersVotedX(room);

        // O도 없고, 전원 X도 아니고, 시간도 안 지났으면 계속 대기
        if (!challengerExists && !allVotedX && !timeOver) {
            return;
        }

        // 챌린저가 없으면 선언 인정
        if (!challengerExists) {
            room.setLastJudgeResponse(null);
            room.setChallengeResolved(true);
            addLog(room, "JUDGE", "No challenge. Result accepted.");
            room.setTurnPhase(TurnPhase.CHALLENGE_RESULT);
            return;
        }

        JudgeResult judgeResult = hallService.judgeChallenge(room);

        JudgeResponse response = new JudgeResponse();
        response.setJudgeResult(judgeResult);
        response.setActualPrivateSticks(room.getPrivateSticks());
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());
        response.setPublicSticks(room.getPublicSticks());
        response.setActualResult(room.getCurrentYutResult());
        response.setChallengerId(room.getFirstChallengerId());
        response.setTurnPlayerId(room.getTurnInfo().getCurrentTurnPlayerId());

        addLog(room, "JUDGE", "Challenge result: " + judgeResult);

        if (judgeResult == JudgeResult.CHALLENGE_SUCCESS) {
            ChanceCard rewardCard = playerService.giveRandomChanceCard(
                    room.getFirstChallengerId());

            response.setRewardChanceCard(rewardCard != null);

            if (rewardCard != null) {
                response.setRewardCard(rewardCard.name());

                addLog(
                        room,
                        "REWARD",
                        room.getFirstChallengerId()
                                + " received chance card: "
                                + rewardCard.name());
            } else {
                response.setRewardCard(null);
            }

            response.setPenaltyApplied(false);
            response.setPenaltyType(null);
            response.setPenaltyPieceId(null);

        } else {
            Piece penaltyPiece = boardService.sendRandomPieceToWaitingArea(
                    room.getBoard(),
                    room.getFirstChallengerId());

            response.setRewardChanceCard(false);

            if (penaltyPiece != null) {
                response.setPenaltyApplied(true);
                response.setPenaltyType("SEND_PIECE_TO_WAITING_AREA");
                response.setPenaltyPieceId(penaltyPiece.getId());

                addLog(
                        room,
                        "PENALTY",
                        room.getFirstChallengerId()
                                + "'s piece was sent back to waiting area.");

            } else {
                effectService.addEffect(
                        room,
                        EffectType.ONE_PRIVATE_STICK,
                        room.getFirstChallengerId(),
                        room.getTurnInfo().getCurrentTurnPlayerId(),
                        1,
                        0);

                response.setPenaltyApplied(true);
                response.setPenaltyType("ONE_PRIVATE_STICK");
                response.setPenaltyPieceId(null);

                addLog(
                        room,
                        "PENALTY",
                        room.getFirstChallengerId()
                                + " received ONE_PRIVATE_STICK penalty.");
            }
        }

        room.setLastJudgeResponse(response);
        room.setChallengeResolved(true);
        room.setTurnPhase(TurnPhase.CHALLENGE_RESULT);
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

    public void addCurrentResultToPending(GameRoom room) {
        if (room.getCurrentYutResult() == null) {
            return;
        }

        room.getPendingYutResults().add(room.getCurrentYutResult());
    }

    public boolean shouldContinueThrow(GameRoom room) {
        if (room.getCurrentYutResult() == null) {
            return false;
        }

        return room.getCurrentYutResult().isExtraTurn();
    }

    public void proceedAfterThrowResolved(GameRoom room) {
        addCurrentResultToPending(room);

        room.setChallengeResolved(true);

        if (shouldContinueThrow(room)) {
            resetThrowState(room);
            resetChallengeState(room);
            room.setTurnPhase(TurnPhase.PRIVATE_THROW);
        } else {
            removeUnmovableBackDoResults(room);

            if (room.getPendingYutResults().isEmpty()) {
                finishMovePhase(room);
            } else {
                startMovePhase(room);
            }
        }
    }

    public void handleMoveResult(GameRoom room) {

        if (room.getPendingYutResults().isEmpty()) {
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

        // 게임 종료 시 해당 방 플레이어들의 찬스카드 인벤토리 초기화
        playerService.clearInventories(room.getPlayerIds());

        // 턴 정보 초기화
        room.getTurnInfo().setCurrentTurnPlayerId(null);
        room.getTurnInfo().setCurrentTurnIndex(0);
        room.getTurnInfo().getTurnOrder().clear();

        addLog(room, "GAME_OVER", "Winner: " + winnerId);
    }

    private void resetThrowState(GameRoom room) {
        yutService.resetTurn(room);
        room.setDeclaredPrivateSticks(null);
    }

    private void resetChallengeState(GameRoom room) {
        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
        room.getChallengeVotes().clear();
        room.setChallengeResolved(false);
        room.setChallengeDeadlineMillis(0);
    }

    public void resetForNextTurn(GameRoom room) {
        resetThrowState(room);
        resetChallengeState(room);
    }

    private void removeUnmovableBackDoResults(GameRoom room) {
        String currentPlayerId = room.getTurnInfo().getCurrentTurnPlayerId();

        if (currentPlayerId == null) {
            return;
        }

        List<Piece> pieces = room.getBoard().getPieces().get(currentPlayerId);

        if (pieces == null || pieces.isEmpty()) {
            return;
        }

        Iterator<YutResult> iterator = room.getPendingYutResults().iterator();

        while (iterator.hasNext()) {
            YutResult result = iterator.next();

            if (result.getResult() != YutName.BACK_DO) {
                continue;
            }

            if (!canMoveBackDo(room, pieces)) {
                iterator.remove();

                addLog(
                        room,
                        "AUTO_SKIP",
                        "BACK_DO was skipped because no piece can move backward.");
            }
        }
    }

    private boolean canMoveBackDo(GameRoom room, List<Piece> pieces) {
        for (Piece piece : pieces) {
            if (piece.getCurrentPosition() == 99) {
                continue;
            }

            if (piece.getCarriedByPieceId() != null) {
                continue;
            }

            int currentPosition = piece.getCurrentPosition();

            // 대기석(-1)과 시작점(0)에서는 BACK_DO로 이동 불가
            if (currentPosition == -1 || currentPosition == 0) {
                continue;
            }

            int targetPosition = boardService.calculateNextPath(
                    room.getBoard(),
                    currentPosition,
                    -1);

            if (targetPosition != currentPosition) {
                return true;
            }
        }

        return false;
    }

    public void startCatchBonusThrow(GameRoom room) {
        resetThrowState(room);
        room.setTurnPhase(TurnPhase.CATCH_BONUS_THROW);
    }

    public void resolveCatchBonusThrow(GameRoom room) {
        addCurrentResultToPending(room);
        room.setTurnPhase(TurnPhase.YUT_MOVE);
    }

    public void continueAfterChallengeResult(GameRoom room) {
        if (room.getTurnPhase() != TurnPhase.CHALLENGE_RESULT) {
            throw new RuntimeException("Not in challenge result phase.");
        }

        applyAcceptedDeclaredResult(room);

        proceedAfterThrowResolved(room);
    }

    private void applyAcceptedDeclaredResult(GameRoom room) {
        if (room.getDeclaredPrivateSticks() == null) {
            return;
        }

        // 챌린지 성공 = 거짓 선언이 들킴
        // 이 경우 선언 결과를 적용하면 안 되고, 실제 currentYutResult를 유지해야 함
        if (room.getLastJudgeResponse() != null
                && room.getLastJudgeResponse().getJudgeResult() == JudgeResult.CHALLENGE_SUCCESS) {
            return;
        }

        StickSide[] declaredPrivate = room.getDeclaredPrivateSticks();
        StickSide[] publicSticks = room.getPublicSticks();

        StickSide[] acceptedSticks = new StickSide[declaredPrivate.length + publicSticks.length];

        int index = 0;

        for (StickSide stick : declaredPrivate) {
            acceptedSticks[index++] = stick;
        }

        for (StickSide stick : publicSticks) {
            acceptedSticks[index++] = stick;
        }

        YutResult acceptedResult = yutService.calculateResultFromSticks(acceptedSticks);
        acceptedResult.setSource("THROW");
        acceptedResult.setSourceCard(null);

        room.setCurrentYutResult(acceptedResult);
    }
}