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

    public String challenge(GameRoom room, String playerId) {

        if (System.currentTimeMillis() > room.getChallengeDeadlineMillis()) {
            return "Challenge time is over.";
        }

        if (room.getFirstChallengerId() == null) {
            room.setFirstChallengerId(playerId);
            room.getChallengeQueue().add(playerId);
            return "You are the first challenger.";
        }

        return "Too late. First challenger is " + room.getFirstChallengerId();
    }

    public JudgeResult judgeChallenge(GameRoom room) {

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