package com.example.mse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;

@Service
public class GameStateAssembler {

    @Autowired
    private RoomService roomService;

    @Autowired
    private PlayerService playerService;

    public GameStateResponse build(GameRoom room) {

        GameStateResponse response = new GameStateResponse();

        response.setRoomInfo(roomService.toRoomInfo(room));
        response.setTurnInfo(room.getTurnInfo());
        response.setPlayers(playerService.getPlayerInfoByIds(room.getPlayerIds()));

        BoardStatusResponse boardStatus = new BoardStatusResponse();

        boardStatus.setAllPieces(room.getBoard().getPieces());

        boardStatus.setExtraTurn(
                room.getCurrentYutResult() != null &&
                        room.getCurrentYutResult().isExtraTurn());

        ThrowResponse throwResponse = new ThrowResponse();
        throwResponse.setSticks(room.getSticks());
        throwResponse.setPrivateSticks(room.getPrivateSticks());
        throwResponse.setPublicSticks(room.getPublicSticks());
        throwResponse.setYutResult(room.getCurrentYutResult());

        boardStatus.setThrowResult(throwResponse);

        boardStatus.setCurrentTurnPlayerId(
                room.getTurnInfo().getCurrentTurnPlayerId());

        boardStatus.setTurnPhase(room.getTurnPhase());

        response.setBoardStatus(boardStatus);

        response.setPublicSticks(room.getPublicSticks());
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());

        response.setChallengeDeadlineMillis(room.getChallengeDeadlineMillis());
        response.setServerTimeMillis(System.currentTimeMillis());

        response.setFirstChallenger(room.getFirstChallengerId());
        response.setChallengeQueue(room.getChallengeQueue());

        response.setWinnerId(room.getWinnerId());

        return response;
    }
}