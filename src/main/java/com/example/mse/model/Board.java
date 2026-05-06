package com.example.mse.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Board {
    // 1. 윷판의 지도 (0~28번 노드의 연결 정보)
    private Map<Integer, BoardNode> nodeMap = new HashMap<>();
    // 2. 플레이어별 말의 목록 (Key: playerId, Value: 말 4개가 담긴 리스트)
    private Map<String, List<Piece>> pieces = new HashMap<>();
    // 3. 특정 노드(칸)에 현재 어떤 말들이 있는지 빠른 검색을 위한 Map (잡기/업기 확인용)
    private Map<Integer, List<Piece>> nodePiecesMap = new HashMap<>();
}
