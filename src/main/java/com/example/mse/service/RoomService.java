package com.example.mse.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.mse.model.GameRoom;

@Service
public class RoomService {

    private Map<String, GameRoom> rooms = new HashMap<>();

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

        if (room.getPlayerIds().size() < 2) {
            throw new RuntimeException("Need at least 2 players");
        }

        room.setStarted(true);

        return room;
    }

    public GameRoom requireRoom(String roomId) {
        GameRoom room = rooms.get(roomId);

        if (room == null) {
            throw new RuntimeException("Room not found");
        }

        return room;
    }

}