package com.example.mse.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChallengeVoteRequest {
    private String roomId;
    private String playerId;
    private boolean challenge;
}