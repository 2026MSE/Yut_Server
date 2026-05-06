package com.example.mse.service;

import com.example.mse.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.mse.dto.PlayerInfo;

@Service
public class PlayerService {

    private Map<String, Player> players = new HashMap<>();

    public Player save(Player player) {
        players.put(player.getId(), player);
        return player;
    }

    public Player get(String id) {
        return players.get(id);
    }

    public Map<String, Player> getAll() {
        return players;
    }

    public List<PlayerInfo> getPlayerInfoByIds(List<String> playerIds) {
        List<PlayerInfo> result = new ArrayList<>();

        for (String playerId : playerIds) {
            Player player = players.get(playerId);

            if (player == null) {
                continue;
            }

            PlayerInfo info = new PlayerInfo();

            info.setPlayerId(player.getId());
            info.setName(player.getName());
            info.setCurrentEmoticon("");

            result.add(info);
        }

        return result;
    }

}