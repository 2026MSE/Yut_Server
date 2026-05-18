package com.example.mse.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.RoomInfo;
import com.example.mse.model.Board;
import com.example.mse.model.BoardNode;
import com.example.mse.model.GameRoom;
import com.example.mse.model.TurnPhase;

@Service
public class RoomService {

    private Map<String, GameRoom> rooms = new HashMap<>();

    // 영준 추가함.
    @Autowired
    private BoardService boardService; // 지도를 그려주는 서비스

    // 방 만든 사람이 방장
    public GameRoom createRoom(String playerId) {
        GameRoom room = new GameRoom();

        room.setRoomId(generateRoomCode());
        room.setHostId(playerId);
        room.getPlayerIds().add(playerId);

        rooms.put(room.getRoomId(), room);

        return room;
    }

    // 방 코드 간소화
    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        String code;

        do {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 5; i++) {
                int index = (int) (Math.random() * chars.length());
                sb.append(chars.charAt(index));
            }

            code = sb.toString();
        } while (rooms.containsKey(code));

        return code;
    }

    public GameRoom joinRoom(String roomId, String playerId) {
        GameRoom room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found.");
        }

        if (room.isStarted()) {
            throw new RuntimeException("Game already started.");
        }

        if (!room.getPlayerIds().contains(playerId)) {
            if (room.getPlayerIds().size() >= 4) {
                throw new RuntimeException("Room is full.");
            }

            room.getPlayerIds().add(playerId);
        }

        return room;
    }

    public GameRoom getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public Map<String, GameRoom> getAllRooms() {
        return rooms;
    }

    public GameRoom startRoom(String roomId) {
        GameRoom room = requireRoom(roomId);

        // if (room.getPlayerIds().size() < 2) {
        // throw new RuntimeException("Need at least 2 players");
        // }

        room.setStarted(true);
        room.setWinnerId(null);
        room.setTurnPhase(TurnPhase.WAITING);

        room.setBoard(new Board());

        // 영준 추가
        // 게임이 시작되면, BoardService에게 지도를 받아와서 방의 보드판에 장착합니다.
        Map<Integer, BoardNode> initialMap = boardService.initBoard();
        room.getBoard().setNodeMap(initialMap);

        // 추가된 부분 2.플레이어한테 말 4개씩 쥐어주기
        boardService.initPieces(room.getBoard(), room.getPlayerIds());

        return room;
    }

    public GameRoom requireRoom(String roomId) {
        GameRoom room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        return room;
    }

    // 찬미 RoomInfo DTO로 변환하는 메서드 추가
    public RoomInfo toRoomInfo(GameRoom room) {
        RoomInfo info = new RoomInfo();
        info.setRoomId(room.getRoomId());
        info.setPlayerIds(room.getPlayerIds());
        info.setStarted(room.isStarted());
        info.setHostId(room.getHostId());
        return info;
    }

    // 현재 프로토타입에서는 게임 시작 후 leave/disconnect를 지원하지 않는다.
    // 게임 중 퇴장을 허용하려면:
    // 1. turnOrder에서 플레이어 제거
    // 2. board에서 해당 플레이어의 말 제거
    // 3. 현재 턴 플레이어가 나간 경우 nextTurn 처리
    // 4. 챌린저가 나간 경우 challenge 상태 정리
    // 5. 남은 플레이어가 1명인 경우 자동 승리 처리가 필요하다.
    // 방 나가면 다음 사람이 자동 방장
    public GameRoom leaveRoom(String roomId, String playerId) {
        GameRoom room = requireRoom(roomId);

        if (room.isStarted()) {
            throw new RuntimeException("Cannot leave after game started.");
        }

        room.getPlayerIds().remove(playerId);

        if (room.getPlayerIds().isEmpty()) {
            rooms.remove(roomId);
            return room;
        }

        if (playerId.equals(room.getHostId())) {
            room.setHostId(room.getPlayerIds().get(0));
        }

        return room;
    }

}