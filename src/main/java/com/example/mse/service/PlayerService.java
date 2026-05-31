package com.example.mse.service;

import com.example.mse.model.ChanceCard;
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
            info.setProfileUrl(player.getProfileUrl());

            List<String> inventory = new ArrayList<>();

            for (ChanceCard card : player.getInventory()) {
                inventory.add(card.name());
            }

            info.setInventory(inventory);

            result.add(info);
        }

        return result;
    }

    public ChanceCard giveRandomChanceCard(String playerId) {
        Player player = players.get(playerId);

        if (player == null) {
            return null;
        }

        ChanceCard[] cards = ChanceCard.values();

        if (cards.length == 0) {
            return null;
        }

        int index = (int) (Math.random() * cards.length);
        ChanceCard card = cards[index];

        player.getInventory().add(card);

        return card;
    }

    public boolean hasChanceCard(String playerId, ChanceCard card) {
        Player player = players.get(playerId);

        if (player == null) {
            return false;
        }

        return player.getInventory().contains(card);
    }

    public void removeChanceCard(String playerId, ChanceCard card) {
        Player player = players.get(playerId);

        if (player == null) {
            throw new RuntimeException("Player not found.");
        }

        boolean removed = player.getInventory().remove(card);

        if (!removed) {
            throw new RuntimeException("Chance card not found in inventory.");
        }
    }
}