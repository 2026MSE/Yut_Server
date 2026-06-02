package com.example.mse.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.BoardStatusResponse;
import com.example.mse.dto.GameStateResponse;
import com.example.mse.dto.PieceInfo;
import com.example.mse.dto.PlayerEffectInfo;
import com.example.mse.dto.PlayerInfo;
import com.example.mse.model.GameRoom;
import com.example.mse.model.Piece;
import com.example.mse.model.PlayerEffect;
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
        response.setTurnPhase(room.getTurnPhase());

        List<PlayerInfo> players = playerService.getPlayerInfoByIds(room.getPlayerIds());

        for (PlayerInfo player : players) {
            if (!player.getPlayerId().equals(playerId)) {
                player.getInventory().clear();
            }
// 영준 추가 6/2 실제 player모델에서 이모티콘 값을 가져와 세팅
            com.example.mse.model.Player originalPlayer = playerService.get(player.getPlayerId());
            if (originalPlayer != null && originalPlayer.getCurrentEmoticon() != null) {
                player.setCurrentEmoticon(originalPlayer.getCurrentEmoticon());
            }
        }

        response.setPlayers(players);

        BoardStatusResponse boardStatus = new BoardStatusResponse();

        boardStatus.setAllPieces(toPieceInfoMap(room.getBoard().getPieces()));

        response.setBoardStatus(boardStatus);

        boolean isTurnPlayer = room.getTurnInfo().getCurrentTurnPlayerId() != null
                && room.getTurnInfo().getCurrentTurnPlayerId().equals(playerId);

        boolean hasYutResult = room.getCurrentYutResult() != null;

        boolean isPrivateThrowResultPhase = room.getTurnPhase() == TurnPhase.PRIVATE_THROW_RESULT;

        boolean isCatchBonusThrowResultPhase = room.getTurnPhase() == TurnPhase.CATCH_BONUS_THROW_RESULT;

        boolean isDeclareOrLater = room.getTurnPhase() == TurnPhase.MAIN_HALL_DECLARE
                || room.getTurnPhase() == TurnPhase.MAIN_HALL_CHALLENGE
                || room.getTurnPhase() == TurnPhase.CHALLENGE_RESULT
                || room.getTurnPhase() == TurnPhase.YUT_MOVE
                || room.getTurnPhase() == TurnPhase.YUT_MOVE_DONE
                || room.getTurnPhase() == TurnPhase.TURN_END
                || room.getTurnPhase() == TurnPhase.GAME_OVER;

        boolean isResultPublic = room.isChallengeResolved()
                || room.getTurnPhase() == TurnPhase.CHALLENGE_RESULT
                || room.getTurnPhase() == TurnPhase.YUT_MOVE
                || room.getTurnPhase() == TurnPhase.YUT_MOVE_DONE
                || room.getTurnPhase() == TurnPhase.TURN_END
                || room.getTurnPhase() == TurnPhase.GAME_OVER;

        if (hasYutResult && (isTurnPlayer || isResultPublic)) {
            response.setCurrentYutResult(room.getCurrentYutResult());
        } else {
            response.setCurrentYutResult(null);
        }

        // PRIVATE_THROW_RESULT:
        // 턴 플레이어만 publicSticks 확인
        //
        // MAIN_HALL_DECLARE 이후:
        // 모두 publicSticks 확인
        //
        // CATCH_BONUS_THROW_RESULT:
        // 잡기 보너스 결과 확인 중이므로 턴 플레이어만 publicSticks 확인
        if (hasYutResult && isTurnPlayer) {
            response.setPublicSticks(room.getPublicSticks());
        } else if (hasYutResult && isDeclareOrLater && !isPrivateThrowResultPhase && !isCatchBonusThrowResultPhase) {
            response.setPublicSticks(room.getPublicSticks());
        } else {
            response.setPublicSticks(null);
        }

        // privateSticks는 항상 턴 플레이어만 확인
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

        boolean isPendingResultPublic = room.getTurnPhase() == TurnPhase.CHALLENGE_RESULT
                || room.getTurnPhase() == TurnPhase.YUT_MOVE
                || room.getTurnPhase() == TurnPhase.YUT_MOVE_DONE
                || room.getTurnPhase() == TurnPhase.TURN_END
                || room.getTurnPhase() == TurnPhase.GAME_OVER;

        if (isPendingResultPublic || isTurnPlayer) {
            response.setPendingYutResults(room.getPendingYutResults());
        } else {
            response.setPendingYutResults(null);
        }

        response.setWinnerId(room.getWinnerId());
        response.setMoveHistory(room.getMoveHistory());
        response.setLastJudgeResponse(room.getLastJudgeResponse());
        response.setActiveEffects(toEffectInfoList(room.getEffects()));

        return response;
    }

    private Map<String, List<PieceInfo>> toPieceInfoMap(Map<String, List<Piece>> piecesMap) {
        Map<String, List<PieceInfo>> result = new HashMap<>();

        for (Map.Entry<String, List<Piece>> entry : piecesMap.entrySet()) {
            String playerId = entry.getKey();
            List<Piece> pieces = entry.getValue();

            List<PieceInfo> pieceInfos = new ArrayList<>();

            for (Piece piece : pieces) {
                List<String> carriedPieceIds = new ArrayList<>();

                for (Piece carried : piece.getCarriedPieces()) {
                    carriedPieceIds.add(carried.getId());
                }

                PieceInfo info = new PieceInfo(
                        piece.getId(),
                        piece.getOwnerId(),
                        piece.getCurrentPosition(),
                        piece.getCarriedByPieceId(),
                        carriedPieceIds);

                pieceInfos.add(info);
            }

            result.put(playerId, pieceInfos);
        }

        return result;
    }

    private List<PlayerEffectInfo> toEffectInfoList(List<PlayerEffect> effects) {
        List<PlayerEffectInfo> result = new ArrayList<>();

        for (PlayerEffect effect : effects) {
            if (effect.getRemainingTurns() <= 0) {
                continue;
            }

            result.add(new PlayerEffectInfo(
                    effect.getType(),
                    effect.getTargetPlayerId(),
                    effect.getSourcePlayerId(),
                    effect.getRemainingTurns(),
                    effect.getValue()));
        }

        return result;
    }
}