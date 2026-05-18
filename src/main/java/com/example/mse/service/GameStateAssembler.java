package com.example.mse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.TurnPhase;

@Service
public class GameStateAssembler {

    @Autowired
    private RoomService roomService;

    @Autowired
    private PlayerService playerService;

    public GameStateResponse build(GameRoom room, String playerId) {

        GameStateResponse response = new GameStateResponse();

        response.setLogs(room.getLogs());

        response.setRoomInfo(roomService.toRoomInfo(room));
        response.setTurnInfo(room.getTurnInfo());
        response.setPlayers(playerService.getPlayerInfoByIds(room.getPlayerIds()));

        BoardStatusResponse boardStatus = new BoardStatusResponse();

        boardStatus.setAllPieces(room.getBoard().getPieces());

        boardStatus.setCurrentTurnPlayerId(
                room.getTurnInfo().getCurrentTurnPlayerId());

        boardStatus.setTurnPhase(room.getTurnPhase());

        response.setBoardStatus(boardStatus);

        boolean isTurnPlayer = room.getTurnInfo().getCurrentTurnPlayerId() != null
                && room.getTurnInfo().getCurrentTurnPlayerId().equals(playerId);

        boolean hasYutResult = room.getCurrentYutResult() != null;

        boolean isResultPublic = room.isChallengeResolved()
                || room.getTurnPhase() == TurnPhase.YUT_MOVE
                || room.getTurnPhase() == TurnPhase.YUT_MOVE_DONE
                || room.getTurnPhase() == TurnPhase.TURN_END
                || room.getTurnPhase() == TurnPhase.GAME_OVER
                || room.getTurnPhase() == TurnPhase.CATCH_BONUS_THROW;

        if (hasYutResult && (isTurnPlayer || isResultPublic)) {
            response.setCurrentYutResult(room.getCurrentYutResult());
        } else {
            response.setCurrentYutResult(null);
        }

        if (hasYutResult) {
            response.setPublicSticks(room.getPublicSticks());
        } else {
            response.setPublicSticks(null);
        }

        if (hasYutResult && isTurnPlayer) {
            response.setPrivateSticks(room.getPrivateSticks());
        } else {
            response.setPrivateSticks(null);
        }

        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());

        response.setChallengeDeadlineMillis(room.getChallengeDeadlineMillis());
        response.setServerTimeMillis(System.currentTimeMillis());

        response.setFirstChallenger(room.getFirstChallengerId());
        response.setChallengeQueue(room.getChallengeQueue());
        response.setChallengeVotes(room.getChallengeVotes());

        boolean isPendingResultPublic = room.getTurnPhase() == TurnPhase.YUT_MOVE
                || room.getTurnPhase() == TurnPhase.YUT_MOVE_DONE
                || room.getTurnPhase() == TurnPhase.TURN_END
                || room.getTurnPhase() == TurnPhase.GAME_OVER
                || room.getTurnPhase() == TurnPhase.CATCH_BONUS_THROW;

        if (isPendingResultPublic || isTurnPlayer) {
            response.setPendingYutResults(room.getPendingYutResults());
        } else {
            response.setPendingYutResults(null);
        }

        response.setWinnerId(room.getWinnerId());

        return response;
    }
}