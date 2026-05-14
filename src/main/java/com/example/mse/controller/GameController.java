package com.example.mse.controller;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.service.PlayerService;
import com.example.mse.service.RoomService;
import com.example.mse.service.GameFlowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@CrossOrigin(origins = "*")
public class GameController {

    // 방 조회 및 RoomInfo 변환 담당 서비스
    @Autowired
    private RoomService roomService;

    // 플레이어 정보 조회 서비스
    @Autowired
    private PlayerService playerService;

    // 게임 흐름 및 TurnPhase 관리 서비스
    @Autowired
    GameFlowService gameFlowService;

    /**
     * 멀티플레이 전체 게임 상태 polling API
     * 
     * Unity 클라이언트가 주기적으로 호출하여:
     * - 현재 턴
     * - 윷 상태
     * - 보드 상태
     * - 챌린지 상태
     * - 플레이어 정보
     * 등을 한 번에 받아간다.
     */
    @GetMapping("/state")
    public ApiResponse<GameStateResponse> getGameState(@RequestParam String roomId) {

        // roomId로 현재 게임방 조회
        GameRoom room = roomService.requireRoom(roomId);

        // 챌린지 시간이 끝났는지 확인
        // - challenger가 없고
        // - 5초가 지났으면
        // 자동으로 YUT_MOVE phase로 전환
        gameFlowService.resolveChallengeTimeout(room);

        // 최종적으로 클라이언트에게 내려줄 통합 응답 DTO
        GameStateResponse response = new GameStateResponse();

        // 방 정보 세팅
        // - roomId
        // - hostId
        // - playerIds
        // - started 여부 등
        response.setRoomInfo(roomService.toRoomInfo(room));

        // 현재 턴 정보 세팅
        // - 현재 턴 플레이어
        // - 턴 순서
        // - 현재 room(Scene) 등
        response.setTurnInfo(room.getTurnInfo());

        // 플레이어 정보 목록 세팅
        // - 이름
        // - 프로필 이미지
        // - 현재 이모티콘 등
        response.setPlayers(playerService.getPlayerInfoByIds(room.getPlayerIds()));

        // 보드 상태 DTO 생성
        BoardStatusResponse boardStatus = new BoardStatusResponse();

        // 모든 플레이어의 말 상태 세팅
        boardStatus.setAllPieces(room.getBoard().getPieces());

        // 현재 결과가 추가 턴인지 여부
        boardStatus.setExtraTurn(
                room.getCurrentYutResult() != null &&
                        room.getCurrentYutResult().isExtraTurn());

        // 윷 던진 결과 DTO 생성
        ThrowResponse throwResponse = new ThrowResponse();

        // 실제 윷 4개 결과
        throwResponse.setSticks(room.getSticks());

        // private room에서만 보이는 윷 2개
        throwResponse.setPrivateSticks(room.getPrivateSticks());

        // main hall에 공개되는 윷 2개
        throwResponse.setPublicSticks(room.getPublicSticks());

        // 최종 윷 결과
        // (도/개/걸/윷/모/백도)
        throwResponse.setYutResult(room.getCurrentYutResult());

        // boardStatus에 윷 결과 저장
        boardStatus.setThrowResult(throwResponse);

        // 현재 턴 플레이어 ID
        boardStatus.setCurrentTurnPlayerId(
                room.getTurnInfo().getCurrentTurnPlayerId());

        // 현재 턴 플레이어가 위치한 Scene
        // (PRIVATE_ROOM / MAIN_HALL / YUT_ROOM)
        boardStatus.setCurrentRoom(
                room.getTurnInfo().getCurrentTurnPlayerRoom());

        // 현재 게임 진행 단계
        // (TurnPhase)
        boardStatus.setTurnPhase(room.getTurnPhase());

        // 최종 boardStatus 세팅
        response.setBoardStatus(boardStatus);

        // Main Hall에 공개된 윷 정보
        response.setPublicSticks(room.getPublicSticks());

        // 턴 플레이어가 선언한 private sticks
        response.setDeclaredPrivateSticks(room.getDeclaredPrivateSticks());

        // 챌린지 종료 시간(ms)
        // Unity에서 남은 시간 계산 가능
        response.setChallengeDeadlineMillis(
                room.getChallengeDeadlineMillis());

        // 현재 서버 시간(ms)
        // 클라이언트와 시간 동기화용
        response.setServerTimeMillis(
                System.currentTimeMillis());

        // 가장 먼저 챌린지를 누른 플레이어
        response.setFirstChallenger(
                room.getFirstChallengerId());

        // 챌린지 요청 순서 목록
        response.setChallengeQueue(
                room.getChallengeQueue());

        // 게임 종료 시 승리 플레이어 ID
        response.setWinnerId(
                room.getWinnerId());

        // 최종 응답 반환
        return ApiResponse.ok("Game state loaded.", response);
    }
}