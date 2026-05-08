package com.example.mse.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mse.dto.RoomInfo;
import com.example.mse.model.BoardNode;
import com.example.mse.model.GameRoom;

@Service
public class RoomService {

    private Map<String, GameRoom> rooms = new HashMap<>();

    // 영준 추가함.
    @Autowired
    private BoardService boardService; // 지도를 그려주는 서비스

    public GameRoom createRoom(String playerId) {
        GameRoom room = new GameRoom();

        room.setRoomId(UUID.randomUUID().toString());
        room.getPlayerIds().add(playerId);

        rooms.put(room.getRoomId(), room);

        return room;
    }

    public GameRoom joinRoom(String roomId, String playerId) {
        GameRoom room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        if (room.isStarted()) {
            throw new RuntimeException("Game already started");
        }

        if (!room.getPlayerIds().contains(playerId)) {
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
        //     throw new RuntimeException("Need at least 2 players");
        // }

        room.setStarted(true);

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

        return info;
    }

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

        return room;
    }

}