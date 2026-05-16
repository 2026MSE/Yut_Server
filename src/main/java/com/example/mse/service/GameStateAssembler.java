package com.example.mse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.model.GameRoom;

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

        response.setCurrentYutResult(room.getCurrentYutResult());

        boolean isTurnPlayer = room.getTurnInfo().getCurrentTurnPlayerId() != null
                && room.getTurnInfo().getCurrentTurnPlayerId().equals(playerId);

        if (isTurnPlayer) {
            response.setPrivateSticks(room.getPrivateSticks());
        } else {
            response.setPrivateSticks(null);
        }

        response.setBoardStatus(boardStatus);

        response.setPublicSticks(room.getPublicSticks());
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());

        response.setChallengeDeadlineMillis(room.getChallengeDeadlineMillis());
        response.setServerTimeMillis(System.currentTimeMillis());

        response.setFirstChallenger(room.getFirstChallengerId());
        response.setChallengeQueue(room.getChallengeQueue());

        response.setPendingYutResults(room.getPendingYutResults());

        response.setWinnerId(room.getWinnerId());

        return response;
    }
}