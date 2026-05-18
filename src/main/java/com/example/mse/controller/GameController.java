package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.service.RoomService;
import com.example.mse.service.GameFlowService;
import com.example.mse.service.GameStateAssembler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = "*")
public class GameController {

        // 방 조회 및 RoomInfo 변환 담당 서비스
        @Autowired
        private RoomService roomService;

        // 게임 흐름 및 TurnPhase 관리 서비스
        @Autowired
        private GameFlowService gameFlowService;

        @Autowired
        private GameStateAssembler gameStateAssembler;

        @GetMapping("/state")
        public ApiResponse<GameStateResponse> getGameState(
                        @RequestParam String roomId,
                        @RequestParam String playerId) {

                GameRoom room = roomService.requireRoom(roomId);

                gameFlowService.resolveChallengeIfReady(room);

                GameStateResponse response = gameStateAssembler.build(room, playerId);

                return ApiResponse.ok("Game state loaded.", response);
        }
}