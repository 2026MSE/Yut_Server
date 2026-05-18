package com.example.mse.service;

import org.springframework.stereotype.Service;

import com.example.mse.model.EffectType;
import com.example.mse.model.GameRoom;
import com.example.mse.model.PlayerEffect;

@Service
public class EffectService {

    public void addEffect(
            GameRoom room,
            EffectType type,
            String targetPlayerId,
            String sourcePlayerId,
            int remainingTurns,
            int value) {

        room.getEffects().add(
                new PlayerEffect(
                        type,
                        targetPlayerId,
                        sourcePlayerId,
                        remainingTurns,
                        value));
    }

    public boolean hasEffect(GameRoom room, String playerId, EffectType type) {
        return getEffect(room, playerId, type) != null;
    }

    public PlayerEffect getEffect(GameRoom room, String playerId, EffectType type) {
        for (PlayerEffect effect : room.getEffects()) {
            if (effect.getTargetPlayerId().equals(playerId)
                    && effect.getType() == type
                    && effect.getRemainingTurns() > 0) {
                return effect;
            }
        }

        return null;
    }

    public void consumeEffect(GameRoom room, String playerId, EffectType type) {
        for (PlayerEffect effect : room.getEffects()) {
            if (effect.getTargetPlayerId().equals(playerId)
                    && effect.getType() == type
                    && effect.getRemainingTurns() > 0) {

                effect.setRemainingTurns(effect.getRemainingTurns() - 1);
            }
        }

        room.getEffects().removeIf(effect -> effect.getRemainingTurns() <= 0);
    }

    public void removeEffect(GameRoom room, String playerId, EffectType type) {
        room.getEffects().removeIf(effect -> effect.getTargetPlayerId().equals(playerId)
                && effect.getType() == type);
    }
}