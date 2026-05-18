package com.example.mse.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.example.mse.dto.JudgeResponse.JudgeResult;
import com.example.mse.model.GameRoom;
import com.example.mse.model.StickSide;

@Service
public class HallService {

    public void declarePrivateSticks(GameRoom room, StickSide s1, StickSide s2) {
        room.setDeclaredPrivateSticks(new StickSide[] { s1, s2 });
    }

    public void declarePrivateSticks(GameRoom room, StickSide s1) {
        room.setDeclaredPrivateSticks(new StickSide[] { s1 });
    }

    public String voteChallenge(GameRoom room, String playerId, boolean challenge) {

        if (playerId == null) {
            return "Player id is required.";
        }

        if (room.isChallengeResolved()) {
            return "Challenge already resolved.";
        }

        if (room.getFirstChallengerId() != null) {
            return "Challenge already made.";
        }

        if (System.currentTimeMillis() > room.getChallengeDeadlineMillis()) {
            return "Challenge time is over.";
        }

        String turnPlayerId = room.getTurnInfo().getCurrentTurnPlayerId();

        if (playerId.equals(turnPlayerId)) {
            return "Turn player cannot vote challenge.";
        }

        room.getChallengeVotes().put(playerId, challenge);

        if (challenge) {
            room.setFirstChallengerId(playerId);

            if (!room.getChallengeQueue().contains(playerId)) {
                room.getChallengeQueue().add(playerId);
            }

            return "Challenge voted: O";
        }

        return "Challenge voted: X";
    }

    public JudgeResult judgeChallenge(GameRoom room) {

        if (room.getDeclaredPrivateSticks() == null
                || room.getDeclaredPrivateSticks().length != 2) {
            throw new RuntimeException("Declared private sticks not found.");
        }

        boolean truth = Arrays.equals(
                room.getPrivateSticks(),
                room.getDeclaredPrivateSticks());

        if (truth) {
            return JudgeResult.CHALLENGE_FAIL;
        } else {
            return JudgeResult.CHALLENGE_SUCCESS;
        }
    }
}