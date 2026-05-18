package com.example.mse.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Player;
import com.example.mse.model.StickSide;
import com.example.mse.model.TurnPhase;
import com.example.mse.model.YutName;
import com.example.mse.model.YutResult;
import com.example.mse.service.GameFlowService;
import com.example.mse.service.GameStateAssembler;
import com.example.mse.service.HallService;
import com.example.mse.service.PlayerService;
import com.example.mse.service.RoomService;
import com.example.mse.service.TurnService;
import com.example.mse.service.YutService;

@RestController
@RequestMapping("/debug/game")
@CrossOrigin(origins = "*")
// Debug only. Remove or disable before final build.
public class DebugGameController {

    private static final String DEBUG_ROOM_ID = "DEBUG_GAME";
    private static final String P1 = "p1";
    private static final String P2 = "p2";

    @Autowired
    private RoomService roomService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TurnService turnService;

    @Autowired
    private YutService yutService;

    @Autowired
    private HallService hallService;

    @Autowired
    private GameFlowService gameFlowService;

    @Autowired
    private GameStateAssembler gameStateAssembler;

    /**
     * 테스트용 게임방을 새로 만든다.
     * roomId = DEBUG_GAME
     * players = p1, p2
     * turnPhase = PRIVATE_THROW
     */
    @PostMapping("/setup")
    public ApiResponse<GameStateResponse> setup(
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = createFreshDebugGame();

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Debug game created.", response);
    }

    /**
     * 현재 디버그 게임 상태 확인.
     */
    @GetMapping("/state")
    public ApiResponse<GameStateResponse> state(
            @RequestParam(defaultValue = DEBUG_ROOM_ID) String roomId,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = roomService.requireRoom(roomId);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Debug game state loaded.", response);
    }

    /**
     * 윷 던지기 직전 상태로 세팅.
     * 이후 실제 API POST /turn/throw 테스트 가능.
     */
    @PostMapping("/ready-to-throw")
    public ApiResponse<GameStateResponse> readyToThrow(
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        room.setTurnPhase(TurnPhase.PRIVATE_THROW);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Ready to throw.", response);
    }

