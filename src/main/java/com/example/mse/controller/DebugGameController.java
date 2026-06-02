package com.example.mse.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.dto.ThrowResponse;
import com.example.mse.model.ChanceCard;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Piece;
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
     *
     * POST /debug/game/setup
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
     *
     * GET /debug/game/state?viewerId=p1
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
     *
     * POST /debug/game/ready-to-throw
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
     *
     * POST /debug/game/ready-to-declare
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
     * POST /debug/game/ready-to-challenge?truth=false
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

        if (actualPrivateSticks.length == 1) {
            StickSide declaredS1 = truth
                    ? actualPrivateSticks[0]
                    : flipFirstPrivateStick(actualPrivateSticks[0]);

            hallService.declarePrivateSticks(room, declaredS1);
        } else {
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
        }

        gameFlowService.startChallengePhase(room);

        // 디버그에서는 충분히 긴 시간 부여
        room.setChallengeDeadlineMillis(System.currentTimeMillis() + 10 * 60 * 1000);

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
     * 챌린지 결과 단계로 바로 세팅.
     *
     * yutName=YUT 또는 MO로 세팅한 뒤
     * POST /hall/challenge/result/confirm 호출 시
     * PRIVATE_THROW로 한 번 더 가는지 확인 가능.
     *
     * POST /debug/game/ready-challenge-result?yutName=YUT
     */
    @PostMapping("/ready-challenge-result")
    public ApiResponse<GameStateResponse> readyChallengeResult(
            @RequestParam(defaultValue = "YUT") YutName yutName,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        YutResult result = createYutResult(yutName, "THROW", null);

        room.setCurrentYutResult(result);
        room.setLastJudgeResponse(null);
        room.setChallengeResolved(true);
        room.setTurnPhase(TurnPhase.CHALLENGE_RESULT);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Ready challenge result. Yut result: " + yutName);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Ready challenge result.", response);
    }

    /**
     * 이동 단계로 바로 세팅.
     * pendingYutResults에 원하는 윷 결과 하나를 넣는다.
     *
     * POST /debug/game/ready-to-move?yutName=DO
     */
    @PostMapping("/ready-to-move")
    public ApiResponse<GameStateResponse> readyToMove(
            @RequestParam(defaultValue = "DO") YutName yutName,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        YutResult result = createYutResult(yutName, "THROW", null);

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
     * 이동 단계로 바로 세팅하되, pendingYutResults에 여러 개의 윷 결과를 넣는다.
     *
     * 예:
     * POST /debug/game/ready-to-move-multiple?yutNames=DO&yutNames=YUT&yutNames=GAE
     */
    @PostMapping("/ready-to-move-multiple")
    public ApiResponse<GameStateResponse> readyToMoveMultiple(
            @RequestParam List<YutName> yutNames,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        resetThrowAndChallengeState(room);

        room.getPendingYutResults().clear();

        for (YutName yutName : yutNames) {
            room.getPendingYutResults().add(
                    createYutResult(yutName, "THROW", null));
        }

        if (!room.getPendingYutResults().isEmpty()) {
            room.setCurrentYutResult(
                    room.getPendingYutResults().get(room.getPendingYutResults().size() - 1));
        } else {
            room.setCurrentYutResult(null);
        }

        room.setTurnPhase(TurnPhase.YUT_MOVE);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Ready to move multiple. Pending results: " + yutNames);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Ready to move multiple.", response);
    }

    /**
     * 찬스카드 이동권을 pendingYutResults에 직접 추가한다.
     * /chance/use 없이 moveList 표시만 빠르게 확인하고 싶을 때 사용.
     *
     * POST /debug/game/add-chance-result?card=BONUS_GEOL
     */
    @PostMapping("/add-chance-result")
    public ApiResponse<GameStateResponse> addChanceResult(
            @RequestParam ChanceCard card,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        if (room.getTurnPhase() != TurnPhase.YUT_MOVE) {
            room.setTurnPhase(TurnPhase.YUT_MOVE);
        }

        YutResult result = createYutResultFromChanceCard(card);

        room.getPendingYutResults().add(result);
        room.setCurrentYutResult(result);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Chance result added directly: " + card.name());

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Chance result added.", response);
    }

    /**
     * 특정 플레이어에게 찬스카드를 강제로 지급한다.
     *
     * 예:
     * POST /debug/game/give-card?playerId=p1&card=BONUS_GEOL
     */
    @PostMapping("/give-card")
    public ApiResponse<GameStateResponse> giveChanceCard(
            @RequestParam(defaultValue = P1) String playerId,
            @RequestParam ChanceCard card,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        Player player = playerService.get(playerId);

        if (player == null) {
            return ApiResponse.fail("Player not found.");
        }

        player.getInventory().add(card);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Chance card given: " + playerId + " -> " + card.name());

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Chance card given.", response);
    }

    /**
     * 특정 플레이어의 인벤토리를 비운다.
     *
     * POST /debug/game/clear-inventory?playerId=p1
     */
    @PostMapping("/clear-inventory")
    public ApiResponse<GameStateResponse> clearInventory(
            @RequestParam(defaultValue = P1) String playerId,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        Player player = playerService.get(playerId);

        if (player == null) {
            return ApiResponse.fail("Player not found.");
        }

        player.getInventory().clear();

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Inventory cleared: " + playerId);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Inventory cleared.", response);
    }

    /**
     * 특정 말의 위치를 강제로 변경한다.
     *
     * 잡기 테스트 예:
     * POST /debug/game/set-piece-position?playerId=p2&pieceId=p2_piece_1&position=1
     */
    @PostMapping("/set-piece-position")
    public ApiResponse<GameStateResponse> setPiecePosition(
            @RequestParam String playerId,
            @RequestParam String pieceId,
            @RequestParam int position,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        List<Piece> pieces = room.getBoard().getPieces().get(playerId);

        if (pieces == null) {
            return ApiResponse.fail("Player pieces not found.");
        }

        Piece targetPiece = null;

        for (Piece piece : pieces) {
            if (piece.getId().equals(pieceId)) {
                targetPiece = piece;
                break;
            }
        }

        if (targetPiece == null) {
            return ApiResponse.fail("Piece not found.");
        }

        removePieceFromAllNodes(room, targetPiece);

        // 디버그 강제 이동에서는 업기 관계를 초기화한다.
        targetPiece.setCarriedByPieceId(null);
        targetPiece.getCarriedPieces().clear();
        targetPiece.setCurrentPosition(position);

        room.getBoard()
                .getNodePiecesMap()
                .computeIfAbsent(position, k -> new ArrayList<>())
                .add(targetPiece);

        gameFlowService.addLog(
                room,
                "DEBUG",
                "Piece position changed: "
                        + pieceId
                        + " -> "
                        + position);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Piece position changed.", response);
    }

    /**
     * 턴 종료 직전 상태로 세팅.
     * 이후 실제 API POST /turn/end 테스트 가능.
     *
     * POST /debug/game/ready-to-end-turn
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
     *
     * POST /debug/game/next-turn
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
     *
     * POST /debug/game/phase?phase=YUT_MOVE
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

    /**
     * 디버그 게임을 GAME_OVER 상태로 만든다.
     * endGame() 동작, winnerId, inventory 초기화 테스트에 사용.
     *
     * POST /debug/game/game-over?winnerId=p1
     */
    @PostMapping("/game-over")
    public ApiResponse<GameStateResponse> forceGameOver(
            @RequestParam(defaultValue = P1) String winnerId,
            @RequestParam(defaultValue = P1) String viewerId) {

        GameRoom room = getOrCreateDebugGame();

        gameFlowService.endGame(room, winnerId);

        GameStateResponse response = gameStateAssembler.build(room, viewerId);

        return ApiResponse.ok("Debug game over applied.", response);
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
        Player player = playerService.get(id);

        if (player == null) {
            player = new Player();
            player.setId(id);
        }

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

    private void removePieceFromAllNodes(GameRoom room, Piece piece) {
        for (List<Piece> nodePieces : room.getBoard().getNodePiecesMap().values()) {
            nodePieces.remove(piece);
        }
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

    private YutResult createYutResult(YutName yutName, String source, String sourceCard) {
        YutResult result = new YutResult();

        result.setResult(yutName);
        result.setSource(source);
        result.setSourceCard(sourceCard);

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

    private YutResult createYutResultFromChanceCard(ChanceCard card) {
        switch (card) {
            case BONUS_DO:
                return createYutResult(YutName.DO, "CHANCE_CARD", card.name());

            case BONUS_GAE:
                return createYutResult(YutName.GAE, "CHANCE_CARD", card.name());

            case BONUS_GEOL:
                return createYutResult(YutName.GEOL, "CHANCE_CARD", card.name());

            case BONUS_YUT: {
                YutResult result = createYutResult(YutName.YUT, "CHANCE_CARD", card.name());
                result.setExtraTurn(false);
                return result;
            }

            case BONUS_MO: {
                YutResult result = createYutResult(YutName.MO, "CHANCE_CARD", card.name());
                result.setExtraTurn(false);
                return result;
            }

            default:
                throw new RuntimeException("Unsupported chance card.");
        }
    }
}