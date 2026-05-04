package com.example.mse.service;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.example.mse.model.GameRoom;
import com.example.mse.model.HallState;
import com.example.mse.model.StickSide;

@Service
public class HallService {

    public void declarePrivateSticks(GameRoom room, StickSide s1, StickSide s2) {
        room.setDeclaredPrivateSticks(new StickSide[] { s1, s2 });

        room.setHallState(HallState.CHALLENGE);
        room.setFirstChallengerId(null);
        room.getChallengeQueue().clear();
    }

    public String challenge(GameRoom room, String playerId) {

        if (room.getHallState() != HallState.CHALLENGE) {
            return "Not in challenge phase";
        }

        if (!room.getChallengeQueue().contains(playerId)) {
            room.getChallengeQueue().add(playerId);
        }

        if (room.getFirstChallengerId() == null) {
            room.setFirstChallengerId(playerId);
            return "You are the FIRST challenger!";
        }

        return "Too late. First challenger is " + room.getFirstChallengerId();
    }

    public String judgeChallenge(GameRoom room) {

        if (room.getFirstChallengerId() == null) {
            return "No challenger";
        }

        boolean truth = Arrays.equals(
                room.getPrivateSticks(),
                room.getDeclaredPrivateSticks()
        );

        if (truth) {
            return "Challenge failed. Turn player was telling the truth.";
        } else {
            return "Challenge success. Turn player was bluffing.";
        }
    }
}