    /**
     * 윷을 이미 던진 상태로 세팅.
     * 이후 실제 API POST /hall/declare 테스트 가능.
     */
    @PostMapping("/ready-to-declare")
    public ApiResponse<GameStateResponse> readyToDeclare(
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        room.setTurnPhase(TurnPhase.PRIVATE_THROW);

        ThrowResponse throwResponse = yutService.getThrowResponse(room);

        room.setTurnPhase(TurnPhase.MAIN_HALL_DECLARE);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Ready to declare. Yut result: "
                        + throwResponse.getYutResult().getResult());

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Ready to declare.", response);
    }

    /**
     * 챌린지 단계로 바로 세팅.
     *
     * truth=true  → 실제 private sticks와 같은 선언. 챌린지하면 실패해야 함.
     * truth=false → 실제 private sticks와 다른 선언. 챌린지하면 성공해야 함.
     *
     * 이후 실제 API POST /hall/challenge 테스트 가능.
     */
    @PostMapping("/ready-to-challenge")
    public ApiResponse<GameStateResponse> readyToChallenge(
            @RequestParam(defaultValue = "true") boolean truth,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        room.setTurnPhase(TurnPhase.PRIVATE_THROW);

        ThrowResponse throwResponse = yutService.getThrowResponse(room);

        StickSide[] actualPrivateSticks = throwResponse.getPrivateSticks();

        StickSide declaredS1;
        StickSide declaredS2;

        if (truth) {
            declaredS1 = actualPrivateSticks[0];
            declaredS2 = actualPrivateSticks[1];
        } else {
            declaredS1 = flipFirstPrivateStick(actualPrivateSticks[0]);
            declaredS2 = flipSecondPrivateStick(actualPrivateSticks[1]);
        }

        hallService.declarePrivateSticks(room, declaredS1, declaredS2);

        gameFlowService.startChallengePhase(room);

        gameFlowService.addLog(
                room,
                "DEBUG",
                truth
                        ? "Ready to challenge. Declaration is TRUE."
                        : "Ready to challenge. Declaration is FALSE.");

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Ready to challenge.", response);
    }

    /**
     * 이동 단계로 바로 세팅.
     * pendingYutResults에 원하는 윷 결과 하나를 넣는다.
     *
     * 이후 실제 API:
     * GET  /board/moveList
     * POST /board/move
     * 테스트 가능.
     */
    @PostMapping("/ready-to-move")
    public ApiResponse<GameStateResponse> readyToMove(
            @RequestParam(defaultValue = "DO") YutName yutName,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        YutResult result = createYutResult(yutName);

        room.setCurrentYutResult(result);
        room.getPendingYutResults().clear();
        room.getPendingYutResults().add(result);

        room.setTurnPhase(TurnPhase.YUT_MOVE);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Ready to move. Pending result: " + yutName);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Ready to move.", response);
    }

    /**
     * 턴 종료 직전 상태로 세팅.
     * 이후 실제 API POST /turn/end 테스트 가능.
     */
    @PostMapping("/ready-to-end-turn")
    public ApiResponse<GameStateResponse> readyToEndTurn(
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        room.getPendingYutResults().clear();
        room.setTurnPhase(TurnPhase.YUT_MOVE_DONE);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Ready to end turn.");

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Ready to end turn.", response);
    }

    /**
     * 현재 턴을 강제로 다음 플레이어로 넘긴다.
     */
    @PostMapping("/next-turn")
    public ApiResponse<GameStateResponse> nextTurn(
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        gameFlowService.endTurn(room);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Debug next turn applied.", response);
    }

    /**
     * phase만 강제로 변경한다.
     */
    @PostMapping("/phase")
    public ApiResponse<GameStateResponse> changePhase(
            @RequestParam(defaultValue = DEBUG_ROOM_ID) String roomId,
            @RequestParam TurnPhase phase,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = roomService.requireRoom(roomId);

        room.setTurnPhase(phase);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Debug phase changed.", response);
    }

    private GameRoom getOrCreateDebugGame() {
        GameRoom room = roomService.getRoom(DEBUG_ROOM_ID);

        if (room == null) {
            return createFreshDebugGame();
        }

        return room;
    }

    private GameRoom createFreshDebugGame() {
        saveDebugPlayer(P1, "Debug Player 1");
        saveDebugPlayer(P2, "Debug Player 2");

        GameRoom room = new GameRoom();

        room.setRoomId(DEBUG_ROOM_ID);
        room.setHostId(P1);
        room.getPlayerIds().clear();
        room.getPlayerIds().addAll(List.of(P1, P2));

        roomService.getAllRooms().put(DEBUG_ROOM_ID, room);

        room = roomService.startRoom(DEBUG_ROOM_ID);

        turnService.startGame(room);

        return room;
    }

    private void saveDebugPlayer(String id, String name) {
        Player player = new Player();
        player.setId(id);
        player.setName(name);
        player.setProfileUrl("debug-avatar-" + id);

        playerService.save(player);
    }

    private void resetThrowAndChallengeState(GameRoom room) {
        yutService.resetTurn(room);

        room.setDeclaredPrivateSticks(null);

        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
        room.getChallengeVotes().clear();
        room.setChallengeResolved(false);
        room.setChallengeDeadlineMillis(0L);
        room.setLastJudgeResponse(null);
    }

    private StickSide flipFirstPrivateStick(StickSide stick) {
        if (stick == StickSide.BACK) {
            return StickSide.HEAD;
        }

        return StickSide.BACK;
    }

    private StickSide flipSecondPrivateStick(StickSide stick) {
        if (stick == StickSide.HEAD) {
            return StickSide.TAIL;
        }

        return StickSide.HEAD;
    }

    private YutResult createYutResult(YutName yutName) {
        YutResult result = new YutResult();

        result.setResult(yutName);

        switch (yutName) {
            case BACK_DO:
                result.setMove(-1);
                result.setExtraTurn(false);
                break;

            case DO:
                result.setMove(1);
                result.setExtraTurn(false);
                break;

            case GAE:
                result.setMove(2);
                result.setExtraTurn(false);
                break;

            case GEOL:
                result.setMove(3);
                result.setExtraTurn(false);
                break;

            case YUT:
                result.setMove(4);
                result.setExtraTurn(true);
                break;

            case MO:
                result.setMove(5);
                result.setExtraTurn(true);
                break;
        }

        return result;
    }
}