package com.example.mse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private Map<String, Player> players = new HashMap<>();

    public Player save(Player player){
        players.put(player.getId(), player);
        return player;
    }

    public Player get(String id){
        return players.get(id);
    }

    public Map<String, Player> getAll(){
        return players;
    }

    public List<PlayerInfo> getAllPlayerInfo() {
        List<PlayerInfo> result = new ArrayList<>();

        for (Player player : players.values()) {
            PlayerInfo info = new PlayerInfo();

            info.setPlayerId(player.getId());
            info.setName(player.getName());
            info.setProfileUrl(player.getProfileUrl());

            info.setIcon("default");
            info.setCurrentEmoticon("");
            info.setAlive(true);

            result.add(info);
        }

        return result;
    }

}