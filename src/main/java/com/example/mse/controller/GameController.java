package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.service.PlayerService;
import com.example.mse.service.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private PlayerService playerService;

    @GetMapping("/state")
    public ApiResponse<GameStateResponse> getGameState(@RequestParam String roomId) {

        GameRoom room = roomService.requireRoom(roomId);

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
        boardStatus.setCurrentTurnPlayerId(room.getTurnInfo().getCurrentTurnPlayerId());
        boardStatus.setCurrentRoom(room.getTurnInfo().getCurrentTurnPlayerRoom());
        boardStatus.setTurnPhase(room.getTurnPhase());

        response.setBoardStatus(boardStatus);

        response.setPublicSticks(room.getPublicSticks());
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());
        response.setFirstChallenger(room.getFirstChallengerId());
        response.setChallengeQueue(room.getChallengeQueue());
        response.setWinnerId(room.getWinnerId());

        return ApiResponse.ok("Game state loaded.", response);
    }
}