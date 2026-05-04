package com.example.yut_server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter 
@Setter 
@AllArgsConstructor
public class MoveResult {

    private String playerId;    
    private List<Integer> movingPieceIds;   // 함께 이동하는 모든 말의 id
    private List<Integer> path; // 이동 경로
    @JsonProperty("isCatch")
    private boolean isCatch;    // 잡기 발생 여부
    @JsonProperty("isFinished")
    private boolean isFinished; // 완주여부
}